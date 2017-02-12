package com.github.ddth.kafka.internal;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.github.ddth.kafka.KafkaClient.ProducerType;
import com.github.ddth.kafka.KafkaTopicPartitionOffset;

/**
 * Helper utility class.
 * 
 * @author Thanh Ba Nguyen <bnguyen2k@gmail.com>
 * @since 1.2.0
 */
public class KafkaHelper {

    /**
     * Seeks to a specified offset.
     * 
     * @param consumer
     * @param tpo
     * @return {@code true} if the consumer has subscribed to the specified
     *         topic/partition, {@code false} otherwise.
     * @since 1.3.2
     */
    public static boolean seek(KafkaConsumer<?, ?> consumer, KafkaTopicPartitionOffset tpo) {
        synchronized (consumer) {
            Set<TopicPartition> topicParts = consumer.assignment();
            if (topicParts != null) {
                for (TopicPartition tp : topicParts) {
                    if (StringUtils.equals(tpo.topic, tp.topic())
                            && tpo.partition == tp.partition()) {
                        consumer.seek(tp, tpo.offset);
                        // we want to seek as soon as possible
                        // since seekToEnd evaluates lazily, invoke position()
                        // so
                        // that seeking will be committed.
                        consumer.position(tp);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Seeks the consumer's cursor to the beginning of a topic.
     * 
     * <p>
     * This method only set cursors of topic's partitions that are assigned to
     * the consumer!
     * </p>
     * 
     * @param consumer
     * @param topic
     * @return {@code true} if the consumer has subscribed to the specified
     *         topic, {@code false} otherwise.
     */
    public static boolean seekToBeginning(KafkaConsumer<?, ?> consumer, String topic) {
        boolean result = false;
        synchronized (consumer) {
            Set<TopicPartition> topicParts = consumer.assignment();
            if (topicParts != null) {
                for (TopicPartition tp : topicParts) {
                    if (StringUtils.equals(topic, tp.topic())) {
                        consumer.seekToBeginning(Arrays.asList(tp));
                        // we want to seek as soon as possible
                        // since seekToEnd evaluates lazily, invoke position()
                        // so
                        // that seeking will be committed.
                        consumer.position(tp);
                        result = true;
                    }
                }
                if (result) {
                    consumer.commitSync();
                }
            }
        }
        return result;
    }

    /**
     * Seeks the consumer's cursor to the end of a topic.
     * 
     * <p>
     * This method only set cursors of topic's partitions that are assigned to
     * the consumer!
     * </p>
     * 
     * @param consumer
     * @param topic
     * @return {@code true} if the consumer has subscribed to the specified
     *         topic, {@code false} otherwise.
     */
    public static boolean seekToEnd(KafkaConsumer<?, ?> consumer, String topic) {
        boolean result = false;
        synchronized (consumer) {
            Set<TopicPartition> topicParts = consumer.assignment();
            if (topicParts != null) {
                for (TopicPartition tp : topicParts) {
                    if (StringUtils.equals(topic, tp.topic())) {
                        consumer.seekToEnd(Arrays.asList(tp));
                        // we want to seek as soon as possible
                        // since seekToEnd evaluates lazily, invoke position()
                        // so
                        // that seeking will be committed.
                        consumer.position(tp);
                        result = true;
                    }
                }
                if (result) {
                    consumer.commitSync();
                }
            }
        }
        return result;
    }

    /**
     * Creates a new {@link KafkaProducer} instance, with default
     * configurations.
     * 
     * @param type
     * @param bootstrapServers
     * @return
     */
    public static KafkaProducer<String, byte[]> createKafkaProducer(ProducerType type,
            String bootstrapServers) {
        return createKafkaProducer(type, bootstrapServers, null);
    }

    /**
     * Creates a new {@link KafkaProducer} instance, with custom configuration
     * properties.
     * 
     * <p>
     * Note: custom configuration properties will be populated <i>after</i> and
     * <i>additional/overridden</i> to the default configuration.
     * </p>
     * 
     * @param type
     * @param bootstrapServers
     * @param customProps
     * @return
     * @since 1.2.1
     */
    public static KafkaProducer<String, byte[]> createKafkaProducer(ProducerType type,
            String bootstrapServers, Properties customProps) {
        Properties props = buildKafkaProducerProps(type, bootstrapServers, customProps);
        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(props);
        return producer;
    }

    /**
     * Builds default producer's properties.
     * 
     * @param type
     * @param bootstrapServers
     * @return
     * @since 1.3.2
     */
    public static Properties buildKafkaProducerProps(ProducerType type, String bootstrapServers) {
        return buildKafkaProducerProps(type, bootstrapServers, null);
    }

    /**
     * Builds default consumer's properties, and applies custom configurations
     * if any.
     * 
     * <p>
     * Note: custom configuration properties will be populated <i>after</i> and
     * <i>additional/overridden</i> to the default configuration.
     * </p>
     * 
     * @param type
     * @param bootstrapServers
     * @param customProps
     * @return
     * @since 1.3.2
     */
    public static Properties buildKafkaProducerProps(ProducerType type, String bootstrapServers,
            Properties customProps) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                ByteArraySerializer.class.getName());

        // max request size: 128kb, it's also max message size
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, String.valueOf(128 * 1024));

        // 60mb buffer & 256-record batch size
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, String.valueOf(60 * 1024 * 1024));
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, String.valueOf(256));

        // KafkaProducer.send() / KafkaProducer.partitionsFor() max block: 60s
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, String.valueOf(60000));

        // props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        props.put(ProducerConfig.LINGER_MS_CONFIG, String.valueOf(10));

        // ack timeout 30 seconds
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(30000));

        // metadata fetch timeout: 10 seconds
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, String.valueOf(10000));

        props.put(ProducerConfig.RETRIES_CONFIG, String.valueOf(3));
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, String.valueOf(1000));

        switch (type) {
        // case FULL_ASYNC: {
        // props.put(ProducerConfig.ACKS_CONFIG, "0");
        // props.put("producer.type", "async");
        // break;
        // }
        case LEADER_ACK: {
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            // props.put("producer.type", "sync");
            break;
        }
        case ALL_ACKS: {
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            // props.put("producer.type", "sync");
            break;
        }
        case NO_ACK:
        default: {
            props.put(ProducerConfig.ACKS_CONFIG, "0");
            // props.put("producer.type", "sync");
            break;
        }
        }

        if (customProps != null) {
            // populate custom configurations
            props.putAll(customProps);
        }

        return props;
    }

    /**
     * Creates a new {@link KafkaConsumer} instance, with default
     * configurations.
     * 
     * @param bootstrapServers
     * @param consumerGroupId
     * @param consumeFromBeginning
     * @param autoCommitOffsets
     * @return
     */
    public static KafkaConsumer<String, byte[]> createKafkaConsumer(String bootstrapServers,
            String consumerGroupId, boolean consumeFromBeginning, boolean autoCommitOffsets) {
        return createKafkaConsumer(bootstrapServers, consumerGroupId, consumeFromBeginning,
                autoCommitOffsets, null);
    }

    /**
     * Creates a new {@link KafkaConsumer} instance, with custom configuration
     * properties.
     * 
     * <p>
     * Note: custom configuration properties will be populated <i>after</i> and
     * <i>additional/overridden</i> to the default configuration.
     * </p>
     * 
     * @param bootstrapServers
     * @param consumerGroupId
     * @param consumeFromBeginning
     * @param autoCommitOffsets
     * @param customProps
     * @return
     * @since 1.2.1
     */
    public static KafkaConsumer<String, byte[]> createKafkaConsumer(String bootstrapServers,
            String consumerGroupId, boolean consumeFromBeginning, boolean autoCommitOffsets,
            Properties customProps) {
        Properties props = buildKafkaConsumerProps(bootstrapServers, consumerGroupId,
                consumeFromBeginning, autoCommitOffsets, customProps);
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(props);
        return consumer;
    }

    /**
     * Builds default consumer's properties.
     * 
     * @param bootstrapServers
     * @param consumerGroupId
     * @param consumeFromBeginning
     * @param autoCommitOffsets
     * @return
     */
    public static Properties buildKafkaConsumerProps(String bootstrapServers,
            String consumerGroupId, boolean consumeFromBeginning, boolean autoCommitOffsets) {
        return buildKafkaConsumerProps(bootstrapServers, consumerGroupId, consumeFromBeginning,
                autoCommitOffsets, null);
    }

    /**
     * Builds default consumer's properties, and applies custom configurations
     * if any.
     * 
     * <p>
     * Note: custom configuration properties will be populated <i>after</i> and
     * <i>additional/overridden</i> to the default configuration.
     * </p>
     * 
     * @param bootstrapServers
     * @param consumerGroupId
     * @param consumeFromBeginning
     * @param autoCommitOffsets
     * @param customProps
     * @since 1.2.1
     * @return
     */
    public static Properties buildKafkaConsumerProps(String bootstrapServers,
            String consumerGroupId, boolean consumeFromBeginning, boolean autoCommitOffsets,
            Properties customProps) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerGroupId);

        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, String.valueOf(1000));

        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(6000));
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, String.valueOf(2000));

        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(60000));
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(61000));

        if (autoCommitOffsets) {
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(1000));
        } else {
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        }

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                consumeFromBeginning ? "earliest" : "latest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ByteArrayDeserializer.class.getName());

        // if (leaderAutoRebalance) {
        // props.put("auto.leader.rebalance.enable", true);
        // props.put("rebalance.backoff.ms", String.valueOf(10000));
        // props.put("refresh.leader.backoff.ms", String.valueOf(1000));
        // } else {
        // props.put("auto.leader.rebalance.enable", false);
        // }

        // props.put("controlled.shutdown.enable", true);
        // props.put("controlled.shutdown.max.retries", String.valueOf(3));
        // props.put("controlled.shutdown.retry.backoff.ms",
        // String.valueOf(3000));

        // max 256kb
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, String.valueOf(256 * 1024));

        if (customProps != null) {
            // populate custom configurations
            props.putAll(customProps);
        }

        return props;
    }
}
