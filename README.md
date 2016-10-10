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
### Codes:
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
```
### Output:
```
Result: 6
```

