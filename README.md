```java
Socket client = server.accept();           
// → Get the connection pipe

InputStream is = client.getInputStream();  
// → Access the raw bytes flowing through the pipe

InputStreamReader isr = new InputStreamReader(is); 
// → Interpret those bytes as text characters (not just numbers)

BufferedReader in = new BufferedReader(isr);       
// → Read efficiently in chunks and provide readLine()

String message = in.readLine();            
// → Get your actual message as a String
```