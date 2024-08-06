package com.hepsiemlak.todo.service;

import com.hepsiemlak.todo.aspect.Log;
import com.hepsiemlak.todo.entity.Todo;
import com.hepsiemlak.todo.entity.User;
import com.hepsiemlak.todo.exception.NotFoundException;
import com.hepsiemlak.todo.mapping.TodoMapper;
import com.hepsiemlak.todo.model.todo.AddTodoRequest;
import com.hepsiemlak.todo.model.todo.TodoResponse;
import com.hepsiemlak.todo.model.todo.UpdateTodoRequest;
import com.hepsiemlak.todo.repository.TodoRepository;
import com.hepsiemlak.todo.repository.UserRepository;
import com.hepsiemlak.todo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;
    private final JwtUtil jwtUtil;

    @Log
    public void createTodo(String authorization, AddTodoRequest request) {
        String username = jwtUtil.extractUsernameByAuthorization(authorization);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));

        Todo todo = todoMapper.toEntity(request, username);
        Todo savedTodo = todoRepository.save(todo);

        user.getTasks().add(savedTodo.getId());
        userRepository.save(user);
    }


    @Log
    public void updateTodo(String authorization, String id, UpdateTodoRequest request) {
        String username = jwtUtil.extractUsernameByAuthorization(authorization);

        Todo todo = todoRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found"));

        if (!todo.getUsername().equals(username)) {
            throw new NotFoundException("Task not found");
        }

        todo = todoMapper.toEntity(request, todo);
        todoRepository.save(todo);
    }

    @Log
    public void deleteTodo(String authorization, String id) {
        try {
            String username = jwtUtil.extractUsernameByAuthorization(authorization);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (!user.getTasks().contains(id)) {
                throw new NotFoundException("Task not found");
            }

            user.getTasks().remove(id);
            userRepository.save(user);

            todoRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Task not found");
        }
    }

    @Log
    public TodoResponse getTodoById(String authorization, String id) {
        String username = jwtUtil.extractUsernameByAuthorization(authorization);
        Todo todo = todoRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found"));

        if (!todo.getUsername().equals(username)) {
            throw new NotFoundException("Task not found");
        }

        return todoMapper.toResponse(todo);
    }

    @Log
    public Page<TodoResponse> getTodos(String authorization, int page, int size) {
        String username = jwtUtil.extractUsernameByAuthorization(authorization);
        Pageable pageable = PageRequest.of(page, size);

        Page<Todo> todos = todoRepository.findByUsername(username, pageable);
        return todoMapper.toPageResponse(todos);
    }
}
