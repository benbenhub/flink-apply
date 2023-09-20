# -*- coding: utf-8 -*-
import yaml # pip3 install pyyaml
import os

def load_yml():
  yaml_file = os.getenv('POD_TEMPLATE_PATH')
  file = open(yaml_file, 'r', encoding="utf-8")
  file_data = file.read()
  file.close()
  data = yaml.load(file_data, yaml.Loader)
  return data

def generate_yaml_doc(yaml_file, yaml_object):
  file = open(yaml_file, 'w', encoding='utf-8')
  yaml.dump(yaml_object, file)
  file.close()

def job_yml_create(dict_yaml_data):
  app_name = os.getenv('APP_NAME')
  app_option = os.getenv('APP_OPTION')
  main_class = os.getenv('MAIN_CLASS')
  jb_mem = os.getenv('JB_MEM')
  jb_cpu = os.getenv('JB_CPU')
  tm_mem = os.getenv('TM_MEM')
  tm_cpu = os.getenv('TM_CPU')
  paralize = os.getenv('PARALIZE')
  slots = os.getenv('SLOTS')
  app_image = os.getenv('APP_IMAGE')
  app_path = os.getenv('APP_PATH')

  dict_yaml_data['metadata']['name'] = app_name
  dict_yaml_data['spec']['image'] = app_image
  dict_yaml_data['spec']['flinkConfiguration']['taskmanager.numberOfTaskSlots'] = slots
  dict_yaml_data['spec']['podTemplate']['metadata']['name'] = app_name
  dict_yaml_data['spec']['jobManager']['resource']['memory'] = jb_mem
  dict_yaml_data['spec']['jobManager']['resource']['cpu'] = float(jb_cpu)
  dict_yaml_data['spec']['taskManager']['resource']['memory'] = tm_mem
  dict_yaml_data['spec']['taskManager']['resource']['cpu'] = float(tm_cpu)
  dict_yaml_data['spec']['job']['jarURI'] = app_path
  dict_yaml_data['spec']['job']['entryClass'] = main_class
  dict_yaml_data['spec']['job']['parallelism'] = int(paralize)
  dict_yaml_data['spec']['job']['args'] = app_option.split(' ')

  # str(os.getenv('RES_PATH')) + '/operatorYaml/' +
  scps_job_file_path = app_name + '.yaml'
  generate_yaml_doc(scps_job_file_path, dict_yaml_data)
  return scps_job_file_path

if __name__ == '__main__':
  f = open(job_yml_create(load_yml()), 'r', encoding="utf-8")
  f_d = f.read()
  print('**************yaml文件内容*******************')
  print(f_d)
  print('**************yaml文件内容*******************')
  f.close()