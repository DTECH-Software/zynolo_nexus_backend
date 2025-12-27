package com.zynolo_nexus.auth_service.validator.validators;

import com.zynolo_nexus.auth_service.validator.annotations.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        boolean hasUpper = value.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = value.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = value.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        return value.length() >= 8 && hasUpper && hasLower && hasDigit && hasSymbol;
    }
}
