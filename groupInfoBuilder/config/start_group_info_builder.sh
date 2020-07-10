#!/bin/bash
filepath=$(cd "$(dirname "$0")"; pwd)
cd $filepath
PIDFILE=./group_info_builder.pid

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

nohup java -cp $LOCALCLASSPATH -Xms2g -Xmx2g -XX:+DisableExplicitGC -XX:+DisableAttachMechanism -XX:ErrorFile=./hs_err_pid%p.log com.inveno.server.contentgroup.GroupInfoBuilder > group_info_builder.out &
echo $! > "${PIDFILE}"
chmod 644 "${PIDFILE}"
