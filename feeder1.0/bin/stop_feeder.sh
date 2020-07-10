#!/bin/bash
PIDFILE=./feeder.pid
cat ${PIDFILE} | xargs kill -9
rm ${PIDFILE}
