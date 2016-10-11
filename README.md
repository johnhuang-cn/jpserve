## JPServe: Calling Python from JAVA

Jpserve provides a simple and high performance way to execute Python script in JAVA. It includes PyServe and JClient API. 

* PyServe is an execute server running on Python side and listening the execute requests from JAVA.
* The JClient API can executes Python snippet or complete script file from java, it send the script to PyServe and get the execution result. The result is JSON format, so you can exchange the complex data between JAVA and Python flexibly.

## Quick Start
### Python Side
- Install jpserve Package

  ```
  pip install jpserve
  ```
- Start the JPServe
  ```
  >>> from jpserve.jpserve import JPServe
  >>> serve = JPServe(("localhost", 8888))
  >>> serve.start()

  INFO:JPServe:JPServe starting...
  INFO:JPServe:JPServe listening in localhost 8888
  ```
  
### JAVA Side
- Prerequisites
  1. JPserve Maven package are under preparing, so before it, please download and add jpserve-jclient-0.1.0.jar to your project
  2. [Jackson](https://github.com/FasterXML/jackson) library is required
    ```
  <properties>
    <jackson.version>2.7.0</jackson.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>${jackson.version}</version>
    </dependency>
  </dependencies>
    ```
- Sample code
  ```
  // init the PyServeContext, it will make a connection to PyServe
  PyServeContext.init("localhost", 8888);

  // prepare the script, and assign the return value to _result_
  String script = "a = 2\n"
                + "b = 3\n"
                + "_result_ = a * b";

  // sned the script to PyServe, it returns the final result
  PyResult rs = executor.exec(s);

  // check if the execution is success
  if (rs.isSuccess()) {
    System.out.println("Result: " + rs.getResult()); // get the _result_ value
  }
  else {
    System.out.println("Execute python script failed: " + rs.getMsg());
  }              
  ------------------------
  Result: 6
  ```

## Execute Python Script File
```
File f = new File("src/test/java/net/xdevelop/jpclient/test/helloworld.py");
PyResult rs = PyServeContext.getExecutor().exec(f);

InputStream in = ClientSample.class.getResourceAsStream("helloworld.py");
PyResult rs = PyServeContext.getExecutor().exec(in);
```

## Handle Complex Return Type
JPServe uses json.dumps to convert the _result_ value to JSON string, so JAVA can deserializes the result to JAVA object.
```
String script = "a = 2\n"
              + "b = 3\n"
              + "_result_ = ["hello world", a, b, {"axb": a * b}]";
PyResult rs = PyServeContext.getExecutor().exec(s);

if (rs.isSuccess()) {
    System.out.println("Result: " + rs.getResult());
    Object[] rs = mapper.readValue(rs.getResult(), Object[].class);
    System.out.println("-- result after deserialized");
    System.out.println("0 str: " + rs[0]);
    System.out.println("1 int: " + rs[1]);
    System.out.println("2 int: " + rs[2]);
    System.out.println("3 map(axb): " + ((Map)rs[3]).get("axb"));
}
else {
  System.out.println("Execute python script failed: " + rs.getMsg());
} 
------------------------
Result: ["hello world", 2, 3, {"axb": 6}]
-- result after deserialized
0 str: hello world
1 int: 2
2 int: 3
3 map(axb): 6
```
