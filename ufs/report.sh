#!/bin/bash

cmd1="/usr/local/inveno/Ufs/kafka_2.10-0.9.0.1/bin/kafka-consumer-offset-checker.sh --group "
cmd2=" --zookeeper 192.168.1.60:2181,192.168.1.61:2181,192.168.1.62:2181 --topic "

while [ 1 -eq 1 ];do
    group=zhizi.impression-reformat
    topic=impression-reformat
    cmd=$cmd1$group$cmd2$topic
    lag=`$cmd 2>&1|grep $group|grep $topic|awk 'BEGIN{a=0}{a+=$6}END{print a}'`
    echo -e "ufs.lag.reformat.impression\t$lag"|/usr/local/inveno/Ufs/bin/report >/dev/null 2>&1

    group=zhizi.click-reformat
    topic=click-reformat
    cmd=$cmd1$group$cmd2$topic
    lag=`$cmd 2>&1|grep $group|grep $topic|awk 'BEGIN{a=0}{a+=$6}END{print a}'`
    echo -e "ufs.lag.reformat.click\t$lag"|/usr/local/inveno/Ufs/bin/report >/dev/null 2>&1

    group=zhizi.ufs
    topic=click
    cmd=$cmd1$group$cmd2$topic
    lag=`$cmd 2>&1|grep $group|grep $topic|awk 'BEGIN{a=0}{a+=$6}END{print a}'`
    echo -e "ufs.lag.click\t$lag"|/usr/local/inveno/Ufs/bin/report >/dev/null 2>&1

    sleep 1s
done

