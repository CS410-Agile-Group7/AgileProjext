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
    
    if(args.length > 3)
      System.out.println("RTFM!"); //Yes, I know there is no manual
    
    if(args.length < 1) {
      System.out.print("Server: ");
      server = console.readLine();
    }
    else
      server = args[0];
    
    if(args.length < 2) {
      System.out.print("Username: ");
      username = console.readLine();
    }
    else
      username = args[1];
    
    if(args.length < 3) {
      System.out.print("Password: ");
      password = new String(console.readPassword());
    }
    else
      password = args[2];
    
    try {
      ftpClient.connect(server, port);
      showServerReply(ftpClient);

      if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
        System.out.println("Could not connect");
        System.exit(1);
      }

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
      System.out.println("Something went wrong :(");
    }
  }
  
  private static void showServerReply(FTPClient ftpClient) {
    String[] replies = ftpClient.getReplyStrings();
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