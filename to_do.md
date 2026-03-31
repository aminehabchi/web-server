# TODO — Java Local Server

A prioritized list of everything this project needs, from blocking bugs to future enhancements.

---

## Critical — Blocking (Nothing Works Without These)

- [ ] **Implement `Connector.writeToClient()`**
  The method body is empty. The server reads requests but never sends a response back to the client. This is the most critical missing piece.

- [ ] **Fix the request parsing pipeline**
  There are two parallel, disconnected request systems: `ConnectorRequest` (used in the connector layer for streaming) and `HttpRequest` (created by `RequestParser`). They never exchange data. The `ConnectorRequest.finish` flag is never checked to trigger `Application.handleClientRequest()`, meaning the application never actually processes parsed requests.

- [ ] **Fix response assignment in `Dispatcher.handleReturnValue()`**
  When a controller returns an `HttpResponse`, the line `response = httpResponse` reassigns a local variable — it does not modify the response that will be sent. The handler's response is silently discarded.

---

## High Priority — Core HTTP Features

- [ ] **Add `PUT` and `PATCH` HTTP methods**
  Currently only `GET`, `POST`, and `DELETE` are supported. `PUT` and `PATCH` are required for standard REST APIs. Add entries to `HttpMethod` enum and corresponding route maps in `Router`.

- [ ] **Add path variable support**
  Routes like `/users/{id}` or `/posts/{id}/comments/{commentId}` are not supported. All matching is exact-string only. Needs a new `@PathVariable` annotation, pattern-based route matching in `Router`, and a resolver in `ArgumentResolvers`.

- [ ] **Implement proper `Connection: keep-alive`**
  `handleWrite()` closes the connection after every response regardless of the `Connection` header. Keep-alive connections must be tracked and only closed when the client requests it or the timeout is reached.

- [ ] **Fix typo: `filaterChain` → `filterChain`** in `Application.java`

- [ ] **Fix typo: `exculde` → `exclude`** in `FilterChain.java` and the `@Filter` annotation

- [ ] **Validate max body size** (`Config.maxBodySize`)
  `ConnectorRequest.parseBody()` reads the Content-Length but never checks it against the configured limit. Requests over the limit should be rejected with `413 Payload Too Large`.

- [ ] **Add graceful shutdown**
  `Application.stop()` exists but has no hook to drain in-flight requests before closing. Pressing Ctrl+C leaves connections in an undefined state.

---

## Medium Priority — Missing Standard Features

- [ ] **Add `OPTIONS` and `HEAD` method support**
  `OPTIONS` is required for CORS preflight. `HEAD` lets clients fetch headers without a body, which is important for caching and content negotiation.

- [ ] **Add CORS support**
  No `Origin`, `Access-Control-Allow-Origin`, or `Access-Control-Allow-Methods` headers are handled. The server cannot be used as a backend for any browser-based frontend.

- [ ] **URL-decode query parameters and paths**
  `?name=John%20Doe` is stored as-is (`John%20Doe`) instead of being decoded to `John Doe`. `URLDecoder.decode()` is missing from `Url.java`.

- [ ] **Add multipart/form-data parsing**
  File uploads require multipart body parsing. Currently only raw body content is read.

- [ ] **Add redirect helpers**
  No `Location` header builder or redirect response factory. `301`, `302`, `307`, `308` responses have to be constructed manually every time.

- [ ] **Add missing HTTP status codes**
  `HttpStatus` is missing common codes: `204 No Content`, `304 Not Modified`, `301/302/307/308` redirects, `405 Method Not Allowed`, `413 Payload Too Large`, `429 Too Many Requests`, `503 Service Unavailable`.

- [ ] **Fix `ApplicationContext` constructor resolution**
  `clazz.getConstructors()[0]` assumes at least one public constructor exists. Fails with a cryptic `ArrayIndexOutOfBoundsException` if there are none. Add proper validation and a clear error message.

- [ ] **Add circular dependency detection in IoC container**
  A → B → A will cause a `StackOverflowError` at startup with no useful message.

- [ ] **Schedule session cleanup**
  `SessionManager` stores sessions in a `ConcurrentHashMap` but expired sessions are only removed lazily (if accessed). A scheduled cleanup task is needed to prevent unbounded memory growth.

---

## Low Priority — Quality & Robustness

- [ ] **Replace all `System.out.println()` with a logging framework**
  Add SLF4J + Logback or Log4j2. Add configurable log levels in `application.properties`.

- [ ] **Add unit and integration tests**
  No test framework is configured. Add JUnit 5 and Mockito to `pom.xml`. Write tests for `RequestParser`, `Router`, `Dispatcher`, `ArgumentResolvers`, `SessionManager`, and `FilterChain`.

- [ ] **Extend `convertType()` in `ArgumentResolvers`**
  Currently only handles `String`, `int`, `long`, `boolean`. Add `float`, `double`, `BigDecimal`, `UUID`, `LocalDate`, and `Enum` support.

- [ ] **Add gzip/deflate compression**
  Check the `Accept-Encoding` request header and compress responses for clients that support it.

- [ ] **Add cache headers**
  Support `ETag`, `Last-Modified`, `Cache-Control`, and `304 Not Modified` responses for static files.

- [ ] **Support multi-value query parameters**
  `Url.addParameter()` silently overwrites duplicate keys. `?tag=a&tag=b` should produce `List<String>{"a","b"}`, not `"b"`.

- [ ] **Add environment variable / system property overrides for config**
  `application.properties` is the only configuration source. Environment variables should be able to override any property (useful for containers/CI).

- [ ] **Add HTTPS / TLS support**
  Replace `ServerSocketChannel` with `SSLServerSocketChannel` using a configured keystore. The `session.cookie.secure` flag is already in config but TLS is not implemented.

- [ ] **Add rate limiting**
  No per-IP connection or request throttling. The server is fully open to flooding.

- [ ] **Make buffer size configurable**
  The NIO read buffer (8192 bytes) is hardcoded. Large request headers or streaming bodies need a larger or dynamic buffer.

- [ ] **Add `@PostConstruct` / `@PreDestroy` lifecycle hooks**
  Beans have no initialization or cleanup lifecycle. Services that manage connections or caches cannot set up or tear down properly.

- [ ] **Add chunked transfer encoding**
  The server must buffer the entire response in memory before sending. Chunked encoding enables streaming large responses.

- [ ] **Add session invalidation**
  `Session.java` has a `removed` flag but `SessionManager` never acts on it. A standard `session.invalidate()` method that immediately removes the session from the store is missing.

- [ ] **Fix hardcoded default port 80 in `RequestParser`**
  Should default to the port the server is actually listening on, not always 80.

---

## Architecture — Bigger Refactors

- [ ] **Unify the two request parsing systems**
  `ConnectorRequest` and `HttpRequest` serve overlapping purposes. Define a clean boundary: `ConnectorRequest` handles byte streaming, hands off a complete `byte[]` to `RequestParser`, which produces the `HttpRequest`. Remove any duplicated logic.

- [ ] **Implement the thread pool**
  `Config.threadPoolSize` is read and stored but never used. The server processes everything on the selector thread. Add an `ExecutorService` to dispatch request handling off the I/O thread.

- [ ] **Implement the connection timeout**
  `Config.connectionTimeout` is configured but never enforced. Idle connections should be closed after the configured duration.

- [ ] **Make `Router` support method-not-allowed responses**
  If a path exists but not for the requested HTTP method, the server should return `405 Method Not Allowed` (with an `Allow` header listing valid methods), not `404 Not Found`.

- [ ] **Decouple `testing/` from production code**
  Example controllers and filters live inside `src/main/java`. Move them to a separate module or `src/test/java` so they are not bundled into a production build.
