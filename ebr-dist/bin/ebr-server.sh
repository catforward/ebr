#!/bin/bash

SCRIPT_ROOT=$(cd $(dirname $0); pwd)
export EBR_ROOT=$(dirname ${SCRIPT_ROOT})
APP_NAME=ebr-server.jar
APP_JAR=${EBR_ROOT}/lib/${APP_NAME}
CLI_LOG=${EBR_ROOT}/logs/${APP_NAME}.log
CMD_ARGS=$@

JAVA_BIN=/usr/bin/java
JAVA_OPTS="-Xms64m -Xmx256m -Xlog:gc:${EBR_ROOT}/logs/gc_${APP_NAME}.log"

is_exist() {
	pid=`ps -ef | grep ${APP_NAME} | grep -v grep | awk '{print $2}'`
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
    START_TIME=`date "+%Y-%m-%d %H:%M:%S"`
    echo "--------------------- ${START_TIME} ---------------------" >> ${CLI_LOG} 2>&1
    nohup ${JAVA_BIN} ${JAVA_OPTS} -jar ${APP_JAR} $@ >> ${CLI_LOG} 2>&1 &
fi
