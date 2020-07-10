#!/bin/bash
filepath=$(cd "$(dirname "$0")"; pwd)
cd $filepath
PIDFILE=./feeder.pid

LOCALCLASSPATH=.:build

# Include all the jar libraries.
for i in "lib"/*.jar
do
    if [ -z "$LOCALCLASSPATH" ]; then
        LOCALCLASSPATH="$i"
    else
        LOCALCLASSPATH="$LOCALCLASSPATH:$i"
    fi
done

nohup java -cp $LOCALCLASSPATH -Duser.timezone="Asia/Shanghai" -Xms2g -Xmx2g -XX:+DisableExplicitGC -XX:+DisableAttachMechanism -XX:ErrorFile=./hs_err_pid%p.log com.inveno.feeder.Feeder >feeder.out &
echo $! > "${PIDFILE}"
chmod 644 "${PIDFILE}"
