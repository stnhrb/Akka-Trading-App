
1. Download Zookeeper and kafka 
2. Copy kafka config to Zookeeper config folder in order to use it \
`cp ../../kafka/kafka_2.13-3.4.0/config/zookeeper.properties ./conf/`
3. Start Zookeeper server \
`./bin/zkServer.sh start`
4. Connecting to Zookeeper cli \
`./bin/zkCli.sh -server 127.0.0.1:2181`
5. Start Kafka server \
`./bin/kafka-server-start.sh config/server.properties`
6. Create Kafka topic \
   `./bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic market \
   --partitions 1 --replication-factor 1 \
   --config cleanup.policy=compact \
   --config min.cleanable.dirty.ratio=0.001 \
   --config segment.ms=100`