# Java Local Server

A lightweight, custom-built HTTP server framework written from scratch in Java. This project provides a Spring Framework-like web server with dependency injection, annotation-based routing, request filtering, and asynchronous I/O.

## Overview

**Java Local Server** is an educational HTTP server framework that demonstrates core web server concepts including:
- Asynchronous, non-blocking I/O using Java NIO
- Dependency injection container (IoC)
- Annotation-based route discovery and request mapping
- Filter chain pattern for request/response processing
- Automatic JSON serialization/deserialization
- Multi-port server support

## Features

- **Annotation-Based Configuration**: Use `@Controller`, `@Service`, `@Component`, `@Repository` for dependency injection
- **HTTP Method Routing**: `@Get`, `@Post`, `@Delete` annotations for route mapping
- **Parameter Injection**: Support for `@RequestParam`, `@RequestBody`, `@RequestHeader`, `@PathVariable`
- **Filter Chain**: Ordered filter execution with port-specific exclusions via `@Filter`
- **Automatic JSON Handling**: Serialize objects to JSON responses automatically
- **Multi-Port Support**: Run multiple server instances on different ports simultaneously
- **Asynchronous I/O**: Non-blocking request handling using Java NIO `AsynchronousServerSocketChannel`
- **Dependency Injection**: Automatic bean discovery and constructor injection

## Project Structure

```
src/main/java/com/http/
├── Main.java                       # Application entry point
├── annotation/                     # Custom framework annotations
│   ├── classes/                   # @Controller, @Service, @Component, @Repository, @Filter
│   ├── methodes/                  # @Get, @Post, @Delete
│   └── parameters/                # @RequestParam, @RequestBody, @RequestHeader, @PathVariable
├── application/
│   ├── Application.java           # Core application orchestrator
│   ├── context/
│   │   └── ApplicationContext.java # IoC container
│   ├── dispatcher/
│   │   ├── Dispatcher.java        # Request dispatcher
│   │   └── ArgumentResolvers.java # Method parameter resolution
│   ├── routing/
│   │   ├── Router.java            # HTTP route registry
│   │   └── Route.java             # Route definition
│   └── filters/
│       ├── FilterChain.java       # Filter execution manager
│       ├── OncePerRequestFilter.java # Filter interface
│       └── SecurityFilterChain.java  # Security configuration
├── config/
│   ├── connectors/
│   │   └── Connector.java         # NIO asynchronous server
│   ├── enums/
│   │   ├── HttpMethod.java        # GET, POST, DELETE
│   │   └── HttpStatus.java        # HTTP status codes
│   ├── url/
│   │   └── Url.java               # URL and query parameter parsing
│   └── cookie/
│       └── Cookie.java            # HTTP cookie handling
├── http/
│   ├── request/
│   │   └── HttpRequest.java       # HTTP request object
│   └── response/
│       └── HttpResponse.java      # HTTP response object
├── utils/
│   └── JSON.java                  # JSON serialization utilities
└── testing/                       # Example implementations
    ├── helloController.java       # Sample controller
    ├── FiltersConfig.java         # Filter configuration
    └── CutsomFilter[1-4].java     # Example filters
```

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.x

### Build and Run

```bash
# Build the project
mvn clean package

# Run the server
mvn exec:java -Dexec.mainClass="com.http.Main"

# Or use the run script
./run.sh
```

The server will start on ports 8080 and 8081 by default.

### Create Your First Controller

```java
package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.annotation.parameters.RequestParam;
import com.http.annotation.parameters.RequestHeader;

@Controller
public class HelloController {

    @Get(path = "/hello")
    public String sayHello(
        @RequestParam("name") String name,
        @RequestHeader("User-Agent") String userAgent
    ) {
        return "<h1>Hello, " + name + "!</h1><p>Your browser: " + userAgent + "</p>";
    }

    @Get(path = "/api/user")
    public User getUser() {
        return new User("John Doe", 30); // Automatically serialized to JSON
    }
}
```

### Create a Custom Filter

```java
package com.http.testing;

import com.http.annotation.classes.Filter;
import com.http.application.filters.OncePerRequestFilter;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Filter(order = 1)
public class LoggingFilter implements OncePerRequestFilter {

    @Override
    public boolean doFilter(HttpRequest request, HttpResponse response) {
        System.out.println("Request: " + request.getMethod() + " " + request.getUrl());
        return true; // Continue filter chain
    }
}
```

## Architecture

### Request Processing Flow

```
Client Request
    ↓
Connector.readFromClient() - Async read via NIO
    ↓
Application.handleClientRequest()
    ↓
FilterChain.applyFilters() - Execute filters in order
    ↓ (if all filters pass)
Dispatcher.dispatch()
    ├─ Router.getRoute() - Find matching route
    ├─ ArgumentResolvers.resolveArguments() - Inject method parameters
    ├─ Route.invoke() - Call controller method
    └─ Handle return value (String → HTML, Object → JSON)
    ↓
Application.writeResponseToClient()
    ↓
Connector.writeToClient() - Async write via NIO
    ↓
Client Response
```

### Core Components

#### 1. **Application** (src/main/java/com/http/application/Application.java:16)
The main orchestrator that manages connectors, application context, router, dispatcher, and filter chain.

#### 2. **ApplicationContext** (IoC Container)
- Scans classpath for `@Controller`, `@Service`, `@Component`, `@Repository`
- Creates singleton bean instances
- Supports constructor-based dependency injection
- Provides bean retrieval by type

#### 3. **Router**
- Maintains separate route maps for GET, POST, DELETE
- Auto-discovers routes from controller methods
- Provides route lookup by HTTP method and path

#### 4. **Dispatcher**
- Routes requests to appropriate controller methods
- Resolves method parameters using ArgumentResolvers
- Handles return values (String, Object, HttpResponse)
- Automatically serializes objects to JSON

#### 5. **ArgumentResolvers**
Resolves method parameters by type:
- `HttpRequest` → injects the request object
- `HttpResponse` → injects the response object
- `@RequestBody` → deserializes JSON body
- `@RequestHeader("name")` → extracts header value
- `@RequestParam("name")` → extracts query parameter
- Supports type conversion: String, int, long, boolean

#### 6. **FilterChain**
- Sorts filters by `@Filter(order = N)`
- Executes filters sequentially
- Supports port-specific exclusions via `@Filter(exclude = {port})`
- Stops chain if any filter returns false

#### 7. **Connector** (NIO Server)
- Uses `AsynchronousServerSocketChannel` for async I/O
- Configurable port (1-65535) and request timeout
- Non-blocking request/response handling
- Default port: 8080, timeout: 5000ms

## Annotations Reference

### Class-Level Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Controller` | Marks class as HTTP request handler | `@Controller` |
| `@Service` | Business logic service (injectable) | `@Service` |
| `@Component` | Generic component (injectable) | `@Component` |
| `@Repository` | Data access object (injectable) | `@Repository` |
| `@Filter` | HTTP filter with order and port exclusions | `@Filter(order = 1, exclude = {8081})` |
| `@FilterChainConfig` | Filter chain configuration class | `@FilterChainConfig` |

### Method-Level Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Get` | Maps GET request to method | `@Get(path = "/users")` |
| `@Post` | Maps POST request to method | `@Post(path = "/users")` |
| `@Delete` | Maps DELETE request to method | `@Delete(path = "/users/{id}")` |

### Parameter-Level Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@RequestParam` | Extracts query parameter | `@RequestParam("id") int id` |
| `@RequestBody` | Deserializes JSON request body | `@RequestBody User user` |
| `@RequestHeader` | Extracts HTTP header | `@RequestHeader("Authorization") String token` |
| `@PathVariable` | Extracts path variable | `@PathVariable("id") int id` |

## Dependencies

```xml
<dependencies>
    <!-- Lombok - Reduces boilerplate code -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>

    <!-- Reflections - Classpath scanning for annotations -->
    <dependency>
        <groupId>org.reflections</groupId>
        <artifactId>reflections</artifactId>
        <version>0.10.2</version>
    </dependency>

    <!-- Jackson - JSON serialization/deserialization -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.1</version>
    </dependency>
</dependencies>
```

## Design Patterns Used

1. **Dependency Injection (IoC Container)** - Automatic bean discovery and injection
2. **Annotation-Based Configuration** - Route mapping and component scanning
3. **Reflection-Based Discovery** - Runtime classpath scanning
4. **Strategy Pattern** - Different parameter resolution strategies
5. **Chain of Responsibility** - Filter chain pattern
6. **Factory Pattern** - Static factory methods for HTTP objects
7. **Adapter Pattern** - Dispatcher as adapter between router and controllers

## Configuration

### Main Configuration (src/main/java/com/http/Main.java:7)

```java
public static void main(String[] args) throws Exception {
    Application app = new Application();
    app.addConnector(new Connector(8080)); // HTTP on port 8080
    app.addConnector(new Connector(8081)); // HTTP on port 8081
    app.start();

    Thread.currentThread().join();
}
```

### Default Settings
- Default Port: 8080
- Default Request Timeout: 5000ms
- HTTP Version: HTTP/1.1
- Character Encoding: UTF-8

## HTTP Status Codes

The framework supports comprehensive HTTP status codes:

**2xx Success**
- 200 OK
- 201 CREATED
- 204 NO_CONTENT

**3xx Redirection**
- 301 MOVED_PERMANENTLY
- 302 FOUND

**4xx Client Errors**
- 400 BAD_REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOT_FOUND

**5xx Server Errors**
- 500 INTERNAL_SERVER_ERROR
- 501 NOT_IMPLEMENTED
- 502 BAD_GATEWAY

## Response Handling

The dispatcher automatically handles different return types:

| Return Type | Content-Type | Example |
|------------|--------------|---------|
| `String` | text/html | `return "<h1>Hello</h1>";` |
| `Number` | text/plain | `return 42;` |
| `Object` | application/json | `return new User("John");` |
| `HttpResponse` | Custom | Full control over response |
| `void/null` | Empty response | No body content |

## URL and Query Parameter Parsing

The framework includes a built-in URL parser that extracts:
- Scheme and protocol
- Host and domain
- Query parameters as Map<String, String>

```java
Url url = new Url("http://localhost:8080/users?id=123&name=John");
String id = url.getQueryParams().get("id"); // "123"
String name = url.getQueryParams().get("name"); // "John"
```

## Cookie Support

```java
Cookie cookie = new Cookie();
cookie.setName("sessionId");
cookie.setValue("abc123");
cookie.setPath("/");
cookie.setMaxAge(3600);
cookie.setHttpOnly(true);

response.addCookie(cookie);
```

## Examples

### Example 1: Simple GET Endpoint

```java
@Controller
public class HomeController {

    @Get(path = "/")
    public String home() {
        return "<html><body><h1>Welcome to Java Local Server!</h1></body></html>";
    }
}
```

### Example 2: JSON API Endpoint

```java
@Controller
public class UserController {

    @Get(path = "/api/users")
    public List<User> getUsers() {
        return List.of(
            new User("Alice", 25),
            new User("Bob", 30)
        );
    }

    @Post(path = "/api/users")
    public User createUser(@RequestBody User user) {
        // Save user logic
        return user;
    }
}
```

### Example 3: Filter with Port Exclusion

```java
@Filter(order = 1, exclude = {8081}) // Only runs on port 8080
public class AuthenticationFilter implements OncePerRequestFilter {

    @Override
    public boolean doFilter(HttpRequest request, HttpResponse response) {
        String auth = request.getHeader("Authorization");
        if (auth == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED);
            response.setBody("Unauthorized");
            return false; // Stop filter chain
        }
        return true; // Continue
    }
}
```

## Current Limitations

1. **HTTP Request Parsing** - Raw HTTP parsing is not fully implemented (uses default request object)
2. **PathVariable** - Annotation defined but not fully integrated with routing
3. **HTTPS Support** - Currently only supports HTTP
4. **WebSocket** - Not supported
5. **Static File Serving** - Not implemented
6. **Template Engines** - No built-in support for templating

## Development Status

Recent development focus (based on git history):
- Filter chain implementation
- Argument resolver functionality
- Request flow testing
- Application context setup
- Security filter chain

## Contributing

This is an educational project demonstrating HTTP server internals and web framework architecture. Feel free to explore, learn, and extend!

## License

This project is provided as-is for educational purposes.

## Author

Built with Java 17 and lots of reflection magic!
