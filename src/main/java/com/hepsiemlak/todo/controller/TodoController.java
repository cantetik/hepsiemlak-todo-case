package com.hepsiemlak.todo.controller;

import com.hepsiemlak.todo.aspect.Log;
import com.hepsiemlak.todo.model.todo.AddTodoRequest;
import com.hepsiemlak.todo.model.todo.TodoResponse;
import com.hepsiemlak.todo.model.todo.UpdateTodoRequest;
import com.hepsiemlak.todo.service.TodoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @Log
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "Authorization")
    public void createTodo(@RequestHeader("Authorization") String authorization, @RequestBody @Valid AddTodoRequest request) {
        todoService.createTodo(authorization, request);
    }

    @Log
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "Authorization")
    public void updateTodo(@RequestHeader("Authorization") String authorization,
                           @PathVariable String id,
                           @RequestBody @Valid UpdateTodoRequest request) {
        todoService.updateTodo(authorization, id, request);
    }

    @Log
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "Authorization")
    public void deleteTodo(@RequestHeader("Authorization") String authorization, @PathVariable String id) {
        todoService.deleteTodo(authorization, id);
    }

    @Log
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "Authorization")
    public TodoResponse getTodoById(@RequestHeader("Authorization") String authorization, @PathVariable String id) {
        return todoService.getTodoById(authorization, id);
    }

    @Log
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "Authorization")
    public Page<TodoResponse> getTodos(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return todoService.getTodos(authorization, page, size);
    }
}