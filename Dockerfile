FROM flink:1.15.2-scala_2.12-java11

COPY scps-lib      /opt/flink/lib/

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && mkdir /opt/flink/plugins/s3-fs-hadoop \
    && cp /opt/flink/opt/flink-s3-fs-hadoop-1.15.2.jar /opt/flink/plugins/s3-fs-hadoop/ \
    && mkdir /opt/flink/plugins/s3-fs-presto \
    && cp /opt/flink/opt/flink-s3-fs-presto-1.15.2.jar /opt/flink/plugins/s3-fs-presto/
