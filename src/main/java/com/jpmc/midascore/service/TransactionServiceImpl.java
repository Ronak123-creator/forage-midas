package com.jpmc.midascore.service;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService{

    private final TransactionRecordRepository transactionRecordRepository;
    private final UserRepository userRepository;
    private final DatabaseConduit databaseConduit;


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

        // NOW SAVE TRANSACTION — links to refreshed entities
        transactionRecordRepository.save(new TransactionRecord(sender, recipient, amount));

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
