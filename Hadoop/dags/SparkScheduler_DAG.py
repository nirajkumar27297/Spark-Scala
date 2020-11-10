from datetime import datetime, timedelta

from airflow import DAG
from airflow.contrib.operators.spark_submit_operator import SparkSubmitOperator
from airflow.operators.python_operator import PythonOperator


def printMessage():
    print("Hello World !!!!!")

default_args = {
    'owner':'Niraj',
    'start_date':datetime(2020,11,10),
    'retries':0,
    'catchup':False,
    'retry_delay':timedelta(minutes=5)
}

dag = DAG('run_spark_scala_job',
          default_args=default_args,
          schedule_interval='* * * * *')
pythonOperator = PythonOperator(task_id='print_message_task',python_callable=printMessage,dag=dag)

spark_config = {
    'conf': {
        "spark.yarn.maxAppAttempts":"1",
        "spark.yarn.executor.memoryOverhead":"512"
    },
    "conn_id":"spark_local",
    "java_class": "DemoPackage.WordCount",
    "application": "/home/niraj/IdeaProjects/Practice/target/scala-2.12/practice_2.12-0.1.jar",
    "driver_memory": "1g",
    "executor_cores": 1 ,
    "num_executors" : 1,
    "executor_memory":"1g"
}
spark_operator = SparkSubmitOperator(task_id='spark_scala_job_spark_submit',dag = dag,**spark_config)
pythonOperator.set_downstream(spark_operator)

if __name__ == "__main__":
    dag.cli()
