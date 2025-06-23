package edu.validation;

import edu.dto.PasswordDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, PasswordDTO> {
    @Override
    public void initialize(PasswordsMatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(PasswordDTO passwordDTO, ConstraintValidatorContext context) {
        if (passwordDTO == null) {
            return true;
        }

        boolean isValid = true;

        if (passwordDTO.getNewPassword() != null && passwordDTO.getConfirmPassword() != null) {
            if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Passwords do not match")
                        .addPropertyNode("confirmPassword")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        if (passwordDTO.getCurrentPassword() != null && passwordDTO.getNewPassword() != null) {
            if (passwordDTO.getCurrentPassword().equals(passwordDTO.getNewPassword())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("New password must be different from current password")
                        .addPropertyNode("newPassword")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}