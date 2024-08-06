package com.hepsiemlak.todo.mapping;

import com.hepsiemlak.todo.entity.Todo;
import com.hepsiemlak.todo.model.todo.AddTodoRequest;
import com.hepsiemlak.todo.model.todo.TodoResponse;
import com.hepsiemlak.todo.model.todo.UpdateTodoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TodoMapper {

    Todo toEntity(AddTodoRequest addTodoRequest,String username);

    Todo toEntity(UpdateTodoRequest updateTodoRequest, @MappingTarget Todo todo);

    TodoResponse toResponse(Todo todo);

    List<TodoResponse> toResponse(List<Todo> todos);

    default Page<TodoResponse> toPageResponse(Page<Todo> todoPage) {
        List<TodoResponse> todoResponseList = toResponse(todoPage.getContent());
        return new PageImpl<>(todoResponseList, todoPage.getPageable(), todoPage.getTotalElements());
    }

}
