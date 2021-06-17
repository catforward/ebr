#!/bin/bash

APP_NAME=ebr.jar

is_exist() {
	pid=$(ps -ef | grep ${APP_NAME} | grep -v grep | awk '{print $2}')
	if [ -z ${pid} ]; then
		return 1
	else
		return 0
	fi
}

is_exist
if [ $? -eq 0 ]; then
	echo "${APP_NAME} is already running. pid=${pid} shutdown...."
  kill -15 ${pid}
else
  echo "${APP_NAME} is not running."
fi