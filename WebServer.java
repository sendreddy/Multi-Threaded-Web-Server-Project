//Sumana Endreddy, Srujana Endreddy, Yashika Y
//CSC 360-02 - Dr. Li

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

public final class WebServer {
    private static final int PORT1 = 8888;
    private static final int PORT2 = 5555;

    public static void main(String[] args) throws Exception {
        // Selector for incoming time requests
        Selector selector = Selector.open();

        // Create a new server socket and set to non-blocking mode
        ServerSocketChannel ssc1 = ServerSocketChannel.open();
        ssc1.configureBlocking(false);
        ServerSocket ss1 = ssc1.socket();
        InetSocketAddress address1 = new InetSocketAddress(PORT1);
        ss1.bind(address1);

        // Create another server socket and set to non-blocking mode
        ServerSocketChannel ssc2 = ServerSocketChannel.open();
        ssc2.configureBlocking(false);
        ServerSocket ss2 = ssc2.socket();
        InetSocketAddress address2 = new InetSocketAddress(PORT2);
        ss2.bind(address2);

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        ssc1.register(selector, SelectionKey.OP_ACCEPT);
        ssc2.register(selector, SelectionKey.OP_ACCEPT);

        // Process any pending connections
        while (true) {
            // Wait for an event one of the registered channels
            selector.select();

            // Iterate over the set of keys for which events are available
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    // Accept the connection
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    SocketChannel sc = ssc.accept();
                
                    // Adjust this part: Set the accepted SocketChannel to blocking mode before obtaining the Socket
                    sc.configureBlocking(true);  // This line is added to enable blocking mode
                
                    Socket socket = sc.socket();  // Now, 'socket' is in blocking mode and compatible with your threads
                
                    // Handle the request based on the port
                    int port = ssc.socket().getLocalPort();
                    if (port == PORT1) {
                        Thread requestThread = new Thread(new HttpRequest(socket));
                        requestThread.start();
                    } else if (port == PORT2) {
                        Thread MovedRequestThread = new Thread(new MovedRequest(socket));
                        MovedRequestThread.start();
                    }
                }
                
                keyIterator.remove();
            }
        }
    }
}

// Include your HttpRequest and MovedRequest classes here.



final class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    //Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    @Override
    public void run() {
        try {
            processRequest();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private void processRequest() throws Exception {
        // Get a reference to the socket's input and output streams.
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        //Set up input stream filters
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        //Get the request line of the HTTP request message
        String requestLine = br.readLine();

        //Display the request line
        System.out.println();
        System.out.println(requestLine);

        //Extract the filename from the request line
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); //skip over the method, which should be "GET"
        String fileName = tokens.nextToken();

        //Prepend a "." so that the file request is within the current directory.
        fileName = "." + fileName;

        //Open the requested file
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            //if the file doesn't exist, set the flag to false
            fileExists = false;
        }

        //Construct the response message
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;

        if(fileExists) {
            //set the status line for an existing file
            statusLine = "HTTP/1.1 200 OK" + CRLF; //sets to 200 to indicate succesful response
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        } else {
            //Set the status line for a non-existing file(404 not found)
            statusLine = "HTTP/1.1 404 Not Found" + CRLF;
            contentTypeLine = "Content-type: text/html" + CRLF;
            entityBody = "<HTML>" +
                    "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                    "<BODY>Not Found</BODY></HTML>";
        }

        //send the status line
        os.writeBytes(statusLine);

        //send the content type line to client
        os.writeBytes(contentTypeLine);

        //send a blank line to indicate the end of the header lines
        os.writeBytes(CRLF);

        //send the entity body
        if(fileExists) {
            sendBytes(fis, os);
            fis.close();
        } else {
            os.writeBytes(entityBody); //response body is sent to client
        }


        //Get and display the header lines.
        // String headerLine = null;
        // while ((headerLine = br.readLine()).length() != 0) {
        //     System.out.println(headerLine);
        // }

        // Close streams and socket.
        os.close();
        br.close();
        socket.close();

        
    }

    //send the requested file to the client
    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    //Determine the MIME type based on the file extension
    private static String contentType(String fileName) {
        if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "applicaton/octet-stream";
        }
    }

}

// New class to handle redirect requests
final class MovedRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public MovedRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    @Override
    public void run() {
        try {
            processRequest1();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

private void processRequest1() throws Exception {
    // We are going to construct a minimal HTTP response.
    OutputStream os = socket.getOutputStream();
    PrintWriter out = new PrintWriter(os);

    // Send a permanent redirect response
    out.print("HTTP/1.1 301 Moved Permanently" + CRLF);
    out.print("Location: http://www.google.com" + CRLF);
    out.print(CRLF); // Important, signifies end of message header

    out.flush();
    out.close();
    socket.close();
}
}





