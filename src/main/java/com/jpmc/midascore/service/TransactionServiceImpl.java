package com.jpmc.midascore.service;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService{

    private final TransactionRecordRepository transactionRecordRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final String incentiveUrl;

    public TransactionServiceImpl(TransactionRecordRepository transactionRecordRepository,
                                  UserRepository userRepository, RestTemplate restTemplate,
                                  @Value("${incentive.api.url:http://localhost:8080/incentive}")
                                  String incentiveUrl) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.incentiveUrl = incentiveUrl;
    }

    @Override
    @Transactional
    public void validateAndApply(Long senderId, Long recipientId, float amount) {

        Optional<UserRecord> senderOption = userRepository.findById(senderId);
        Optional<UserRecord> recipientOption = userRepository.findById(recipientId);

        if(senderOption.isEmpty() || recipientOption.isEmpty()){
            return;
        }
        UserRecord sender = senderOption.get();
        UserRecord recipient = recipientOption.get();

        if(sender.getBalance() < amount){
            return;
        }

        float incentive = 0f;
        try {
            Incentive res = restTemplate.postForObject(
                    incentiveUrl,
                    new Transaction(senderId,recipientId,amount),
                    Incentive.class
            );
            if(res != null && res.getAmount() >=0){
                incentive = res.getAmount();
            }
        }catch (Exception e){
            incentive = 0f;
        }

        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentive);

        transactionRecordRepository.save(
                new TransactionRecord(
                        sender,
                        recipient,
                        amount,
                        incentive
                )
        );
        transactionRecordRepository.save(new TransactionRecord(sender, recipient, amount, incentive));
        userRepository.save(sender);
        userRepository.save(recipient);

    }

    @Override
    @Transactional
    public void validateAndApplyByNames(String senderName, String recipientName, float amount) {
        String s = senderName == null?null: senderName.trim();
        String r = recipientName==null?null: recipientName.trim();

        if(s == null || r == null || s.isBlank() || r.isEmpty()){
            return;
        }
        Optional<UserRecord> senderOpt = userRepository.findByName(s);
        Optional<UserRecord> recipientOpt = userRepository.findByName(r);

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()){ return;}

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        if (sender.getBalance() < amount){
            return;
        }
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        transactionRecordRepository.save(
                new TransactionRecord(
                        sender,
                        recipient,
                        amount
                )
        );
        userRepository.save(sender);
        userRepository.save(recipient);

    }
}
