#!/bin/sh

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


java -cp $LOCALCLASSPATH -Xmx2G $*
