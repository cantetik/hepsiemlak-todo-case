# TO-DO App

## Project Overview

This project is a simple TO-DO application where users can register and create their own to-do lists. The project is developed using Java and Spring Boot, and is supported by Couchbase as the database. Swagger provides API documentation.

## Technology Stack

- Java 11
- Spring Boot
- Spring Data Couchbase
- Couchbase
- Swagger
- Maven
- Docker
- JUnit and Mockito

## Setup and Running

### 1. Clone the Repository

First, clone the project from Github:

```bash
git clone https://github.com/cantetik/hepsiemlak-todo-case.git
cd hepsiemlak-todo-case
```

### 2. Install Dependencies

To install the project dependencies, use Maven:

```bash
./mvnw clean install -DskipTests
```

### 3. Run Tests

To execute the unit tests:

```bash
./mvnw test
```

### 4. Run the Application

Before starting the app, you need to set couchbase infos in /src/main/resources/application.yml.

You also need to open buckets before starting the app.

To start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

### 5. Access Swagger API Documentation

To access the Swagger documentation while the application is running, visit:

```bash
open http://localhost:8080/swagger-ui/index.html
```

## Running with Docker

### 1. Build Docker Image

To build the Docker image:

```bash
docker build -t todo-app .
```

### 2. Run Docker Compose
Before starting the app, you need to set couchbase infos in docker-compose.yml

You also need to open buckets before starting the app.

To start the app and couchbase containers:

```bash
docker-compose up 
```