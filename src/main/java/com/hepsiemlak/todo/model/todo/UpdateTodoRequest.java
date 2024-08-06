package com.hepsiemlak.todo.model.todo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTodoRequest {

    private String title;
    private String description;
    private Boolean completed;
}
