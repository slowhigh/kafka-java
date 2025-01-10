package com.slowhigh.kafka;

import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@RequestMapping("/kafka")
@RestController
public class KafkaController {
    private final Producer<String, String> producer;

    public KafkaController(Producer<String, String> producer) {
        this.producer = producer;
    }

    @GetMapping("/")
    public String init() {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>("test", "init");
            producer.send(record).get(5, TimeUnit.SECONDS);

            return "Send Message Success";
        } catch (Exception e) {
            System.out.println(e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/{value}")
    public String produceMessage(@PathVariable String value) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>("test", value);
            producer.send(record).get(5, TimeUnit.SECONDS);

            for (int i = 0; i < 1000000; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        Message message = new Message(value + index);
                        String jsonMessage = new ObjectMapper().writeValueAsString(message);
                        ProducerRecord<String, String> threadRecord = new ProducerRecord<>("test", jsonMessage);
                        producer.send(threadRecord).get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }).start();
            }

            return "Send Message Success";
        } catch (Exception e) {
            System.out.println(e);
            return "Error: " + e.getMessage();
        }
    }
}