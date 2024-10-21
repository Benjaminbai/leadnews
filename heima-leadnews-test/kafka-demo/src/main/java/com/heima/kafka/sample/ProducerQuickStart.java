package com.heima.kafka.sample;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerQuickStart {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties prop = new Properties();
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.20.10.3:9092");
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        prop.put(ProducerConfig.ACKS_CONFIG, "all");
        prop.put(ProducerConfig.RETRIES_CONFIG, 10);
        prop.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        KafkaProducer<String, String> kafkaProducer= new KafkaProducer<String, String>(prop);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>("topic-first", "key-001", "hello kafka");

//        RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
//        System.out.println(recordMetadata.offset());

        kafkaProducer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if(e != null) {
                    System.out.println("记录异常信息到日志表中");
                }
                System.out.println(recordMetadata.offset());
            }
        });
        kafkaProducer.close();
    }
}
