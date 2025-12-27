package com.zynolo_nexus.auth_service.dto.search;

import lombok.Data;

@Data
public class UserSearchCriteria {
    private String username;
    private String email;
    private String status;
}
