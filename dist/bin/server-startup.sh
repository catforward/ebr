#!/bin/bash

SCRIPT_ROOT=$(cd $(dirname $0); pwd)
export EBR_ROOT=$(cd ${SCRIPT_ROOT}/..; pwd)
APP_NAME=ebr-server.jar
APP_JAR=${EBR_ROOT}/libs/${APP_NAME}
CLI_LOG=${EBR_ROOT}/logs/${APP_NAME}.log

JAVA_BIN=/usr/bin/java
JAVA_OPTS="-Djava.awt.headless=true -Xms32m -Xmx64m -XX:+ShowCodeDetailsInExceptionMessages -XX:+UseG1GC -Xlog:gc:${EBR_ROOT}/logs/gc_${APP_NAME}.log"

is_exist() {
    pid=$(ps -ef | grep ${APP_NAME} | grep -v grep | awk '{print $2}')
    if [ -z ${pid} ]; then
        return 1
    else
        return 0
    fi
}

cd ${EBR_ROOT}

is_exist
if [ $? -eq 0 ]; then
    echo "${APP_NAME} is already running. pid=${pid}"
else
    START_TIME=$(date "+%Y-%m-%d %H:%M:%S")
    echo "--------------------- START: ${START_TIME} ---------------------" >> ${CLI_LOG} 2>&1
    nohup ${JAVA_BIN} ${JAVA_OPTS} -jar ${APP_JAR} $@ >/dev/null 2>&1 &
fi