import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class Server implements Runnable {

  static final File WEB_ROOT = new File(".");
  static final String DEFAULT_FILE = "index.html";
  static final String FILE_NOT_FOUND = "404.html";
  static final String METHOD_NOT_SUPPORTED = "not_supported.html";

  // port connection
  static final int PORT = 8080;

  // verbose mode
  static final boolean verbose = true;

  // Client connection with socket class
  private Socket connect;

  public Server(Socket c) {
    connect = c;
  }

  public static void main(String[] args) {
    try {
      ServerSocket serverConnect = new ServerSocket(PORT);
      System.out.println("Server started.\nListening for connections on port: " + PORT + "...\n");

      // Listen until user halts server connection
      while (true) {
        Server myServer = new Server(serverConnect.accept());

        if (verbose) {
          System.out.println("Connection opened. (" + new Date() + ")");
        }

        // create dedicated thread to manage the client connection
        Thread thread = new Thread(myServer);
        thread.start();
      }

    } catch (IOException e) {
      System.out.println("Server connection error: " + e.getMessage());
    }
  }


  @Override
  public void run() {
    // Manage client connection
    BufferedReader in = null;
    PrintWriter out = null;
    BufferedOutputStream dataOut = null;
    String fileRequested = null;

    try {
      // read characters from the client via the input stream on the socket
      in = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));

      // we get the character output stream to the client for headers
      out = new PrintWriter(connect.getOutputStream());

      // get binary output stream to client for requested data
      dataOut = new BufferedOutputStream(connect.getOutputStream());

      // get first line of the request from the client
      String input = in.readLine();
      System.out.println("input " + input);
      // we parse the request with a string tokenizer
      StringTokenizer parse = new StringTokenizer(input);
      String method = parse.nextToken().toUpperCase(); // get the HTTP method of the client
      // get the file requested
      fileRequested = parse.nextToken().toLowerCase();

      // Only support GET and HEAD methods
      if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
        if (verbose) {
          System.out.println("501 Not Implemented: " + method + " method.");
        }

        // return the not supported file to the client
        File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();
        String contentMimeType = "text/html";

        // read content to return to client
        byte[] fileData = readFileData(file, fileLength);

        // send HTTP headers with data to client
        out.println("HTTP/1.1 501 Not Implemented");
        out.println("Server : Java HTTP Server from Lukas : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + contentMimeType);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();// flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

      } else {
        // GET or HEAD method
        if (fileRequested.endsWith("/")) {
          fileRequested += DEFAULT_FILE;
        }

        File file = new File(WEB_ROOT, fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        if (method.equals("GET")) { // GET Method so we return content
          byte[] fileData = readFileData(file, fileLength);

          // send HTTP headers
          out.println("HTTP/1.1 200 OK");
          out.println("Server : Java HTTP Server from Lukas : 1.0");
          out.println("Date: " + new Date());
          out.println("Content-type: " + content);
          out.println("Content-length: " + fileLength);
          out.println();
          out.flush();// flush character output stream buffer

          dataOut.write(fileData, 0, fileLength);
          dataOut.flush();

        } else if (method.equals("POST")) { // POST method
          boolean goodFile = true;

          // check the language
          if (content == "application/vnd.ms-excel") {
            // vb
            
          } else {
            goodFile = false;
          }

          if (goodFile) {
            out.println("HTTP/1.1 200 OK");
            out.println("Server : Java HTTP Server from Lukas : 1.0");
            out.println("Date: " + new Date());
            // out.println("Content-type: " + content);
            // out.println("Content-length: " + fileLength);
            out.println();
            out.flush();// flush character output stream buffer
          } else {
            out.println("HTTP/1.1 501 Not Implemented");
            out.println("Server : Java HTTP Server from Lukas : 1.0");
            out.println("Date: " + new Date());
            out.println("Content-type: " + content);
            out.println("Content-length: " + fileLength);
            out.println();
            out.flush();// flush character output stream buffer
          }
        }

        if (verbose) {
          System.out.println("File: " + fileRequested + " of type " + content + " returned");
        }
      }

    } catch (FileNotFoundException e) {
      try {
        fileNotFound(out, dataOut, fileRequested);
      } catch (IOException e1) {
        System.err.println("Error with file not found exception: " + e.getMessage());
      }
    } catch (IOException e) {
      System.err.println("Server error: " + e);
    } finally {
      try {
        in.close();
        out.close();
        dataOut.close();
        connect.close(); // close socket connection
      } catch (Exception e) {
        System.err.println("Error closing stream: " + e.getMessage());
      }

      if (verbose) {
        System.out.println("Connection closed\n");
      }
    }

  }

  private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested)
      throws IOException {
    File file = new File(WEB_ROOT, FILE_NOT_FOUND);
    int fileLength = (int) file.length();
    String content = "text/html";
    byte[] fileData = readFileData(file, fileLength);

    // send HTTP headers
    out.println("HTTP/1.1 404 File Not Found");
    out.println("Server : Java HTTP Server from Lukas : 1.0");
    out.println("Date: " + new Date());
    out.println("Content-type: " + content);
    out.println("Content-length: " + fileLength);
    out.println();
    out.flush();// flush character output stream buffer

    dataOut.write(fileData, 0, fileLength);
    dataOut.flush();

    if (verbose) {
      System.out.println("File " + fileRequested + " not found");
    }
  }

  // return supported mime types
  private String getContentType(String fileRequested) {
    if (fileRequested.endsWith(".html") || fileRequested.endsWith(".htm"))
      return "text/html";
    else if (fileRequested.endsWith(".css"))
      return "text/css";
    else if (fileRequested.endsWith(".php"))
      return "application/x-php";
    else if (fileRequested.endsWith(".aspx"))
      return "application/vnd.ms-excel";
    else
      return "text/plain";
  }

  private byte[] readFileData(File file, int fileLength) throws IOException {
    FileInputStream fileIn = null;
    byte[] fileData = new byte[fileLength];

    try {
      fileIn = new FileInputStream(file);
      fileIn.read(fileData);
    } finally {
      if (fileIn != null)
        fileIn.close();
    }

    return fileData;
  }


}
