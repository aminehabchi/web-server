# Connector → Parser Integration Guide

## TL;DR: How It Works

```
Client Request
     ↓
[Connector.handleRead()]  ← Reads socket bytes
     ↓
Accumulates headers + body as raw bytes
(handles CRLFCRLF split, Content-Length, chunked encoding)
     ↓
[Connector.processPendingRequests()]  ← Packages complete request
     ↓
Calls: Application.ParseReaquest(connector, clientChannel, byte[])
     ↓
[Application.ParseReaquest()]  ← Entry point to your parser
     ↓
Converts byte[] → String (UTF-8)
Calls: RequestParser.parseRequest(rawRequest)
     ↓
[RequestParser.parseRequest()]  ← YOUR PARSER CODE
     ↓
Returns: HttpRequest object
(method, url, headers, cookies, body, etc.)
     ↓
[Application.ParseReaquest() continues]
     ↓
Pass HttpRequest to Dispatcher
Dispatcher routes to Controller
Controller generates HttpResponse
ResponseWriter serializes to String
     ↓
[Connector.writeToClient()]  ← Sends response
     ↓
Response sent to client
```

---

## The Interface Contract

### What Connector Gives You

**Entry Point:** `Application.ParseReaquest()`

```java
public void ParseReaquest(Connector connector, SocketChannel client, byte[] requestData)
```

**Parameters:**
- `connector`: Reference to the Connector instance (for sending responses)
- `client`: SocketChannel to this client (for routing response)
- `requestData`: **Complete HTTP request as bytes**, with format:

  ```
  [Headers (ASCII)]\r\n\r\n[Body (binary or ASCII)]
  ```

**What you receive:**
- A **fully formed request** (not streaming, not partial).
- Headers and body are already **completely read from the socket**.
- The CRLFCRLF boundary is **included in the byte array**.

### Example `requestData` (non-chunked):

```
GET /api/users?id=123 HTTP/1.1\r\n
Host: localhost:8080\r\n
Content-Length: 5\r\n
\r\n
hello
```

As bytes (with CRLFCRLF at position where headers end):
```
[71, 69, 84, 32, 47, 97, ..., 104, 101, 108, 108, 111]
                            ↑
                     CRLFCRLF here (index ~70)
```

### Example `requestData` (chunked):

```
POST /upload HTTP/1.1\r\n
Transfer-Encoding: chunked\r\n
\r\n
5\r\n
hello\r\n
5\r\n
world\r\n
0\r\n
\r\n
```

As bytes (body already assembled by Connector):
```
[80, 79, 83, 84, ..., \r\n\r\n] + [104, 101, 108, 108, 111, 119, 111, 114, 108, 100]
                                   ↑
                            Assembled from chunks
```

---

## RequestParser Integration

### Current Flow (in `Application.ParseReaquest()`)

```java
// Step 1: Convert bytes to String (Connector has done the hard part)
String rawRequest = new String(requestData, StandardCharsets.UTF_8);

// Step 2: Pass to your parser
request = RequestParser.parseRequest(rawRequest);

// Step 3: Dispatcher uses the HttpRequest object
HttpResponse response = this.dispatcher.dispatch(request);
```

### What RequestParser Does

**Input:** Raw HTTP string with CRLFCRLF and body.

```
GET /api/users?id=123 HTTP/1.1\r\nHost: localhost:8080\r\nContent-Length: 5\r\n\r\nhello
```

**Output:** `HttpRequest` object with fields populated:

```java
HttpRequest {
  method = GET
  url = Url("/api/users?id=123")
  httpVersion = "HTTP/1.1"
  headers = {
    "Host" → "localhost:8080",
    "Content-Length" → "5"
  }
  body = "hello"
  cookies = {} (parsed from Cookie header if present)
  authorization = null (or parsed from Authorization header)
}
```

### Key Parser Responsibilities

| Task | Code | Input | Output |
|------|------|-------|--------|
| **Split on CRLFCRLF** | `lines = rawRequest.split("\r\n")` | Raw string | String[] |
| **Parse request line** | `lines[0].split(" ")` | `"GET /path HTTP/1.1"` | `[method, path, version]` |
| **Parse headers** | Loop lines 1-N until empty line | `"Header-Name: value"` | `HashMap<String, String>` |
| **Parse cookies** | `parseCookies()` | Cookie header value | `HashMap<String, String>` |
| **Extract body** | Remaining lines after empty line | All text after CRLFCRLF | `String` |

---

## What Connector Handles (You Don't Need To)

✅ **Already Done — Don't Duplicate:**

| What | Why | Where |
|------|-----|-------|
| **Socket reading** | Uses non-blocking NIO | `Connector.handleRead()` |
| **CRLFCRLF detection** | Byte-level scan | `Connector.handleRead()` |
| **Content-Length parsing** | Extracts integer | `Connector.parseContentMeta()` |
| **Chunked reassembly** | Assembles chunk data | `Connector.accumulateChunkedBody()` |
| **Complete request assembly** | byte[] with headers + body | `Connector.processPendingRequests()` |
| **Body bytes as-is** | No modification | Passed to your parser |

✅ **Your Parser Should:**

1. Split the byte[] (or String) on CRLFCRLF.
2. Parse **request line** (method, URL, version).
3. Parse **headers** into key-value map.
4. Extract **body** as remaining text.
5. Build and return `HttpRequest` object.

---

## Integration Checklist for Your Parser Team

### ✅ Do This

```java
// 1. Accept String (Connector converts bytes for you)
public static HttpRequest parseRequest(String rawRequest) {
    // 2. Split on CRLFCRLF
    String[] lines = rawRequest.split("\r\n");
    
    // 3. Parse request line: METHOD /path HTTP/1.1
    String[] requestLine = lines[0].split(" ");
    request.setMethod(HttpMethod.valueOf(requestLine[0]));
    request.setUrl(new Url(requestLine[1]));
    request.setHttpVersion(requestLine[2]);
    
    // 4. Parse headers (lines 1-N until empty)
    for (int i = 1; i < lines.length; i++) {
        if (lines[i].isEmpty()) break;
        int colonIdx = lines[i].indexOf(":");
        String key = lines[i].substring(0, colonIdx).trim();
        String value = lines[i].substring(colonIdx + 1).trim();
        request.getHeaders().put(key, value);
    }
    
    // 5. Extract body (everything after empty line)
    // Already done by Connector; just store it
    request.setBody(bodyText);
    
    return request;
}
```

### ❌ Don't Do This

```java
// ❌ Don't read from socket (Connector already did)
clientChannel.read(buffer);

// ❌ Don't parse chunked encoding (Connector already did)
// The body bytes are already assembled

// ❌ Don't try to handle partial reads (Connector handles splits)
// You always get a complete request

// ❌ Don't throw Exception if parsing fails
// Catch and handle gracefully; let Application.ParseReaquest() return error
```

---

## Error Handling Strategy

### In RequestParser

```java
public static HttpRequest parseRequest(String rawRequest) {
    HttpRequest request = new HttpRequest();
    
    try {
        // Your parsing logic
        String[] lines = rawRequest.split("\r\n");
        
        if (lines.length == 0) {
            // Return empty/default HttpRequest (don't throw)
            return request;
        }
        
        // Parse request line safely
        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 3) {
            // Return partial/invalid request; Application.ParseReaquest() handles it
            return request;
        }
        
        // Continue parsing...
        return request;
        
    } catch (Exception e) {
        // Log but return something (don't crash)
        System.err.println("Parse error: " + e.getMessage());
        return request;  // Return partial/empty request
    }
}
```

### In Application.ParseReaquest()

```java
public void ParseReaquest(Connector connector, SocketChannel client, byte[] requestData) {
    HttpRequest request = null;
    try {
        String rawRequest = new String(requestData, StandardCharsets.UTF_8);
        request = RequestParser.parseRequest(rawRequest);
    } catch (Exception e) {
        e.printStackTrace();
        // Send error response; don't crash
        connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
        return;
    }
    
    // Dispatch the (possibly incomplete) request
    if (this.dispatcher != null) {
        HttpResponse response = this.dispatcher.dispatch(request);
        String res = ResponseWriter.write(response);
        connector.writeToClient(client, res);
    }
}
```

---

## Example: Non-Chunked Request Flow

```
Raw bytes from Connector:
[71, 69, 84, 32, 47, 117, 115, 101, 114, 115, ...]
"GET /users HTTP/1.1\r\nHost: localhost\r\nContent-Length: 4\r\n\r\ntest"

↓ Connector assembles and calls:

Application.ParseReaquest(connector, clientChannel, requestData)

↓ Converts to String:

"GET /users HTTP/1.1\r\nHost: localhost\r\nContent-Length: 4\r\n\r\ntest"

↓ Calls RequestParser.parseRequest():

Split on "\r\n":
  lines[0] = "GET /users HTTP/1.1"
  lines[1] = "Host: localhost"
  lines[2] = "Content-Length: 4"
  lines[3] = ""  (empty line = headers end)
  lines[4] = "test"  (body)

Parse request line:
  method = GET
  url = /users
  version = HTTP/1.1

Parse headers:
  Host = localhost
  Content-Length = 4

Extract body:
  body = "test"

Return:
  HttpRequest(GET, /users, HTTP/1.1, {Host, Content-Length}, "test")

↓ Dispatcher routes to Controller

↓ Controller generates HttpResponse

↓ Application calls ResponseWriter.write(response)

↓ Application calls connector.writeToClient(client, responseString)

↓ Connector queues response for writing
```

---

## Example: Chunked Request Flow

```
Raw bytes from Connector (already assembled chunks):
[80, 79, 83, 84, 32, 47, 117, 112, ...]
"POST /upload HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\nhelloworld"

Note: Connector already:
  - Parsed "5\r\nhello\r\n5\r\nworld\r\n0\r\n\r\n" 
  - Assembled body = "helloworld"
  - Passed complete request

↓ RequestParser.parseRequest():

Split on "\r\n":
  lines[0] = "POST /upload HTTP/1.1"
  lines[1] = "Transfer-Encoding: chunked"
  lines[2] = ""  (empty line)
  lines[3] = "helloworld"  (assembled body)

Parse request line:
  method = POST
  url = /upload
  version = HTTP/1.1

Parse headers:
  Transfer-Encoding = chunked

Extract body:
  body = "helloworld"  (already reassembled by Connector)

Return:
  HttpRequest(POST, /upload, HTTP/1.1, {Transfer-Encoding: chunked}, "helloworld")

↓ Dispatcher handles normally (no chunked parsing needed in application layer)
```

---

## Summary: Connector ↔ Parser Contract

| Responsibility | Connector | Parser |
|---|---|---|
| **Read socket** | ✓ | ✗ |
| **Detect CRLFCRLF** | ✓ | ✗ |
| **Handle Content-Length** | ✓ | ✗ |
| **Handle chunked encoding** | ✓ | ✗ |
| **Assemble body bytes** | ✓ | ✗ |
| **Pass complete request** | ✓ | ✗ |
| **Parse request line** | ✗ | ✓ |
| **Parse headers** | ✗ | ✓ |
| **Extract body as-is** | ✗ | ✓ |
| **Build HttpRequest object** | ✗ | ✓ |
| **Handle parse errors** | ✗ | ✓ (gracefully) |

---

## Tips for Your Teammate

### 1. **Test the Parser in Isolation**

```java
// Unit test without involving Connector
String raw = "GET /test HTTP/1.1\r\nHost: localhost\r\n\r\n";
HttpRequest req = RequestParser.parseRequest(raw);
assert req.getMethod() == GET;
assert req.getUrl().getPath().equals("/test");
```

### 2. **Handle Edge Cases**

- Missing HTTP version → default to "HTTP/1.1"
- Malformed headers → skip or log
- No body → set body to empty string, not null
- Unknown HTTP method → handle gracefully

### 3. **Don't Modify Connector's Work**

The byte array you receive is **final**:
- Don't try to read more from the socket.
- Don't modify bytes or reassemble.
- Just parse what's there.

### 4. **Keep Parser Stateless**

```java
// ✓ Good: No state
public static HttpRequest parseRequest(String raw) { ... }

// ✗ Bad: Stateful parser
class Parser {
    private String buffer;  // ← DON'T DO THIS
    ...
}
```

The Connector calls your parser for **each complete request**; don't hold state between calls.

### 5. **Test with Real Traffic**

```bash
# Send a test request
curl -X POST http://localhost:8080/api/test \
  -H "Content-Type: application/json" \
  -d '{"key": "value"}'

# Check logs in Application.ParseReaquest() to see raw request
```

---

## Debugging Checklist

**If parser is receiving garbage:**
- Check UTF-8 encoding: `new String(bytes, StandardCharsets.UTF_8)`
- Verify CRLFCRLF is included in the byte array (it is)

**If body is missing:**
- Verify split on `\r\n` (not just `\n`)
- Ensure you're reading lines **after** the empty line

**If headers are incomplete:**
- Check loop condition; should stop at first empty line, not last line

**If chunked requests fail:**
- Remember Connector already assembled; you should see body as one string, no chunk markers

---

## Next Steps for Your Team

1. **Review** `RequestParser.parseRequest()` current implementation.
2. **Add** form data parsing (URL-encoded or multipart).
3. **Add** query parameter extraction from URL.
4. **Add** request validation (e.g., check required headers).
5. **Test** with curl/Postman against the running server.
6. **Monitor** logs in `Application.ParseReaquest()` to see raw requests.

That's it! Your parser is simple: **split, parse, return object**. Connector handles all the hard I/O work.
