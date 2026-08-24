package com.visionary.roster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Long userId;
    private String username;
    private String role;
    private Long facilityId;
    private String facilityName;
    private Boolean isActive;
}