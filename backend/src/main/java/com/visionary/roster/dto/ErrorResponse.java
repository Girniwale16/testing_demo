package com.visionary.roster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String correlationId;
    private String errorCode;
    private String message;
    private String details;
    private String remediation;
    private LocalDateTime timestamp;
}