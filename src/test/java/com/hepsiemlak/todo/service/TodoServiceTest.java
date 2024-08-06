package com.hepsiemlak.todo.service;

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
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    private static final Faker faker = new Faker();

    @InjectMocks
    private TodoService todoService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoMapper todoMapper;

    @Mock
    private JwtUtil jwtUtil;


    @Test
    void it_should_create_todo() {
        String authorization = faker.name().name();
        String username = faker.name().username();

        AddTodoRequest request = AddTodoRequest.builder()
                .title(faker.name().name())
                .description(faker.name().name())
                .build();

        User user = User.builder()
                .tasks(new ArrayList<>())
                .build();

        Todo todo = Todo.builder().build();
        Todo savedTodo = Todo.builder().id(faker.name().name()).build();

        user.getTasks().add(savedTodo.getId());

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(todoMapper.toEntity(request, username)).thenReturn(todo);
        when(todoRepository.save(todo)).thenReturn(savedTodo);
        when(userRepository.save(user)).thenReturn(user);

        todoService.createTodo(authorization, request);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void it_should_throw_not_found_exception_when_creating_todo() {
        String authorization = faker.name().name();
        String username = faker.name().username();

        AddTodoRequest request = AddTodoRequest.builder()
                .title(faker.name().name())
                .description(faker.name().name())
                .build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> todoService.createTodo(authorization, request));

        verify(userRepository, never()).save(any());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void it_should_update_todo() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        UpdateTodoRequest request = UpdateTodoRequest.builder()
                .title(faker.name().name())
                .description(faker.name().name())
                .build();

        Todo todo = Todo.builder()
                .id(id)
                .username(username)
                .build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(todoRepository.findById(id)).thenReturn(Optional.of(todo));
        when(todoMapper.toEntity(request, todo)).thenReturn(todo);
        when(todoRepository.save(todo)).thenReturn(todo);

        todoService.updateTodo(authorization, id, request);
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    void it_should_throw_not_found_exception_when_updating_todo() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        UpdateTodoRequest request = UpdateTodoRequest.builder()
                .title(faker.name().name())
                .description(faker.name().name())
                .build();

        Todo todo = Todo.builder()
                .id(id)
                .username(faker.name().name())
                .build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(todoRepository.findById(id)).thenReturn(Optional.of(todo));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(authorization, id, request));

        verify(todoRepository, never()).save(any());
        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void it_should_delete_todo() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        List<String> todoIds = new ArrayList<>();
        todoIds.add(id);

        User user = User.builder().tasks(todoIds).username(username).build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(todoRepository).deleteById(id);

        todoService.deleteTodo(authorization, id);
        verify(todoRepository, times(1)).deleteById(id);
    }

    @Test
    void it_should_throw_user_not_found_exception_when_deleting_todo() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(authorization, id));

        verify(todoRepository, never()).deleteById(id);
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void it_should_throw_task_not_found_exception_when_deleting_todo() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        User user = User.builder().tasks(new ArrayList<>()).username(username).build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(authorization, id));

        verify(todoRepository, never()).deleteById(id);
        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void it_should_throw_empty_result_data_access_exception_when_deleting_todo() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        List<String> todoIds = new ArrayList<>();
        todoIds.add(id);

        User user = User.builder().tasks(todoIds).username(username).build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        doThrow(EmptyResultDataAccessException.class).when(todoRepository).deleteById(id);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(authorization, id));

        verify(todoRepository, times(1)).deleteById(id);
        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void it_should_get_todo_by_id() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        Todo todo = Todo.builder().id(id).username(username).build();
        TodoResponse todoResponse = TodoResponse.builder().id(id).build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(todoRepository.findById(id)).thenReturn(Optional.of(todo));
        when(todoMapper.toResponse(todo)).thenReturn(todoResponse);

        TodoResponse actual = todoService.getTodoById(authorization, id);
        verify(todoMapper, times(1)).toResponse(todo);
        assertEquals(todoResponse.getId(), actual.getId());
    }

    @Test
    void it_should_throw_not_found_exception_when_getting_todo_by_id() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();

        Todo todo = Todo.builder().id(id).username(faker.name().name()).build();

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(todoRepository.findById(id)).thenReturn(Optional.of(todo));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> todoService.getTodoById(authorization, id));

        verify(todoMapper, never()).toResponse(todo);
        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void it_should_get_todos() {
        String id = faker.name().name();
        String authorization = faker.name().name();
        String username = faker.name().username();
        int page = faker.number().numberBetween(0, 10);
        int size = faker.number().numberBetween(0, 20);

        Todo todo = Todo.builder().id(id).username(username).build();
        Page<Todo> todoPage = new PageImpl<>(Collections.singletonList(todo), PageRequest.of(page, size), 1);
        TodoResponse todoResponse = TodoResponse.builder().build();
        Pageable pageable = PageRequest.of(page, size);

        when(jwtUtil.extractUsernameByAuthorization(authorization)).thenReturn(username);
        when(todoRepository.findByUsername(username, pageable)).thenReturn(todoPage);
        when(todoMapper.toPageResponse(todoPage)).thenReturn(new PageImpl<>(Collections.singletonList(todoResponse)));

        Page<TodoResponse> actual = todoService.getTodos(authorization, page, size);
        verify(todoMapper, times(1)).toPageResponse(todoPage);
        assertEquals(1, actual.getTotalElements());
    }

}