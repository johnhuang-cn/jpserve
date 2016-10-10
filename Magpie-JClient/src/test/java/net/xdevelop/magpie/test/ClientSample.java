package net.xdevelop.magpie.test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.xdevelop.magpie.PyExecutor;
import net.xdevelop.magpie.PyResult;
import net.xdevelop.magpie.PyServeContext;

public class ClientSample {

	public static void main(String[] args) {
		try {
			PyServeContext.init("localhost", 8888);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		testExec();
		testExecFile();
		testExecStream();
		
		PyServeContext.close();
	}
	
	private static void testExec() {
		System.out.println("# Test exec Python script");
		PyExecutor executor = PyServeContext.getExecutor();
		ArrayList<String> scripts = new ArrayList<String>();
		
		String script = "_result_ = 'Hello World!'";
		scripts.add(script);
	
		script = "a = 2\n"
			    + "b = 3\n"
			    + "_result_ = a * b";
		scripts.add(script);
		
		script = "a = 2\n"
			   + "b = 3\n"
			   + "_result_ = (a, b)";
		scripts.add(script);
		
		script = "a = 2\n"
			   + "b = 3\n"
			   + "_result_ = {'a': a, 'b': b}";
		scripts.add(script);
		
		script = "a = 2\n"
			   + "b = 3\n"
			   + "_result_ = [a , {'a': a, 'b': b}, b]";
		scripts.add(script);	
		
		for (String s : scripts) {
			PyResult rs = executor.exec(s);
			if (rs.isSuccess()) {
				System.out.println("Result: " + rs.getResult());
			}
			else {
				System.out.println("Execute python script failed: " + rs.getMsg());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static void testExecFile() {
		System.out.println("\n# Test exec Python file");
		File f = new File("src/test/java/net/xdevelop/magpie/test/helloworld.py");
		PyResult rs = PyServeContext.getExecutor().exec(f);
		if (rs.isSuccess()) {
			System.out.println("Result: " + rs.getResult());
			ObjectMapper mapper = new ObjectMapper();
			try {
				Object[] myRs = mapper.readValue(rs.getResult(), Object[].class);
				System.out.println("-- result after deserialized");
				System.out.println("0 str: " + myRs[0]);
				System.out.println("1 int: " + myRs[1]);
				System.out.println("2 int: " + myRs[2]);
				System.out.println("3 map(axb): " + ((Map)myRs[3]).get("axb"));
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Execute python script failed: " + rs.getMsg());
		}
	}
	
	private static void testExecStream() {
		System.out.println("\n# Test exec script in stream");
		InputStream in = ClientSample.class.getResourceAsStream("helloworld.py");
		PyResult rs = PyServeContext.getExecutor().exec(in);
		if (rs.isSuccess()) {
			System.out.println("Result: " + rs.getResult());
		}
		else {
			System.out.println("Execute python script failed: " + rs.getMsg());
		}
	}
}
