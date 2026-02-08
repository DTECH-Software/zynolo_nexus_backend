package com.zynolo_nexus.setting_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "username_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsernamePolicy extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer minUpperCase;
    private Integer minLowerCase;
    private Integer minNumbers;
    private Integer minSpecialCharacters;
    private Integer minLength;
    private Integer maxLength;
}
