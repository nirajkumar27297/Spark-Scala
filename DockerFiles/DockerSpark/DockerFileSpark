FROM bitnami/spark:3.0.0
USER root

RUN apt-get update && apt install gnupg -y && \ 
	echo "deb https://dl.bintray.com/sbt/debian /" |  tee -a /etc/apt/sources.list.d/sbt.list && \
 	apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823 && \
 	apt-get update && \
 	apt-get install sbt -y && \
 	apt-get install python3 -y \
        && pip3 install --upgrade pip
 	


WORKDIR /app

COPY StockPredictionStructuredStreaming /app/StockPredictionStructuredStreaming
COPY KafkaProducer   /app/KafkaProducer
COPY commons-pool2-2.8.0.jar /app/jars/commons-pool2-2.8.0.jar
COPY kafka-clients-2.6.0.jar /app/jars/kafka-clients-2.6.0.jar
COPY spark-sql-kafka-0-10_2.12-3.0.0.jar /app/jars/spark-sql-kafka-0-10_2.12-3.0.0.jar
COPY spark-streaming-kafka-0-10_2.12-3.0.0.jar /app/jars/spark-streaming-kafka-0-10_2.12-3.0.0.jar
COPY spark-token-provider-kafka-0-10_2.12-3.0.0.jar /app/jars/spark-token-provider-kafka-0-10_2.12-3.0.0.jar 
COPY spray-json_2.12-1.3.5.jar /app/jars/spray-json_2.12-1.3.5.jar
COPY requirements.txt .
COPY DockerScript.sh .


RUN pip3 install -r requirements.txt








 
