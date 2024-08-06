package com.hepsiemlak.todo.repository;

import com.hepsiemlak.todo.entity.Token;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

import java.util.Optional;

public interface TokenRepository extends CouchbaseRepository<Token, String> {

    Optional<Token> findByRefreshToken(String refreshToken);

}
