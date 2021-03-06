ddth-kafka release notes
========================

1.3.3 - 2017-02-12
------------------

- Fixes & changes:
  - Now there are only 3 producer types: `NO_ACK`, `LEADER_ACK` and `ALL_ACKS` as the new Java Producer
    is async by default.
  - Remove options/properties when building consumer: `auto.leader.rebalance.enable`, `rebalance.backoff.ms`, `refresh.leader.backoff.ms`,
    `controlled.shutdown.enable`, `controlled.shutdown.max.retries`, `controlled.shutdown.retry.backoff.ms`


1.3.2 - 2017-02-09
------------------

- Bug fixed: `KafkaConsumer is not safe for multi-threaded access`.
- `KafkaClient`'s new methods:
  - `commit(...)`: commit offsets.
  - `commitAsync(...)`: commit offsets asynchronously.
  - `seek(...)`: seek to a specific offset.


1.3.1.1 - 2017-02-08
--------------------

- Bug fixed & Enhancements.
- `KafkaClient`'s new methods:
  - `void flush()`: flushes any messages in producer queue.
  - `Future<KafkaMessage> sendMessageAsync(...)`: sends a message asynchronously.
  - `Future<RecordMetadata> sendMessageRaw(...)`: sends a message asynchronously.
  - [BREAKING CHANGE] `KafkaMessage sendMessage(...)` is now synchronously.


1.3.0 - 2017-01-11
------------------

- Bump to `com.github.ddth:ddth-parent:6`, now requires Java 8+!
- Bump to `kafka-clients:0.10.1.1`.


1.2.1.1 - 2016-05-06
--------------------

- Limit the executor service's number of threads to (1-4) to avoid OOM exception.


1.2.1 - 2016-04-29
------------------

- Accept custom `ProducerConfig`/`ConsumerConfig` when creating producers and consumers.


1.2.0 - 2016-03-21
------------------

- Upgrade to v0.9.0.1 of Kafka Java producer and consumer:
  - It no longer works with Kafka broker pre-0.9, [upgrade your Kafka broker cluster to 0.9.x](http://kafka.apache.org/documentation.html#upgrade).
  - Pure Java producer and consumer.
  - Remove Zookeeper dependency.
- New methods `KafkaClient.seekToBeginning(String consumerGroupId, String topic)` and `KafkaClient.seekToEnd(String consumerGroupId, String topic)`.
- New methods `KafakClient.consumeMessage(String, String)` and `KafkaClient.consumeMessage(String, String, long, TimeUnit)`.


1.1.3 - 2016-01-08
------------------

- `KafkaClient` accepts custom thread pool (pull request #2: https://github.com/DDTH/ddth-kafka/pull/2 ).
- Improvement to pull request #2: donot shutdown the `ExecutorService` if it was not created by the `KafkaClient`.


1.1.2 - 2015-09-30
------------------

- Fine tune `auto.leader.rebalance.enable` (now controllable within `KafkaConsumer` class).
- Upgrade to `org.apache.kafka:kafka_2.10:0.8.2.2`


1.1.1 - 2015-09-24
------------------

- Turn off `auto.leader.rebalance.enable`.
- Manual commit offset when consuming messages in manual mode (non-listener mode).
- Bugs fixed & enhancements: two clients with same consumer-group-id receive duplicate messages.


1.1.0 - 2015-09-07
------------------

- Upgrade to Apache Kafka client 2.8.2, producer now switches from Scala to Java client.
- Deprecate class `RandomPartitioner`, use the default partitioner provided by the java producer: 
  - Message with `null` or empty key will be put into a random partition.
  - Message with non-null key will be put into a partition based on it's key's hash value.
- Method `sendMessage(KafkaMessage)` now returns a Future<KafkaMessage>; method `sendMessages(...)` is removed.


1.0.3 - 2014-10-10
------------------

- Bug fixed & change: when message's key is empty use empty string (`""`) instead to make sure custom partitioner works.


1.0.2 - 2014-10-03
------------------

- Bug fixed.


1.0.1 - 2014-10-02
------------------

- `KafkaClient`: force to use a random partitioner (class `RandomPartitioner`) by default.


1.0.0 - 2014-08-23
------------------

- `v1.0.0` released!
- Breaking API changes: new class `KafkaClient` replaces both `KafkaConsumer` and `KafkaProducer`.
- New class `KafkaMessage`, used by both sending and receiving.


0.2.1.1 - 2014-07-29
--------------------

- When there are 2 or more `IKafkaMessageListener`s on same topic, each `IKafkaMessageListener.onMessage` is handled by a separated thread. This would boost the consumer's performance a bit. 


0.2.1 - 2014-04-03
------------------

- `auto.offset.reset` set to `largest` by default. Add option to consume messages from beginning. (See more: [http://kafka.apache.org/08/configuration.html](http://kafka.apache.org/08/configuration.html).


0.2.0 - 2014-03-19
------------------

- Merged with [ddth-osgikafka](https://github.com/DDTH/ddth-osgikafka) and packaged as OSGi bundle.


0.1.0 - 2014-02-21
------------------

- First release.
