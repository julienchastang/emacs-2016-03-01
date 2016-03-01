package meetup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * The Class NPY.
 */
public class NPY {

	/**
	 * Public interface for taking a 2d float array and file handle
	 * write a NPY formatted byte array.
	 * 
	 * http://docs.scipy.org/doc/numpy-dev/neps/npy-format.html
	 *
	 * @param arr the array
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void write(float[][] arr, File file) throws IOException {

		byte[] b = numpyBytes();
		String header = header(arr.length, arr[0].length);
		byte[] padding = pad(header);
		byte[] head = concatenate(header.getBytes(StandardCharsets.US_ASCII),
				padding);
		byte[] twobs = headerLength(head);

		byte[] first = new byte[] { (byte) 0x93, b[0], b[1], b[2], b[3], b[4],
				(byte) 0x01, (byte) 0x00, twobs[0], twobs[1] };
		byte[] cat1 = concatenate(first, head);
		byte[] data = bytify(arr);
		byte[] cat3 = concatenate(cat1, data);

		FileOutputStream fos = new FileOutputStream(file);
		fos.write(cat3);
		fos.close();
	}

	/**
	 * Header.
	 *
	 * @param d the d
	 * @return the string
	 */
	private static String header(int ...d) {
		StringBuffer s = new StringBuffer("(");
		for (int i = 0; i < d.length; i++) {
			s.append(i == 0 ? d[i] : ","+ d[i]);
		}
		s.append(")");
		String header = "{'descr': '>f4', 'fortran_order': False, 'shape': "
				+ s + ", }";
		return header;
	}

	/**
	 * Header length.
	 *
	 * @param head the head
	 * @return the byte[]
	 */
	private static byte[] headerLength(byte[] head) {
		ByteBuffer allocate = ByteBuffer.allocate(2);
		allocate.order(ByteOrder.LITTLE_ENDIAN);
		byte[] twobs = allocate.putChar((char) head.length).array();
		return twobs;
	}

	/**
	 * 'NUMPY' bytes .
	 *
	 * @return the 'NUMPY' byte[]
	 */
	private static byte[] numpyBytes() {
		byte[] b = "NUMPY".getBytes(StandardCharsets.US_ASCII);
		return b;
	}

	/**
	 * Pad with white space.
	 *
	 * @param header the header
	 * @return the byte[]
	 */
	private static byte[] pad(String header) {
		int header_len = header.length();
		int pad = 16 - ((header_len + 10) % 16);
		byte[] padding = new byte[pad];
		for (int i = 0; i < padding.length - 1; i++) {
			padding[i] = (byte) 0x20;
		}
		padding[padding.length - 1] = "\n".getBytes(StandardCharsets.US_ASCII)[0];
		return padding;
	}

	/**
	 * Bytify. Take a 2d float array and return a byte array.
	 *
	 * @param arr the arr
	 * @return the byte[]
	 */
	private static byte[] bytify(float[][] arr) {
		// Assume a square 2d array
		ByteBuffer allocate2 = ByteBuffer.allocate(arr.length * arr[0].length
				* 4);
		allocate2.order(ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				allocate2.putFloat(arr[i][j]);
			}
		}
		byte[] data = allocate2.array();
		return data;
	}

	/**
	 * Concatenate byte arrays.
	 *
	 * @param b0 the b0
	 * @param b1 the b1
	 * @return the byte[]
	 */
	private static byte[] concatenate(byte[] b0, byte[] b1) {
		ByteBuffer buffer = ByteBuffer.allocate(b0.length + b1.length);
		buffer.put(b0);
		buffer.put(b1);
		return buffer.array();
	}
}
