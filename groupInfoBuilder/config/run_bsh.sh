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

java -Xmx2G -cp $LOCALCLASSPATH -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=log4j.properties bsh.Interpreter $*
