function parseYaml {
   local prefix=$2
   local s='[[:space:]]*' w='[a-zA-Z0-9_]*' fs=$(echo @|tr @ '\034')
   sed -ne "s|^\($s\):|\1|" \
        -e "s|^\($s\)\($w\)$s:$s[\"']\(.*\)[\"']$s\$|\1$fs\2$fs\3|p" \
        -e "s|^\($s\)\($w\)$s:$s\(.*\)$s\$|\1$fs\2$fs\3|p"  $1 |
   awk -F$fs '{
      indent = length($1)/2;
      vname[indent] = $2;
      for (i in vname) {if (i > indent) {delete vname[i]}}
      if (length($3) > 0) {
         vn=""; for (i=0; i<indent; i++) {vn=(vn)(vname[i])("_")}
         if("status_jobStatus_"==vn  && $2=="state"){
           printf("export %s%s=%s",vn,$2,$3);
         }
      }
   }'
}


/usr/local/bin/kubectl  apply -f $1
/usr/local/bin/kubectl get flinkdeployments $2 -o yaml > new.yaml
res=`eval parseYaml "new.yaml" "k8s_app"`

echo "++++++++++++++++++++++++++++++++++++++++++++"
echo $res
$res
echo "========================================="
echo "$status_jobStatus_state"
echo "------------------------------------------------"

while [[ $status_jobStatus_state != "FINISHED" ]]
do
   /usr/local/bin/kubectl get flinkdeployments $2 -o yaml > new.yaml
    res=`eval parseYaml "new.yaml" "k8s_app"`
    echo $1  $2
    echo "$status_jobStatus_state"
    echo $res
    $res
    sleep 10s
done

/usr/local/bin/kubectl  delete -f $1