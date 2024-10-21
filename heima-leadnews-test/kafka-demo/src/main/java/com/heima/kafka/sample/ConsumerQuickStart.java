package com.heima.kafka.sample;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class ConsumerQuickStart {

    public static void main(String[] args) {
        Properties prop = new Properties();
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.20.10.3:9092");
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(prop);

        kafkaConsumer.subscribe(Collections.singletonList("topic-first"));

        while(true) {
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                System.out.println(consumerRecord.key());
                System.out.println(consumerRecord.value());
                System.out.println(consumerRecord.offset());

//                try{
//                    kafkaConsumer.commitSync();
//                }catch(CommitFailedException e) {
//                    System.out.println("同步提交失败的异常" + e);
//                }

//                kafkaConsumer.commitAsync(new OffsetCommitCallback() {
//                    @Override
//                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
//                        if(e != null) {
//                            System.out.println("异步提交错误的偏移量" + map + ", 异常信息" + e);
//                        }
//                    }
//                });

                try{
                    kafkaConsumer.commitAsync(new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                            if(e != null) {
                                System.out.println("异步提交错误的偏移量" + map + ", 异常信息" + e);
                            }
                        }
                    });
                }catch(CommitFailedException e) {
                    System.out.println("记录错误信息" + e);
                }finally {
                    kafkaConsumer.commitSync();
                }
            }
        }
    }
}
