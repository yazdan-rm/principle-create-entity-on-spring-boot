package ir.rbp.org.kafka.producer;

import ir.rbp.nabcore.controller.exception.CustomParameterizeException;
import ir.rbp.org.common.constant.ErrorConstants;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@Log4j2
@Service
public class KafkaProducer<T> {

    @Value("${topic.kafka.heartbeat.upstate}")
    private String kafkaHeartbeatUpstate;

    private final KafkaTemplate<String, T> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, T> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(T dto, String topic) {

        ProducerRecord<String, T> producerRecord = new ProducerRecord<>(topic, dto);
        producerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        ListenableFuture<SendResult<String, T>> future = kafkaTemplate.send(producerRecord);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, T> result) {
                log.info("\u001B[32m ********** Sent message=[{}] with topic=[{}] \u001B[0m", dto, result.getRecordMetadata().topic());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("\u001B[31m ********** Unable to send message=[{}] due to : {} \u001B[0m", dto, ex.getMessage());
                throw new CustomParameterizeException(ErrorConstants.ERR_INTERNAL_SERVER);
            }
        });
    }

    public void checkKafkaIsUp(T emptyDto) {
        try {
            sendToKafka(emptyDto, kafkaHeartbeatUpstate);
        } catch (Exception e) {
            throw new CustomParameterizeException(ErrorConstants.ERR_INTERNAL_SERVER);
        }
    }

}
