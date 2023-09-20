#!/bin/bash

################################################################################
#  flink任务提交工具，所需要的参数需要提前export到环境变量。45345
################################################################################
RANGE_START=50000
RANGE_END=60000
UUID=`cat /proc/sys/kernel/random/uuid|sed -r "s/-//g"`
#UUID="defaultname"
#当前脚本所在目录
bin=$(cd $(dirname $0);pwd)

CHUNJUN_JOB_NAME=${CHUNJUN_JOB_NAME}-${UUID}
FLINK_K8S_CONF_PROP=$FLINK_K8S_CONF_PROP
JOB_TYPE=${JOB_TYPE:-sync}

export CHUNJUN_JOB=${CHUNJUN_JOB}
# 容器yaml模板
export POD_TEMPLATE_PATH=${POD_TEMPLATE_PATH:-${bin}/chunjunPodTemplete.yaml}

#flink目录
export FLINK_HOME=${FLINK_HOME}
#flink历史记录目录
export HIS_JOB_PATH=${HIS_JOB_PATH}
#flinkJobID目录
export JOB_ID_PATH=${JOB_ID_PATH}
#python目录
export PYTHON_HOME=${PYTHON_HOME}
#镜像地址
export CHUNJUN_IMAGE=${CHUNJUN_IMAGE:-10.2.4.16:5000/release/chunjun:sopei}
#提交程序端口
export PORT=${PORT}

# 任务完成后，job文件地址
JOB_FILE_PATH=""
# 如果模板文件参数存在，则添加
POD_TEMPLATE_OPTION=""

###############################################
#解析参数，对于必选的参数不存在则退出程序
###############################################
function checkOption() {
  if [[ -z $CHUNJUN_JOB ]]; then
    echo 'CHUNJUN_JOB class must be define!'
    exit 1
  fi

  if [[ -z $PORT ]]; then
    initPort
  fi

  echo "POD_TEMPLATE_PATH--->$POD_TEMPLATE_PATH"
  echo "POD_TEMPLATE_OPTION--->$POD_TEMPLATE_OPTION"
  echo "HIS_JOB_PATH--->$HIS_JOB_PATH"
  echo "FLINK_HOME--->$FLINK_HOME"
  CHUNJUN_HOME=/opt/module/chunjun-1.16
  echo "CHUNJUN_HOME--->$CHUNJUN_HOME"
  echo "PYTHON_HOME--->$PYTHON_HOME"
  echo "CHUNJUN_IMAGE--->$CHUNJUN_IMAGE"
  echo "PORT--->$PORT"
  echo "CHUNJUN_JOB_NAME--->$CHUNJUN_JOB_NAME"
  echo "CHUNJUN_JOB--->$CHUNJUN_JOB"
}


###############################################
#初始化PORT端口，如果有传入值，则不执行此函数，无PORT传参，
# 则随机生成，生成端口如果检测被占用，则换一个
###############################################
function initPort() {
  port=$(shuf -i $RANGE_START-$RANGE_END -n1)
  PORT=$port
  echo "Start Check the port is used ?, url is :\n 127.0.0.1:$port  "

}

###############################################
#提交k8s作业，使用原生flink run方式提交，
# 根据返回值判断是否成功
###############################################
function submitJob() {

  echo "Start submit chunjun on K8s Job"
  conf_prop='{"kubernetes.container.image":"'${CHUNJUN_IMAGE}'","kubernetes.namespace":"default","rest.port":'${PORT}${FLINK_K8S_CONF_PROP}',"kubernetes.pod-template-file":"'${POD_TEMPLATE_PATH}'"}'

  echo "Start submit chunjun on K8s Job"
  echo $conf_prop

  $CHUNJUN_HOME/bin/chunjun-kubernetes-application.sh \
    -jobType $JOB_TYPE \
    -job  $CHUNJUN_JOB \
    -jobName $CHUNJUN_JOB_NAME \
    -remoteChunJunDistDir /opt/flink/lib/chunjun-dist \
    -flinkLibDir $FLINK_HOME/lib \
    -flinkConfDir $FLINK_HOME/conf \
    -confProp $conf_prop

  if [ $? -ne 0 ]; then
    echo "Job submit failed"
    exit 1
  else
    echo "Job submit success"
  fi
}

###############################################
#提交k8s作业之后需要等待任务执行完成，
# 目前没有什么好办法，只能轮询查找job完成后写入的
# job文件并解析 "$PYTHON_HOME"/bin/python3
###############################################
function waitJobComplete(){
    /usr/bin/python3 "${bin}"/getJobStatusFromHttp.py -f $CHUNJUN_JOB_NAME
    
    if [ $? -ne 0 ]; then
      echo "Job run failed"
      exit 1
    else
      echo "Job run success"
    fi

}

function start(){
   checkOption
   submitJob
   waitJobComplete
}
echo "\n"
echo "#########################################################"
echo "#*******************************************************#"
echo "#*******************************************************#"
echo "#*************Flink Submit On K8s Tool******************#"
echo "#*******************************************************#"
echo "#**************** @Version 1.0 *************************#"
echo "#**************** @Auther  steve ***********************#"
echo "#*****************@Date    2022/06/17*******************#"
echo "#*******************************************************#"
echo "#*******************************************************#"
echo "#########################################################"

start "$@"