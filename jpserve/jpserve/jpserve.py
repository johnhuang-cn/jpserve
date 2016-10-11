r"""JPServer is a Python script executor running on the Python side.

JPServe receiving and executing the script from 3rd-part languages, 
then send back the result as JSON format to the caller.
   
Usages:
- Start server
    server = JPServe(("hostname", port))
    server.start()
       
- Stop server
    server.shutdown()
       
- Set log level
    server.setLogLevel(logging.DEBUG)

- The sample to make call from java side
    import net.xdevelop.jpserve.PyClient
    import net.xdevelop.jpserve.PyResult
    
    String script = "a = 2\r\n" +
                    "b = 3\r\n" +
                    "_result_ = a * b\r\n";
    
    PyClient client = PyClient.getInstance("localhost", "8888");                
    PyResult rs = client.exec(script);
    
    // output the _result_ value calculated by Python
    if (rs.getSuccess()) {
        System.out.println(rs.getResult());
    }
    else {
        System.out.println(rs.getMsg());
    }
    
"""

from socketserver import StreamRequestHandler, ThreadingTCPServer, ForkingTCPServer
import logging
import os
import threading
import json

__all__ = ["JPServe"]

logger = logging.getLogger('JPServe')

class JPServe():

    def __init__(self, server_address):
        self.server_address = server_address

        logging.basicConfig(level=logging.INFO)
        logger.setLevel(logging.INFO)
  
    def start(self):
        logger.info("JPServe starting...")

        if os.name == 'nt':
            self.serv = PThreadingTCPServer(self.server_address, ServeHandler)
        else:
            self.serv = PForkingTCPServer(self.server_address, ServeHandler)

        self.t = threading.Thread(target=self.serv.serve_forever)
        self.t.start()
        
        logger.info("JPServe listening in %s %s " % self.server_address)  
            
    def shutdown(self):
        try:
            self.serv.shutdown()
            self.serv.server_close()
        except Exception as e:
            logger.error(e.getMessage())
            
        logger.info("JPServe stopped.")  
        
    def setLogLevel(self, level):
        logger.setLevel(level)
        
         
# Constant values for ServerHandler
BEGIN_MARK = b"#!{"
END_MARK   = b"#!}"
CMD_EXIT  = b"#!exit"         
class ServeHandler(StreamRequestHandler):  
    r""" The handler to receive and exec the python script from 3rd-part side.
    
    Client request syntax:
        line0:   #!{
        line1-n:     python script
        linen+1:     _result_ = the value return to caller
        linen+2: #!}
    
    Response to client:
        line0:  #!{
        #!{
            {
                "result": _result_ value,
                "success": true or false,
                "msg": "success" or "error message"
            }
        #!}
        
    Example:
        Request:
            #!{
            a = 2 * 3
            _result_= a
            #!}
        
        Response:
            #!{
                {
                    "result": 6,
                    "success": true,
                    "msg": "success"
                }
            #!}
    """  
    def handle(self):
        self.request.setblocking(False)
            
        while True:  
            if self.server.stopped:
                break
                
            try:
                # read begin mark #!{
                begin_mark = self.rfile.readline().strip()
                if (begin_mark == CMD_EXIT): # end request
                    logger.info("Client (%s:%d) exit." % (self.client_address[0], self.client_address[1]))
                    break
                
                if begin_mark != BEGIN_MARK:
                    continue
      
                # read python script
                script = ""
                lines = [] 
                while not self.server.stopped:
                    data = self.rfile.readline()
                    
                    if data.strip() == END_MARK: # check end mark
                        break
                    elif len(data) > 0:
                        lines.append(data.decode("utf-8"))
                        
                script = "".join(lines)
                logger.info("Received script from (%s:%d): \n%s" % (self.client_address[0], self.client_address[1], script))
            except Exception as e:
                logger.error("Read request failed: %s" % str(e))
                break
            
            if self.server.stopped:
                break
            
            # exec script
            local_vars = {}
            try:
                local_vars["_result_"] = None
                exec(compile(script, "<string>", "exec"), globals(), local_vars)
                local_vars["_success_"] = True
                local_vars["_msg_"] = "success"
            except Exception as e:
                logger.error("Exec script failed: %s" % str(e))
                local_vars["_success_"] = False
                local_vars["_msg_"] = "Execute script failed: %s" % str(e)
                
            # response the result as JSON
            try:
                response = self.toJSON(local_vars)
                logger.info("return: %s" % response.decode("utf-8"))

                self.wfile.write("#!{\r\n".encode("utf-8"))
                self.wfile.write(response)
                self.wfile.write("\r\n#!}\r\n".encode("utf-8"))
            except Exception as e:
                logger.error("Sent result to client failed: %s" % str(e))
                break
    
    def toJSON(self, local_vars):
        rs = {"success": local_vars["_success_"], "msg": local_vars["_msg_"], "result": json.dumps(local_vars["_result_"]) }
        response = json.dumps(rs, indent=4)
        response = bytes(response, "utf-8")
        
        return response
        
 
class PThreadingTCPServer(ThreadingTCPServer):
    def __init__(self, server_address, RequestHandlerClass, bind_and_activate=True):
        """Constructor.  May be extended, do not override."""
        ThreadingTCPServer.__init__(self, server_address, RequestHandlerClass)
        self.stopped = False
        
    def shutdown(self):
        self.stopped = True
        ThreadingTCPServer.shutdown(self)
        
        
class PForkingTCPServer(ForkingTCPServer):
    def __init__(self, server_address, RequestHandlerClass, bind_and_activate=True):
        """Constructor.  May be extended, do not override."""
        ThreadingTCPServer.__init__(self, server_address, RequestHandlerClass)
        self.stopped = False
        
    def shutdown(self):
        self.stopped = True
        ForkingTCPServer.shutdown(self)
             
 
if __name__ == "__main__":  
    host = "localhost"
    port = 8888
    addr = (host, port)  
    jpserve = JPServe(addr)
    jpserve.start()
    