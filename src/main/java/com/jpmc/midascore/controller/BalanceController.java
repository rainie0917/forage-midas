package com.jpmc.midascore.controller;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

    private final UserRecordRepository userRecordRepository;

    public BalanceController(UserRecordRepository userRecordRepository) {
        this.userRecordRepository = userRecordRepository;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam long userId) {
        float balance = userRecordRepository.findById(userId)
                .map(user -> user.getBalance())
                .orElse(0.0f);
        return new Balance(balance);
    }
}