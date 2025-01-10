package com.slowhigh.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class KafkaConfig {
    private final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public Producer<String, String> kafkaProducer() {
        logger.info("Kafka Producer Init Start");

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.0.1:30921,192.168.0.1:30922,192.168.0.1:30923");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("client.id", "test-producer");
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("request.timeout.ms", "5000");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"user1\" " +
                        "password=\"6LNzbkJVMA\";");

        Producer<String, String> producer = new KafkaProducer<>(props);
        logger.info("Kafka Producer Init End");

        return producer;
    }

    @Bean
    public Consumer<String, String> kafkaConsumer() {
        logger.info("Kafka Consumer Init Start");

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.0.1:30921,192.168.0.1:30922,192.168.0.1:30923");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("client.id", "test-consumer");
        props.put("group.id", "test-group");
        props.put("max.poll.records", 100);
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("request.timeout.ms", "5000");
        props.put("sasl.mechanism", "PLAIN");
        props.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"user1\" " +
                        "password=\"6LNzbkJVMA\";");

        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("test"));
        new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1));

                    for (ConsumerRecord<String, String> record : records) {
                        Message message;
                        try {
                            message = new ObjectMapper().readValue(record.value(), Message.class);
                        } catch (JsonProcessingException e) {
                            logger.error("JSON Parsing Error: " + e.getMessage());
                            continue;
                        }

                        System.out.printf("Received message: %s%n", message.getValue());
                    }
                }
            } catch (Exception e) {
                logger.info("Consumer thread interrupted");
            } finally {
                consumer.close();
            }
        }).start();
        logger.info("Kafka Consumer Init End");

        return consumer;
    }
}
