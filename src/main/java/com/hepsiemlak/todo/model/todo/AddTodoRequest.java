package com.hepsiemlak.todo.model.todo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTodoRequest {

    @NotBlank(message = "{errors.validation.not.blank}")
    private String title;

    @NotBlank(message = "{errors.validation.not.blank}")
    private String description;

    private boolean completed = false;
}
