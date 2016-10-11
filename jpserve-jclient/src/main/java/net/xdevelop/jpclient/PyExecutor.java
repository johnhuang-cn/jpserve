package net.xdevelop.jpclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * The executor to execute the Python script and get the result with JSON format.
 * 
 * Usage:
 * <pre>
 *   PyServeContext.init(host, port);
 *   PyExecutor executor = PyServeContext.getExecutor();
 *   PyResult rs = executor.exec(script);
 *   if (rs.isSuccess()) {
 *   	System.out.println(rs.getResult());
 *   }
 *   else {
 *   	System.out.println(rs.getMsg());
 *   }
 * </pre>
 * 
 * The script should ends with
 * _result_ = "The value returns to client"
 * 
 * The _result_ can be got by Result.getResult(). 
 * If _result_ value is list, tuple, dict, ... it will be converted to JSON string. 
 * 
 * @author john.huang
 *
 */
public class PyExecutor {
	// Max script size 8M
	private final static int MAX_SCRIPT_SIZE = 1024 * 1024 * 8;
	
	PyServeContext context;
	
	PyExecutor(PyServeContext context) {
		this.context = context;
	}
	
	/**
	 * Executes a string format script, each line should split by "\n" or "\r" or "\r\n".
	 * 
	 * Sample:
	 * 		String script = "a = 3\nb = 2\n_result_ = a * b\n"; 
	 * 
	 * @param script
	 * @return execution result, success: if executes successfully, msg: error message if failed, result: the _result_ returns from PyServe
	 */
	public PyResult exec(String script) {
		if (context == null) {
			PyServeContext.getResultOfException("PySeveContext has not been initialized.");
		}
		
		if (script.length() > MAX_SCRIPT_SIZE) {
			PyServeContext.getResultOfException("Exceeds the max script size limit (8M).");
		}
		return context.executeScript(script);
	}
	
	/**
	 * Execute a Python script file
	 * 
	 * @param scriptFile
	 * @return
	 */
	public PyResult exec(File scriptFile) {
		try {
			return exec(new FileInputStream(scriptFile));
		}
		catch (FileNotFoundException e) {
			return PyServeContext.getResultOfException(e);
		}
	}
	
	/**
	 * Read script from the stream and execute it
	 * 
	 * @param scriptStream
	 * @return
	 */
	public PyResult exec(InputStream scriptStream) {
		try {
			String script = streamToString(scriptStream);
			return exec(script);
		} 
		catch (Exception e) {
			return PyServeContext.getResultOfException(e);
		}
	}
	
	private String streamToString(InputStream in) throws IOException, PyServeException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		OutputStreamWriter out = new OutputStreamWriter(bout);
		InputStreamReader input = new InputStreamReader(in);
		
		char[] buffer = new char[8192];
		
		long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            out.write(buffer, 0, n);
            count += n;
        }

        if (count > MAX_SCRIPT_SIZE) {
        	throw new PyServeException("Exceed the max script size limit (8M).");
        }
        out.flush();
        return bout.toString();		
	}
}
