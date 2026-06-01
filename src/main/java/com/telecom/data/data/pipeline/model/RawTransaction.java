package com.telecom.data.data.pipeline.model;

import com.telecom.data.data.pipeline.enums.TransactionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level= AccessLevel.PRIVATE)
@ToString
@Builder
public class RawTransaction {
    Long id;
    String senderAccountId;
    String receiverAccountId;
    String amount;
    String category;
    String description;
    String transactionDate;
    TransactionStatus status;
    String errorMessage;
    LocalDateTime createdAt;
}

