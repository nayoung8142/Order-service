package com.nayoung.orderservice.kafka.producer;

import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    public void send(String kafkaTopic, OrderDto value) {
        sendMessage(kafkaTopic, null, value);
    }

    public void send(String kafkaTopic, String key, OrderDto value) {
        sendMessage(kafkaTopic, key, value);
    }

    private void sendMessage(String topic, String key, @Payload(required = false) OrderDto value) {
        kafkaTemplate.send(topic, key, value)
                .whenComplete((stringOrderDtoSendResult, throwable) -> {
                    if(throwable == null) {
                        RecordMetadata metadata = stringOrderDtoSendResult.getRecordMetadata();
                        if(stringOrderDtoSendResult.getProducerRecord().value() == null) {
                            log.info("Tombstone Record -> topic: {}, partition: {}, event Id: {}",
                                    metadata.topic(),
                                    metadata.partition(),
                                    stringOrderDtoSendResult.getProducerRecord().key());
                        }
                        else {
                            log.info("Producing message Success -> topic: {}, partition: {}, offset: {}, event Id: {}",
                                    metadata.topic(),
                                    metadata.partition(),
                                    metadata.offset(),
                                    stringOrderDtoSendResult.getProducerRecord().key());
                        }
                    } else {
                        log.error("Producing message Failure -> " + throwable.getMessage());
                    }
                });
    }
}