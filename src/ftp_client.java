import java.io.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

//For pretty colors
import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public class ftp_client {
  private static Console console = System.console();
  private static FTPClient ftpClient = new FTPClient();
  
  public static void main(String[] args) {
    setupFTPClient(args);
    String command;
    String localDir = System.getProperty("user.dir");
    
    while(ftpClient.isConnected()) {
      command = getVar("command");
      switch(command) {
        case "list local":
        case "lsl":
          listLocal(localDir);
        break;
        
        case "exit":
        case "logout":
          exit();
        break;
        
        case "help":
          help();
        break;
        
        case "cls":
        case "clear":
          clear();
        break;
        
        default:
          //REGEX Matching
          if(command.matches("cd local (.*)") || command.matches("cdl (.*)")) {
            localDir = changeDirectory(localDir, command);
          }
          else {
            System.out.println("\tCommand not found. For help input \"help\"");
          }
      }
    }
    
    logout();
    System.exit(0);
  }
  
  /***** Command Functions *****/
  
  //Lists the local files/folders in the provided directory
  private static boolean listLocal(String dir) {
    AnsiConsole.systemInstall();
    System.out.println();
    try {
      File folder = new File(dir);
      File[] fileList = folder.listFiles();
      System.out.println(ansi().fg(MAGENTA) + "Current Directory: " + ansi().fg(DEFAULT) + dir);
      for (int i = 0; i < fileList.length; ++i) {
        if(fileList[i].isFile()) {
          System.out.println("\t" + ansi().fg(CYAN) + fileList[i].getName());
        }
        else if(fileList[i].isDirectory()) {
          System.out.println("\t" + ansi().fg(BLUE) + fileList[i].getName());
        }
      }
      System.out.println(ansi().fg(DEFAULT));
    }
    catch(Exception e) {
      AnsiConsole.systemUninstall();
      return false;
    }
    AnsiConsole.systemUninstall();
    return true;
  }
  
  private static String changeDirectory(String dir, String input) {
    input = input.replace("cdl","");
    input = input.replace("cd local","");
    input = input.trim();
    File newDir = new File(dir, input);
    try {
      if (newDir.exists()) {
        System.out.println("New Dir: " + newDir.toPath().normalize().toString());
        return newDir.toPath().normalize().toString();
      }
      else {
        System.out.println("Folder not found :(");
        return dir;
      }
    }
    catch(Exception e) {
      System.out.println("Something went wrong :(");
      return dir;
    }
  }
  
  //Prints the help menu
  private static void help() {
    System.out.println("RTFM"); //fix me
    System.out.println();
  }
  
  //Clears the console
  private static void clear() {
    AnsiConsole.systemInstall();
    System.out.println(ansi().eraseScreen());
    AnsiConsole.systemUninstall();
  }
  
  //Logoff and exit
  private static void exit() {
    logout();
  }
  
  /***** Utility Functions *****/
  
  //gets reply from the server
  private static void showServerReply(FTPClient ftpClient) {
    String[] replies = ftpClient.getReplyStrings();
    //if there are messages, display each one in order
    if (replies != null && replies.length > 0)
      for (String reply : replies)
        System.out.println("SERVER: " + reply);
  }
  
  //Gets input from user or args and calls login
  private static void setupFTPClient(String[] args) {	
    //shouldn't be more than 3 args - error
    if(args.length > 3)
    System.out.println("RTFM!"); //Yes, I know there is no manual
    
    String server = (args.length < 1) ? getVar("Server") : args[0];
    int port = 21;
    String username  = (args.length < 2) ? getVar("User") : args[1];
    String password  = (args.length < 3) ? getPrivateVar("Password") : args[2];
	
    login(server,port,username,password);
  }
  
  private static boolean login(String server, int port, String user, String password) {
    try {
      ftpClient.connect(server, port);
      showServerReply(ftpClient);

      if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
        System.out.println("Could not connect");
        return false;
      }

      return ftpClient.login(user, password);
    }
    catch(Exception ex) {
      System.out.println("Something went wrong :( - couldn't connect to the server");
	  return false;
    }
  }
  
  private static boolean logout() {
    if(ftpClient.isConnected()) {
      try {
      	System.out.println("Logging out...");
        ftpClient.logout();
        ftpClient.disconnect();
        System.out.println("Succesfully Disconnected");
        return true;
      }
      catch(Exception ex) {
        System.out.println("Something went wrong :( - Couldn't logout and disconnect correctly");
        return false;
      }
    }
    return false;
  }
  
  //Prompts user and returns a string
  private static String getVar(String request) {
    System.out.print(request + ": ");
    return console.readLine();
  }
  
  //Prompts user and returns a string w/o outputing it
  private static String getPrivateVar(String request) {
	  System.out.print(request + ": ");
	  return new String(console.readPassword());
  }
}