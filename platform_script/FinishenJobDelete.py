import json
import requests
import os

def get_task_job_delete(url,path):
    res = requests.get(url)
    jobs = json.loads(res.text)["jobs"]

    for job in jobs:
        if job["state"] == "FINISHED":
            os.remove(path + "/" + job["jid"])


if __name__ == '__main__':
    url = "http://10.0.1.12:59696/jobs/overview"
    get_task_job_delete(url,"/opt/appShare/flink-history")