package com.zynolo_nexus.auth_service.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDetails {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastModifiedDate;

    private String createdBy;
    private String lastModifiedBy;

    private Long id;
    private String username;
    private String newUsername;

    private String email;
    private String mobile;

    private String status;
    private String statusDescription;

    private String loginStatus;
    private String loginStatusDescription;

    private UserRoleDto userRole;

    private String firstName;
    private String lastName;
    private String nic;
    private String company;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastLoggedDate;

    private Boolean expectingFirstTimeLogging;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate passwordExpiredDate;

    private String proImg;
    private Boolean reset;
}
