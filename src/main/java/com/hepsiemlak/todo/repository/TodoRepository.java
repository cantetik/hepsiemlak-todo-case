package com.hepsiemlak.todo.repository;

import com.hepsiemlak.todo.entity.Todo;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TodoRepository extends CouchbaseRepository<Todo, String> {

    Page<Todo> findByUsername(String username, Pageable pageable);
}
