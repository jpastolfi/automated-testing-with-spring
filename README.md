This repository has been created to store content and code for [Giuliana Bezerra](https://www.linkedin.com/in/giulianabezerra/)'s [Automated tests with Spring Boot](https://www.udemy.com/course/testes-automatizados-na-pratica-com-spring-boot/) course. I've decided to take it so that I could deepen my testing knowledge in Java and improve my portfolio and CV.

During the course, the following frameworks and libraries were used:
- Spring Boot Test
- Junit 5
- Mockito
- AssertJ
- Hamcrest
- JsonPath
- Jacoco
- Pitest
- Apache Commons
- Test Containers

# API and routes

For this project, an API for creating, listing and deleting planets from the Star Wars universe has been created. The following routes can be accessed:

## GET /planets?climate=foo&terrain=bar
- Used to list planets based on filters supplied by query string. If no filters are received, all planets are returned. If no planets are found, returns an empty List. Returns a HTTP status 200.

## GET /planets/id
- Lists a planet by its id. Returns 200 with the found planet or 404 if not found.

## GET /planets/name/foobar
- Lists a planet by its name. Returns 200 with the found planet or 404 if not found.

## POST /planets
- Receives a name, climate and terrain to create a planet and returns a status 201 with the created planet.

## DELETE /planets/id
- Deletes a planet by its id. Returns 204 or 404 if not found.

# Database
The project uses a MySQL container running on Docker

# Running the project
After cloning the project, you can just run the command `docker compose up` on the root folder of the application with the Docker app running and then execute the application using `./mvnw spring-boot:run`