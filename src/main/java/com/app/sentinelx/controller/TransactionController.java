package com.app.sentinelx.controller;

import com.app.sentinelx.dto.TransactionAssessmentResponse;
import com.app.sentinelx.dto.TransactionRequest;
import com.app.sentinelx.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionAssessmentResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {

        TransactionAssessmentResponse response =
                transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}