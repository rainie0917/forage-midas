package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRecordRepository;
import com.jpmc.midascore.service.IncentiveService;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KafkaTransactionListener {

    private final UserRecordRepository userRecordRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final IncentiveService incentiveService;

    public KafkaTransactionListener(UserRecordRepository userRecordRepository,
                                    TransactionRecordRepository transactionRecordRepository,
                                    IncentiveService incentiveService) {
        this.userRecordRepository = userRecordRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.incentiveService = incentiveService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    @Transactional
    public void listen(Transaction transaction) {
        UserRecord sender = userRecordRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRecordRepository.findById(transaction.getRecipientId()).orElse(null);

        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {
            // Deduct the transaction amount from the sender's balance
            sender.setBalance(sender.getBalance() - transaction.getAmount());

            // Call the Incentive API to get the incentive amount
            float incentiveAmount = incentiveService.getIncentiveAmount(transaction);

            // Add the transaction amount and incentive to the recipient's balance
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

            // Save updated users
            userRecordRepository.save(sender);
            userRecordRepository.save(recipient);

            // Record the transaction
            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setAmount(transaction.getAmount());
            transactionRecord.setIncentive(incentiveAmount); // Save the incentive amount
            transactionRecord.setSender(sender);
            transactionRecord.setRecipient(recipient);
            transactionRecordRepository.save(transactionRecord);
        }

        // Debug: Query the "waldorf" user
        // UserRecord waldorf = userRecordRepository.findByName("waldorf");
        // if (waldorf != null) {
        // System.out.println("Waldorf's balance: " + waldorf.getBalance());
        // }

        // Debug
        // UserRecord wilbur = userRecordRepository.findByName("wilbur");
        // System.out.println("Wilbur's balance: " + wilbur.getBalance());
    }
    
}