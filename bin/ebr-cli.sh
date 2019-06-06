#!/bin/bash

JAVA_BIN=/usr/bin/java
JAVA_OPTS="-Xms32m -Xmx64m"

SCRIPT_ROOT=$(cd $(dirname $0); pwd)
EBR_ROOT=${SCRIPT_ROOT}/..
APP_NAME=ebr-0.1-all.jar
APP_PID=${APP_NAME}.pid
APP_JAR=${EBR_ROOT}/libs/${APP_NAME}

usage() {
	echo "Usage: ebr-cli.sh [start|stop|restart|status]"
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
		nohup ${JAVA_BIN} ${JAVA_OPTS} -jar ${APP_JAR} > /dev/null 2>&1 &
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