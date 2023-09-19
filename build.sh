echo 开始构建jar包
rm -rf scps-lib
mvn clean
mvn package
echo 构建完成
mv scps-basics-batch/target/*.jar scps-lib/
mv scps-core/target/*.jar scps-lib/
mv scps-basics-real/target/*.jar scps-lib/
mv scps-bi/target/*.jar scps-lib/
mv scps-sync/target/*.jar scps-lib/
mv scps-vin/target/*.jar scps-lib/
echo 打包docker镜像
sudo docker build -t 10.43.61.207:5000/scps-flink-1.13.6:dev .
echo 登录镜像仓库
sudo docker login 10.43.61.207:5000 -u admin -p sopei2021
echo 登录完成
sudo docker push 10.43.61.207:5000/scps-flink-1.13.6:dev
echo 上传完成
