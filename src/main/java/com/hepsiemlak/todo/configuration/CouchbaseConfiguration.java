package com.hepsiemlak.todo.configuration;

import com.hepsiemlak.todo.entity.Todo;
import com.hepsiemlak.todo.entity.Token;
import com.hepsiemlak.todo.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.data.couchbase.SimpleCouchbaseClientFactory;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.convert.MappingCouchbaseConverter;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.data.couchbase.repository.config.RepositoryOperationsMapping;

@Configuration
@EnableCouchbaseRepositories(basePackages = "com.hepsiemlak.todo.repository")
public class CouchbaseConfiguration extends AbstractCouchbaseConfiguration {

    @Value("${couchbase.connection-string}")
    private String connectionString;

    @Value("${couchbase.username}")
    private String username;

    @Value("${couchbase.password}")
    private String password;

    @Value("${couchbase.bucket-user.name}")
    private String userBucketName;

    @Value("${couchbase.bucket-todo.name}")
    private String todoBucketName;

    @Value("${couchbase.bucket-token.name}")
    private String tokenBucketName;

    @Override
    public String getConnectionString() {
        return this.connectionString;
    }

    @Override
    public String getUserName() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getBucketName() {
        return this.userBucketName;
    }

    @Override
    protected void configureRepositoryOperationsMapping(RepositoryOperationsMapping mapping) {
        mapping.mapEntity(User.class, userTemplate());
        mapping.mapEntity(Todo.class, todoTemplate());
        mapping.mapEntity(Token.class, tokenTemplate());
    }

    @Bean
    public CouchbaseTemplate userTemplate() {
        return new CouchbaseTemplate(userClientFactory(), new MappingCouchbaseConverter());
    }

    @Bean
    public CouchbaseClientFactory userClientFactory() {
        return new SimpleCouchbaseClientFactory(getConnectionString(), authenticator(), userBucketName);
    }

    @Bean
    public CouchbaseTemplate todoTemplate() {
        return new CouchbaseTemplate(todoClientFactory(), new MappingCouchbaseConverter());
    }

    @Bean
    public CouchbaseClientFactory todoClientFactory() {
        return new SimpleCouchbaseClientFactory(getConnectionString(), authenticator(), todoBucketName);
    }

    @Bean
    public CouchbaseTemplate tokenTemplate() {
        return new CouchbaseTemplate(tokenClientFactory(), new MappingCouchbaseConverter());
    }

    @Bean
    public CouchbaseClientFactory tokenClientFactory() {
        return new SimpleCouchbaseClientFactory(getConnectionString(), authenticator(), tokenBucketName);
    }
}