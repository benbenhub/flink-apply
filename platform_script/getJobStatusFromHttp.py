import json
import time

import requests
import getopt
import json
import os
import sys


def helper():
    print("usage: python getJobStatus.py -f <filename>")


def init_arg(argv):
    try:
        options, args = getopt.getopt(argv, "h:f:", ["help", "file"])
    except getopt.GetoptError:
        helper()
        sys.exit(1)

    for option, value in options:
        if option in ("-h", "--help"):
            helper()
        if option in ("-f", "--file"):
            return value


def get_task_from_json_file(file):
    file_exists = os.path.isfile(file)
    if file_exists:
        with open(file) as jsonFile:
            job = json.load(jsonFile)
        return job
    else:
        print(str(file) + "  Not exists!!!!!!!!")
        sys.exit(1)


def parse_jobs_from_task(task_json):
    job_paths = task_json['archive']
    jobs_json = ''

    for path in job_paths:
        if '/jobs/overview' == path['path']:
            jobs_json = path['json']

    if "" != jobs_json:
        jobs = json.loads(jobs_json)['jobs']
        job = jobs[0]
        return job['state']


def get_task_from_http(url, job_name):
    res = requests.get(url)
    jobs = json.loads(res.text)["jobs"]

    for job in jobs:
        if job["name"] == job_name:
            print(job)
            return job['state']

    print(str(job_name) + "  Not exists! next check in 10s after")
    time.sleep(10) 
    return get_task_from_http(url,job_name)


if __name__ == '__main__':
    url = "http://10.0.1.12:59696/jobs/overview"
    jobName = init_arg(sys.argv[1:])
    job_state = get_task_from_http(url, jobName)
    # job_state = parse_jobs_from_task(task)

    if "FINISHED" == job_state:
        print("Job is run Successful !!!!")
    else:
        print("Job is run Failed !!!!")
        sys.exit(1)