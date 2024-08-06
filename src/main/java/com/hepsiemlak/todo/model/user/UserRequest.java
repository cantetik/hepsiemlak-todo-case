package com.hepsiemlak.todo.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "{errors.validation.not.blank}")
    @Email(message = "{errors.validation.email}")
    private String username;

    @NotBlank(message = "{errors.validation.not.blank}")
    private String password;

    @Override
    public String toString() {
        return "UserRequest{" +
                "username='" + username + '\'' +
                ", password= ***masked***'" + '\'' +
                '}';
    }
}
