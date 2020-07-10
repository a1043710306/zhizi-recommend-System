#!/bin/bash
filepath=$(cd "$(dirname "$0")"; pwd)
cd $filepath
service="Feeder"
script=./start_feeder.sh

PIDFILE=./feeder.pid

if [ -e "${PIDFILE}" ] && (ps -u $(whoami) -opid= | grep -P "^\s*$(cat ${PIDFILE})$" &> /dev/null); then
        echo "$service is already running."
        exit
fi

$script
echo "$service is awaked running."
