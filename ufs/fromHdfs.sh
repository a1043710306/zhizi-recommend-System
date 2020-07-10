#!/bin/bash

jarFile=/home/inveno/feature-assembly-1.0.jar
confDif=/etc/hadoop/conf
yd=`date -dyesterday +%Y%m%d`
if [ $1 == "network" ];then
    hdfsFile=/inveno-projects/offline-user-feature/output/stat-user-network-feature/${yd}/user-network-feature.${yd}
else
    hdfsFile=/inveno-projects/offline-user-feature/output/stat-user-time-feature/${yd}/user-time-feature.${yd}
fi

addrs=192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1

#time java -cp feature-assembly-1.0.jar feature.Hdfs /etc/hadoop/conf /inveno-projects/offline-user-feature/output/stat-user-network-feature/20160806/user-network-feature.20160806 "192.168.1.237:18888:1,192.168.1.79:18888:1,192.168.1.106:18888:1,192.168.1.108:18888:1,192.168.1.110:18888:1,192.168.1.112:18888:1"

cmd="java -cp "${jarFile}" feature.Hdfs "${confDif}" "${hdfsFile}" "${addrs}

$cmd
