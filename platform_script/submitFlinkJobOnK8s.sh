#!/usr/bin/env bash

################################################################################
#  flink任务提交工具，所需要的参数需要提前export到环境变量。
################################################################################
RANGE_START=50000
RANGE_END=60000
UUID=$(uuidgen |sed 's/-//g')
#当前脚本所在目录
bin=$(cd $(dirname $0);pwd)
# 提交k8s容器ID
export APP_NAME=${APP_NAME:-$UUID}
# jobmanager内存
export JB_MEM=${JB_MEM:-1024m}
# taskmanager内存
export TM_MEM=${TM_MEM:-1024m}
# 并行度
export PARALIZE=${PARALIZE:-1}

# 运行主类
export MAIN_CLASS=${MAIN_CLASS}
# 容器yaml模板
export POD_TEMPLATE_PATH=${POD_TEMPLATE_PATH:-$bin/defaultPodTemplete.yaml}
# 应用app jar包路径
export APP_PATH=${APP_PATH}
# flink（k8s）自定义参数
export FLINK_OPTION=${FLINK_OPTION}
# app程序自定义参数
export APP_OPTION=${APP_OPTION}
#flink目录
export FLINK_HOME=${FLINK_HOME}
#flink历史记录目录
export HIS_JOB_PATH=${HIS_JOB_PATH}
#python目录
export PYTHON_HOME=${PYTHON_HOME}
#镜像地址
export APP_IMAGE=${APP_IMAGE:-10.2.4.16:5000/release/scps:v1}
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
  if [[ -z $MAIN_CLASS ]]; then
    echo 'MAIN_CLASS class must be define!'
    exit 1
  fi

  if [[ -z $APP_PATH ]]; then
    echo 'APP_PATH class mast define!'
    exit 1
  fi

  if [[ -z $PORT ]]; then
    initPort
  fi

  if [[ -n $POD_TEMPLATE_PATH ]]; then
    POD_TEMPLATE_OPTION="-Dkubernetes.pod-template-file=$POD_TEMPLATE_PATH"
  fi

  if [[ -n $APP_IMAGE ]]; then
    APP_IMAGE="-Dkubernetes.container.image=$APP_IMAGE"
  fi

  echo "APP_NAME--->$APP_NAME"
  echo "APP_PATH--->$APP_PATH"
  echo "JB_MEM--->$JB_MEM"
  echo "TM_MEM--->$TM_MEM"
  echo "PARALIZE--->$PARALIZE"
  echo "MAIN_CLASS--->$MAIN_CLASS"
  echo "POD_TEMPLATE_PATH--->$POD_TEMPLATE_PATH"
  echo "FLINK_OPTION--->$FLINK_OPTION"
  echo "APP_OPTION--->$APP_OPTION"
  echo "POD_TEMPLATE_OPTION--->$POD_TEMPLATE_OPTION"
  echo "HIS_JOB_PATH--->$HIS_JOB_PATH"
  echo "FLINK_HOME--->$FLINK_HOME"
  echo "PYTHON_HOME--->$PYTHON_HOME"
  echo "APP_IMAGE--->$APP_IMAGE"
  echo "PORT--->$PORT"
}


###############################################
#初始化PORT端口，如果有传入值，则不执行此函数，无PORT传参，
# 则随机生成，生成端口如果检测被占用，则换一个
###############################################
function initPort() {
  port=$(shuf -i $RANGE_START-$RANGE_END -n1)
  echo "Start Check the port is used ?, url is :\n 127.0.0.1:$port  "
  /usr/local/curl/bin/curl 127.0.0.1:$port
  if [ $? -ne 0 ]; then
    echo "The $port is available！"
    PORT=$port
  else
     echo "the port $port is used, check next..."
     initPort
  fi

}

###############################################
#提交k8s作业，使用原生flink run方式提交，
# 根据返回值判断是否成功
###############################################
function submitJob() {

  echo "Start submit Flink Natvie on K8s Job"

  sudo $FLINK_HOME/bin/flink run-application -t kubernetes-application -p $PARALIZE \
    -c $MAIN_CLASS \
    -Dkubernetes.cluster-id=$APP_NAME \
    -Djobmanager.memory.process.size=$JB_MEM \
    -Dtaskmanager.memory.process.size=$TM_MEM \
    -Dkubernetes.pod-template-file=$POD_TEMPLATE_PATH \
    -Drest.port=$PORT \
    $APP_IMAGE \
    $POD_TEMPLATE_OPTION \
    $FLINK_OPTION \
    local://$APP_PATH $APP_OPTION

  if [ $? -ne 0 ]; then
    echo "Job submit failed"
    exit 1
  else
    echo "Job submit success"
  fi
}

###############################################
#提交k8s作业之后使用list命令查找job id,
# 如果集群反应不及时会出现无job情况，需要循环检测，
# 超过3次未检测到视为失败
###############################################
function parseJobId(){
    #重试次数，超过限制则失败
    tryTime=$1
    if [[ $tryTime -gt 3 ]]; then
        echo "Failed to get job task more than 3 times, please check!"
        exit 1;
    fi

    echo "Wait for the pod to start"
    # 等待job启动获取jobid
    job_describe=`sudo $FLINK_HOME/bin/flink list --target kubernetes-application -Dkubernetes.cluster-id=$APP_NAME`
    if [ $? -ne 0 ]; then
        echo "Get job list failed ,log is :${job_describe}"
        exit 1;
    else
        echo "Job list is : $job_describe"
    fi

    # jobid获取到后解析jobid字符串
    result=`echo "${job_describe}" |awk -F ":" 'NR==4 {print $4 }' |awk '{gsub(/^[\t ]*|[\t ]*$/,"");print}'`

    echo "Get job id result is : $result"

    # 解析不到，重试+1
    if [[ -z "$result" ]]; then
        echo "The job information was not obtained, the container may not be started and is retrying"
        parseJobId `expr $tryTime+1`
    else
        JOB_FILE_PATH=`echo $HIS_JOB_PATH/$result`
    fi

}

###############################################
#提交k8s作业之后需要等待任务执行完成，
# 目前没有什么好办法，只能轮询查找job完成后写入的
# job文件并解析
###############################################
function waitJobComplete(){
    JOB_FILE_PATH=$1
    echo "The target job file is : $JOB_FILE_PATH"

    while [ ! -f "$JOB_FILE_PATH" ]
    do
    echo "Job file not found! next check at 60s after!!"
    sleep 60s
    done

    echo "Found job file , start parse job status...."
    /usr/bin/python3 "$bin"/getJobStatus.py -f $JOB_FILE_PATH


    if [ $? -ne 0 ]; then
        echo "Job has failed ! please check log: $JOB_FILE_PATH"
        exit 1;
    else
        echo "Job run successful ! log is : $JOB_FILE_PATH"
    fi

}

function start(){
   checkOption
   submitJob
   parseJobId 1
   waitJobComplete $JOB_FILE_PATH
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