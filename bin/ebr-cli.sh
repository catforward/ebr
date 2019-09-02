#!/bin/bash

SCRIPT_ROOT=$(cd $(dirname $0); pwd)
APP_NAME=ebr-all.jar
APP_JAR=${SCRIPT_ROOT}/../libs/${APP_NAME}
CLI_LOG=${SCRIPT_ROOT}/../logs/cli_${APP_NAME}.log
CMD_ARGS=$@

JAVA_BIN=/usr/bin/java
JAVA_OPTS="-Xms32m -Xmx128m -Xlog:gc:${SCRIPT_ROOT}/../logs/gc_${APP_NAME}.log"

usage() {
	echo "Usage: ebr-cli.sh [start|stop|restart|status] [jar args]"
	exit 1
}

is_exist() {
	pid=`ps -ef | grep ${APP_NAME} | grep -v grep | awk '{print $2}'`
	if [ -z ${pid} ]; then
		return 1
	else
		return 0
	fi
}

start() {
	is_exist
	if [ $? -eq 0 ]; then
		echo "${APP_NAME} is already running. pid=${pid}"
	else
	    START_TIME=`date "+%Y-%m-%d %H:%M:%S"`
	    echo "--------------------- ${START_TIME} ---------------------" >> ${CLI_LOG} 2>&1
	    nohup ${JAVA_BIN} ${JAVA_OPTS} -jar ${APP_JAR} ${CMD_ARGS[@]:1}  >> ${CLI_LOG} 2>&1 &
	fi
}

stop() {
	is_exist
	if [ $? -eq 0 ]; then
		kill -9 ${pid}
	else
		echo "${APP_NAME} is not running"
	fi
}

status() {
	is_exist
	if [ $? -eq 0 ]; then
		echo "${APP_NAME} is running. pid is ${pid}"
	else
		echo "${APP_NAME} is not running."
	fi
}

restart() {
	stop
	start
}

cd ${EBR_ROOT}

case "$1" in
	"start")
		start
		;;
	"stop")
		stop
		;;
	"status")
		status
		;;
	"restart")
		restart
		;;
	*)
		usage
		;;
esac