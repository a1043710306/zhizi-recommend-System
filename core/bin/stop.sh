#!/bin/sh

############ define some various #####################
PROCESS_NAME=CoreLauncher

####################################################


get_pid() {
        JAVA_PID=`ps -C java -f |grep "$1"|grep -v grep|awk '{print $2}'`
        echo $JAVA_PID;
}

PID=`get_pid "${PROCESS_NAME}"`

if [ -z "$PID" ]; then
        echo "Do not found server(${PROCESS_NAME})!"
        exit 0;
else
        kill -15 $PID
fi

LOOPS=0
while (true);
do
    PID2=`get_pid "${PROCESS_NAME}"`
    if [ -z "${PID2}" ] ; then
        echo "stop ${PROCESS_NAME}(${PID})! cost:${LOOPS}s"
        break;
    fi

    if [ "$LOOPS" -ge 10 ] ; then
        kill -9 ${PID2}
    fi

    let LOOPS=LOOPS+1
    sleep 1
done

exit 0;
