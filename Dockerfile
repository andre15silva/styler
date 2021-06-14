# docker build -t ubuntu1604py36
FROM ubuntu:16.04

RUN apt-get update

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get -y install locales
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update
RUN apt-get install -y python3.6
RUN apt-get install -y python3-pip


RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections

RUN apt-get install -y python-software-properties
RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
ENV PATH="${PATH}:$JAVA_HOME/bin"

RUN apt-get install -y git vim

RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.6 2

WORKDIR /Styler

COPY . /Styler

RUN pip3 install --upgrade pip==21.1.2
RUN /Styler/setup_env.sh
