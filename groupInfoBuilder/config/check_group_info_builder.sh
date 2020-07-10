#!/bin/bash
filepath=$(cd "$(dirname "$0")"; pwd)
cd $filepath
service="GroupInfoBuilder"
script=./start_group_info_builder.sh

PIDFILE=./group_info_builder.pid

if [ -e "${PIDFILE}" ] && (ps -u $(whoami) -opid= | grep -P "^\s*$(cat ${PIDFILE})$" &> /dev/null); then
        echo "$service is already running."
        exit
fi

$script
echo "$service is awaked running."
