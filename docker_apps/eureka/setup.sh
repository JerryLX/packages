#!/bin/bash
#author: Huang Jinhua
#date: 01/05/2017

echo "Try to pull Eureka docker image ..."

#Step 1: Install docker firstly
if [ -z "$(which docker 2>/dev/null)" ]; then
    INSTALL_CMD=""
    if [ ! -z "$(which yum 2>/dev/null)" ] ; then
        INSTALL_CMD="yum"
    elif [ ! -z "$(which apt-get 2>/dev/null)" ] ; then
        INSTALL_CMD="apt-get"
    else 
        echo "Not know how to install docker ..."
        exit 0
    fi
    ${INSTALL_CMD} install -y -q docker docker.io
fi

#Step 2: Start docker service
if [ -z "$(ps -aux | grep docker | grep -v grep)" ] ; then
    service docker start
fi

#Step 3: Try to pull latest eureka docker image
if [ -z "$(docker images | grep 'openestuary/eureka' | grep -v grep)" ]  ; then
    docker pull openestuary/eureka:5.0
else 
    echo "Eureka docker image has been pulled"
fi

