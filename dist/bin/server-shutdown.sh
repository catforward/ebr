#!/bin/bash

SCRIPT_ROOT=$(cd $(dirname $0); pwd)
export EBR_ROOT=$(cd ${SCRIPT_ROOT}/..; pwd)
APP_NAME=ebr-server.jar

CLI_LOG=${EBR_ROOT}/logs/${APP_NAME}.log

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
    END_TIME=$(date "+%Y-%m-%d %H:%M:%S")
    echo "--------------------- END  : ${END_TIME} ---------------------" >> ${CLI_LOG} 2>&1
    kill -15 ${pid}
else
    echo "${APP_NAME} is not running."
fi