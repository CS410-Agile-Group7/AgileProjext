import java.io.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class ftp_client {
  private static Console console = System.console();
  private static FTPClient ftpClient = new FTPClient();
  
  private static void setupFTPClient(String[] args) {
    String server;
    int port = 21;
    String username;
    String password;
    
    //shouldn't be more than 3 args - error
    if(args.length > 3)
      System.out.println("RTFM!"); //Yes, I know there is no manual
    
    //shouldn't be fewer than 1 args - error
    if(args.length < 1) {
      System.out.print("Server: ");
      server = console.readLine();
    }
    //otherwise procede
    //server name is the first arg
    else
      server = args[0];
    
    //if not provided...
    if(args.length < 2) {
      //get username from prompt
      System.out.print("Username: ");
      username = console.readLine();
    }
    else
      //othewise get username from args provided
      username = args[1];
    
    //if not provided...
    if(args.length < 3) {
      //get username from prompt
      System.out.print("Password: ");
      password = new String(console.readPassword());
    }
    else
      //otherwise get username from args provided
      password = args[2];
    
    try {
      //try connecting
      ftpClient.connect(server, port);
      showServerReply(ftpClient);

      //couldn't connect - error
      if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
        System.out.println("Could not connect");
        System.exit(1);
      }

      //try logging in
      boolean login = ftpClient.login(username, password);
      showServerReply(ftpClient);
      if(!login) {
        System.out.println("Could not login to the server");
        System.exit(1);
      }
      else
        showServerReply(ftpClient);
    }
    catch(Exception ex) {
    	//error catch
      System.out.println("Something went wrong :(");
    }
  }
  
  //get a reply from the server
  private static void showServerReply(FTPClient ftpClient) {
    String[] replies = ftpClient.getReplyStrings();
    //if there are messages, display each one in order
    if (replies != null && replies.length > 0)
      for (String reply : replies)
        System.out.println("SERVER: " + reply);
  }
  
  public static void main(String[] args) {
    System.out.println("Hello World!");
    setupFTPClient(args);
    
    //Look at me I am logged in or something!
    
    if(ftpClient.isConnected()) {
      try {
        ftpClient.logout();
        ftpClient.disconnect();
      }
      catch(Exception ex) {
        System.out.println("Something went wrong :(");
      }
    }
    System.exit(0);
  }
}