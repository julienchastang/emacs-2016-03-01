# https://github.com/Unidata/Unidata-Dockerfiles/tree/master/python
FROM unidata/python

MAINTAINER Julien Chastang <chastang@ucar.edu>

###
# Usual maintenance
###

USER root

RUN apt-get update

RUN apt-get install -y tar curl git gawk emacs

###
# various emacs ancillary programs
###

RUN apt-get install -y ispell aspell aspell-en hunspell hunspell-en-us ditaa texlive-xetex

WORKDIR $HOME

###
# Create some directories
###

RUN mkdir -p $HOME/.emacs.d/git

RUN mkdir -p $HOME/work

RUN mkdir -p $HOME/downloads

RUN mkdir -p $HOME/bin

###
# Java
###

RUN apt-get install -y build-essential openjdk-8-jdk

# certificates / security
ENV CURL_CA_BUNDLE /etc/ssl/certs/ca-certificates.crt

# cacerts not in version control
ADD cacerts /etc/ssl/certs/java/cacerts

###
# python/conda 
###

USER python

WORKDIR /home/python

ADD emacs-python.yml /home/python/

RUN conda env update --name root -f /home/python/emacs-python.yml

RUN pip install epc

WORKDIR $HOME/.emacs.d/git

###
# Clone various repos
###

USER root

# emacs config
RUN rm -rf dotemacs && git clone -b python \
    https://github.com/julienchastang/dotemacs

# org mode
RUN rm -rf org-mode && git clone --branch release_8.3.3 \
    git://orgmode.org/org-mode.git

# Must manually curate some emacs packages not in melpa-stable

# ob-ipython
RUN rm -rf ob-ipython &&  git clone https://github.com/gregsexton/ob-ipython

# emacs-rotate
RUN rm -rf emacs-rotate && git clone https://github.com/daic-h/emacs-rotate

# build org mode
WORKDIR $HOME/.emacs.d/git/org-mode

RUN make autoloads

###
# Clojure
###

WORKDIR $HOME/bin

ENV PATH $HOME/bin:$PATH

RUN curl -SL \
    https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -o \
    $HOME/bin/lein

RUN sh $HOME/bin/lein

RUN chmod a+x $HOME/bin/lein

ADD profiles.clj $HOME/.lein/profiles.clj

###
# Emacs initialization
###

ADD init.el $HOME/.emacs.d/init.el

# kludge just to force emacs to run once to grab elpa
RUN emacs --batch -l $HOME/.emacs.d/init.el

# Jedi, autocompletion/static analysis library for Python
RUN pip install -U $HOME/.emacs.d/elpa/jedi-core*

# Get rid of weird versions of org floating around
RUN rm -rf $HOME/.emacs.d/elpa/org-2*

###
# Work volumes
###

WORKDIR $HOME/work

VOLUME $HOME/work

###
# Wrapping up some stuff
###

USER root

RUN rm -rf $HOME/downloads/*

ADD .bashrc $HOME/

RUN chown -R python:python $HOME/

###
# Start container
###

# Python user inherited from base image, and is easiest to work with rather than
# creating a new user

USER python

CMD bash
