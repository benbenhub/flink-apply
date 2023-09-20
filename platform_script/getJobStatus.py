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


if __name__ == '__main__':
    filePath = init_arg(sys.argv[1:])
    task = get_task_from_json_file(filePath)
    job_state = parse_jobs_from_task(task)

    if "FINISHED" == job_state:
        print("Job is run Successful !!!!")
    else:
        print("Job is run Failed !!!!")
        sys.exit(1)
