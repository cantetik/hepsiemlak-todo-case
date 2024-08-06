package com.hepsiemlak.todo.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "{errors.validation.not.blank}")
    private String accessToken;

    @NotBlank(message = "{errors.validation.not.blank}")
    private String refreshToken;
}
