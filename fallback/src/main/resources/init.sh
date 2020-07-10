#!/bin/sh
SHELL_PROG=./init.sh
DIR="${BASH_SOURCE-$0}"
DIR=`dirname ${BASH_SOURCE-$0}`
ROOT_HOME=$DIR;export ROOT_HOME
APP_HOME=$DIR/resources/;export APP_HOME
DATA_HOME=$DIR/data/;export DATA_HOME
LIB_HOME=$DIR/lib;export LIB_HOME
CLASSPATH=$ROOT_HOME:$APP_HOME:$DATA_HOME:$LIB_HOME/*;export CLASSPATH
export LC_ALL=zh_CN.UTF8

#ulimit -n 102297

#process name, need to change
MAINPROG=com.inveno.fallback.FallbackServerStartUp

#java -Xdebug -Xrunjdwp:transport=dt_socket,address=6777,server=y,suspend=y -Xms800m -Xmx800m -Xmn256m -Xss256k  -classpath $CLASSPATH $MAINPROG
#java  -Xms800m -Xmx800m -Xmn256m -Xss256k  -classpath $CLASSPATH $MAINPROG

start() {
        echo "[`date`] Begin starting $MAINPROG ... "
        ##java -Xms1024m -Xmx2048m -Xmn2000m -Xss256k -XX:SurvivorRatio=8 -XX:+UseParNewGC -XX:ParallelGCThreads=6 -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=50 -XX:+UseCMSInitiatingOccupancyOnly -XX:MaxTenuringThreshold=64 -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCTimeStamps -Xloggc:gc.log  -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=3  -classpath $CLASSPATH $MAINPROG &
        nohup java -server -Xmx1g -Xms1g -Xmn512m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump -Dorg.apache.activemq.UseDedicatedTaskRunner=false -classpath $CLASSPATH $MAINPROG >/dev/null 2>&1 &
        if [ $? -eq 0 ]
        then
			echo "[`date`] Startup $MAINPROG success."
			return 0
        else
			echo "[`date`] Startup $MAINPROG fail."
			return 1
        fi
}

debug() {
        echo "[`date`] Begin starting $MAINPROG... "
        java -Xdebug -Xrunjdwp:transport=dt_socket,address=6777,server=y,suspend=y -Xms10000m -Xmx10000m -Xmn256m -Xss256k  -classpath $CLASSPATH $MAINPROG &
        if [ $? -eq 0 ]
        then
			echo "[`date`] Startup $MAINPROG success."
			return 0
        else
			echo "[`date`] Startup $MAINPROG fail."
			return 1
        fi
}

stop() {
    echo "[`date`] Begin stop $MAINPROG... "
    PROGID=`ps -ef|grep "$MAINPROG"|grep -v "grep"|sed -n '1p'|awk '{print $2" "$3}'`
	if [ -z "$PROGID" ]
	then
		echo "[`date`] Stop $MAINPROG fail, service is not exist."
		return 1
	fi
	
    kill -9 $PROGID
    if [ $? -eq 0 ]
    then
		echo "[`date`] Stop $MAINPROG success."
		return 0
    else
		echo "[`date`] Stop $MAINPROG fail."
		return 1
    fi
}


case "$1" in
start)
  start
  exit $?
  ;;
stop)
  stop
  exit $?
  ;;
restart)
  stop
  start
  exit $?
  ;;
debug)
  debug
  exit $?
  ;;
*)
  echo "[`date`] Usage: $SHELL_PROG {start|debug|stop|restart}"
  exit 1
  ;;
esac

