#!/bin/bash

date
ip=172.31.8.20
port=8888
hashName=zhizi.ufs.newuser
logpath=/usr/local/inveno/Profile/INV/Profile/INV.Profile_profile_
yd=`date -dyesterday +%Y%m%d`
logpath=$logpath$yd".log"

cat $logpath | /home/developc/uid_create/ct 8 $ip $port $hashName
#cmd="cat $logpath | /home/developc/uid_create/ct 8 $ip $port $hashName"
#echo $cmd
date
