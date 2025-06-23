package edu.dto;

import edu.validation.PasswordsMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


@AllArgsConstructor
@NoArgsConstructor
@Data
@PasswordsMatch

public class PasswordDTO {
    @NotBlank(message = "Current password is required.")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    private String newPassword;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
