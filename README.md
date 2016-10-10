# Magpie
Magpie enable you calling Python from JAVA. Now it includes 2 parts: PyServe and JClient. PyServe is a script execute server running on Python side, and JClient is the JAVA API used to execute Python script.

# Quick Start
## Python Side
Open Python console, import magpie and start the PyServe:
```
>>> from magpie.pyserve import PyServe
>>> server = PyServe(("localhost", 8888))
>>> server.start()

INFO:pserve:Magpie magpie starting...
INFO:pserve:Magpie magpie listening in localhost 8888 
```
## JAVA Side
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

# Execute Python Script File
```
    File f = new File("src/test/java/net/xdevelop/magpie/test/helloworld.py");
		PyResult rs = PyServeContext.getExecutor().exec(f);

    InputStream in = ClientSample.class.getResourceAsStream("helloworld.py");
		PyResult rs = PyServeContext.getExecutor().exec(in);
```

# Complex Return Type
PyServe uses json.dumps to convert the _result_ value to JSON string, so JAVA can deserializes the result to JAVA object.
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
