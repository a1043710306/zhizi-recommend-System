#!/bin/sh
PIDFILE=./group_info_builder.pid
cat ${PIDFILE} | xargs kill -9
rm ${PIDFILE}
