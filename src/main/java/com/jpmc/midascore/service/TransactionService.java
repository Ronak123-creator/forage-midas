package com.jpmc.midascore.service;

public interface TransactionService {

    void validateAndApply(Long senderId, Long recipientId, float amount);
    void validateAndApplyByNames(String senderName, String recipientName, float amount);
}
