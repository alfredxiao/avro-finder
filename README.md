# Introduction

`kfind` is a tool (essentially a kafka consumer) that outputs records from a topic, it is similar to `kafka-avro-console-consumer`, or `kafkactl consume`, but it has special support of some features useful for developers in real world scenarios. E.g. print logical types like Decimals. Some of these features are not available in those alternative tools.

# Features
- **Logical Types support**
  - Supports logical types like decimals, timestamps.
  - No need for extra parameter passed to command line
- **Grep**
  - Filter records by grepping string such that only records with found values are printed
  - Supports a 'grep-limit', which sets upper bound of hits. So for example, with parameter `-gl 1` the tool stops when first record where grep string is found.
- **From Timestamp**, and **From offset**
  - Consume from specified offset, or timestamp (epoch)
- **Select certain fields**
  - Only prints certain fields, instead of full record payload
- **Backward Duration**
  - Consume only records of last period, e.g. last hour, last few days.
  - E.g. PT1H for last hour, P2D for last two days
- **Conditions to Stop**
  - When reaching end of topic
  - When reaching total count (of visited records)
  - When reaching total grep count

# Usage

| Parameter | Required | Description                                                                             |
|-----------|----------|-----------------------------------------------------------------------------------------|
| -b        | No       | bootstrap server, note#1                                                                |
| -t        | Yes      | topic                                                                                   |
| -s        | No       | schema registry url, note#2                                                             |
| -k        | No       | key deserializer, defaults to `StringDeserializer`                                      |
| -v        | No       | value deserializer, defaults to `StringDeserializer`                                    |
| -p        | No       | partition number, defaults to all partitions                                            |
| -o        | No       | from offset, 'beginning' (default) means from beginning，'end' means from end            |
| -f        | No       | from epoch timestamp                                                                    |
| -bd       | No       | goes back specified duration, e.g. `PT1H` for 1 hour, `P2D` for 2 days                  |
| -g        | No       | grep certain string, e.g. `-g my-uuid`                                                  |
| -gl       | No       | grep limit, when specified, consumer stops when count of hits is met                    |
| -l        | No       | total limit, when specified, consumer stops when count of records visited is met        |
| -c        | No       | read consumer config from specified file, which contain for example ssl connection info |
| -e        | No       | exit the consumer when topic end is reached                                             |

Notes:
1. if not specified in command line, can set `bootstrap.servers` in consumer config file.
   if not available still, defaults to localhost:9092
2. if not specified in command line, can set `schema.registry.url` in consumer config file.
   if not available still, defaults to localhost:8081

# Build
(with JDK17)
`./gradlew shadowJar`

# Run
`java -jar build/libs/kfind-0.0.1-SNAPSHOT-all.jar -c config/client.props -t TOPIC_NAME -k io.confluent.kafka.serializers.KafkaAvroDeserializer -v io.confluent.kafka.serializers.KafkaAvroDeserializer -e -gl 10 -g STRING_TO_GREP`


