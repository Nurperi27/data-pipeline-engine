package com.telecom.data.data.pipeline.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class CleanTransaction {
    Long id;
    Long senderAccountId;
    Long receiverAccountId;
    BigDecimal amount;
    String category;
    String description;
    LocalDateTime transactionDate;
    LocalDateTime createdAt;
}
