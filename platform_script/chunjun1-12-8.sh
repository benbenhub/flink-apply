#!/bin/bash

RANGE_START=50000
RANGE_END=60000
UUID=$(uuidgen |sed 's/-//g')

port=0
while [[ $port -eq 0 ]]
do
  # 生成50000到60000的随机数
  port=$(shuf -i $RANGE_START-$RANGE_END -n 1)

  # 检测端口是否被占用
  if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
    port=0
  fi
done

source ${RES_PATH}/password/pro-password.sh

cj_h=/opt/module/chunjun-1.12.8_flink1.12.7

job_json=`echo $1 | sed 's/${CLICKHOUSE_USERNAME}/'${CLICKHOUSE_USERNAME}'/g'` 
job_json=`echo $job_json | sed 's/${CLICKHOUSE_PASSWORD}/'${CLICKHOUSE_PASSWORD}'/g'`
job_json=`echo $job_json | sed 's/${CLICKHOUSE_URL}/'${CLICKHOUSE_URL}'/g'`
job_json=`echo $job_json | sed 's/${MYSQL_write_USERNAME}/'${MYSQL_write_USERNAME}'/g'`
job_json=`echo $job_json | sed 's/${MYSQL_write_PASSWORD}/'${MYSQL_write_PASSWORD}'/g'`
job_json=`echo $job_json | sed 's/${MYSQL_write_URL}/'${MYSQL_write_URL}'/g'`

echo $job_json > $cj_h/job/'chunjun_'$UUID'.json'

sh $cj_h/bin/chunjun-local.sh \
    -jobType sync \
    -job $cj_h/job/'chunjun_'$UUID'.json' \
    -jobName 'chunjun_'$UUID \
    -confProp '{"rest.port":'$port',"rest.bind-port":"50100-51000"}'