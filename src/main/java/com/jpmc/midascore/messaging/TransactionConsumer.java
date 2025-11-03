package com.jpmc.midascore.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {
    private final TransactionService service;
    private final ObjectMapper om = new ObjectMapper();

    public TransactionConsumer(TransactionService service){
        this.service = service;
    }

    @KafkaListener(
            topics = "${app.kafka.transactions-topic}",
            groupId = "midas-core"
    )
//    public void onMessage(Transaction transaction){
//        Long senderId = transaction.getSenderId();
//        Long recipientId = transaction.getRecipientId();
//        float amount = transaction.getAmount();
//
//        transactionService.validateAndApply(senderId,recipientId,amount);
//    }

    @KafkaListener(
            topics = "${app.kafka.transactions-topic}",
            groupId = "midas-core"
    )
    public void onMessage(String json) throws Exception {
        Transaction tx = om.readValue(json, Transaction.class);
        service.validateAndApply(tx.getSenderId(), tx.getRecipientId(), tx.getAmount());
    }

}
