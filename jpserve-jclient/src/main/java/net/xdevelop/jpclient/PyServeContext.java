package net.xdevelop.jpclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * PyServeContext initializes and maintenance the socket connection to PyServer, 
 * then you can get an executor from it to execute the Python script.
 * 
 * Usage:
 * <pre>
 *   PyServeContext.init(host, port);
 *   PyExecutor executor = PyServeContext.getExecutor();
 *   PyResult rs = executor.exec(script);
 * </pre>  
 * 
 * @author john.huang(john.h.cn@gmail.com)
 *
 */
public class PyServeContext {
	private final static String LINE_BREAK = "\r\n";
	private final static String MARK_BEGIN = "#!{";
	private final static String MARK_END = "#!}";
	private final static String CMD_EXIT = "#!exit";
	
	private static PyServeContext instance;
	
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	
	private boolean isClosed;
	
	private PyServeContext(String host, int port) throws PyServeException {
		// init a connection to PyServe
		try {
			socket = new Socket(host, port);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			throw new PyServeException(e);
		}
		
		isClosed = false;
	}
	
	/**
	 * Initialize a context before executing Python script
	 * 
	 * @param host the PyServe host name or IP
	 * @param port the PyServe listening port
	 * @throws PyServeException throws the exception if failed to connect to PyServe
	 */
	public final static void init(String host, int port) throws PyServeException {
		instance = new PyServeContext(host, port);
	}
	
	/**
	 * Get a Python script executor
	 * 
	 * @return Python script executor
	 */
	public static PyExecutor getExecutor() {
		return new PyExecutor(instance);
	}
	
	/**
	 * Send the script to PyServe, receive and handle the result from PyServe side
	 * 
	 * @param script
	 * @return it includes if execute the script successfully, the error message or the _result_ value returns from PyServe
	 */
	PyResult executeScript(String script) {
		PyResult rs = new PyResult();
		if (isClosed) {
			rs.setSuccess(false);
			rs.setMsg("PyServeContext already closed");
			return rs;
		}
		
		StringBuilder request = new StringBuilder();
		request.append(MARK_BEGIN).append(LINE_BREAK).append(script).append(LINE_BREAK).append(MARK_END).append(LINE_BREAK);
		
		try {
			// send script to PyServe
			writer.write(request.toString());
			writer.flush();
			
			// read result from PyServe
			StringBuilder response = new StringBuilder();
			while (!MARK_BEGIN.equals(reader.readLine())) {}
			
			while(true) {
				String s = reader.readLine();
				if (MARK_END.equals(s)) {
					break;
				}
				
				response.append(s);
			}
			
			ObjectMapper mapper = new ObjectMapper(); 
			rs = mapper.readValue(response.toString(), PyResult.class);
		} catch (Exception e) {
			rs = getResultOfException(e);
		}
		
		return rs;
	}
	
	/**
	 * Close the PyServeContext, it will release the connection between client and PyServe.
	 */
	public static void close() {
		if (instance != null) {
			instance.closeContext();
		}
	}
	
	private void closeContext() {
		isClosed = true;
		
		try {
			writer.write(CMD_EXIT + LINE_BREAK);
			writer.flush();
			writer.close();
			reader.close();
		}
		catch (Exception e) {
		}
		finally {
			try {
				socket.close();
			}
			catch (IOException e) {}
		}
	}
	
	static PyResult getResultOfException(Exception e) {
		return getResultOfException(e.getMessage());
	}
	
	static PyResult getResultOfException(String msg) {
		PyResult result = new PyResult();
		result.setSuccess(false);
		result.setMsg(msg);
		
		return result;
	}
}
