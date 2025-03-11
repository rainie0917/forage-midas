package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {

    private final RestTemplate restTemplate;

    public IncentiveService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public float getIncentiveAmount(Transaction transaction) {
        String url = "http://localhost:8080/incentive";
        Incentive incentive = restTemplate.postForObject(url, transaction, Incentive.class);
        return incentive != null ? incentive.getAmount() : 0;
    }
}