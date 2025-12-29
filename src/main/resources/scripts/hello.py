#!/usr/bin/env python3

import os


method = os.environ.get("REQUEST_METHOD", "UNKNOWN")

print("<html>")
print("<body>")
print("<h1>Hello from Python CGI</h1>")
print(f"<p>Request Method: {method}</p>")
print("</body>")
print("</html>")
