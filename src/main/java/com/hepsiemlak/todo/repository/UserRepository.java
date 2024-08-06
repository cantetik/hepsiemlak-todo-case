package com.hepsiemlak.todo.repository;

import com.hepsiemlak.todo.entity.User;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

import java.util.Optional;

public interface UserRepository extends CouchbaseRepository<User, String> {

    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);
}
