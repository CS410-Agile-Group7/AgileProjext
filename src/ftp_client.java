import java.io.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import java.util.*;

public class ftp_client {
  private static Console console = System.console();
  private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
  private static FTPClient ftpClient = new FTPClient();
  private static FileInputStream fileInputStream = null;

  public static boolean isInteger(String input)
  {
     try {
        Integer.parseInt(input);
        return true;
     } catch(NumberFormatException e) {
        return false;
     }
  }

  // this function is part of the task to handle server and port for command line and a connection command
  // it is handed an array of args that look like this
  // -p [port] -s [server] [password] [user]
  // the -p and -s may appear in any order such that port follows -p and the same with server
  // PARAMETERS:
  //    - array of strings
  // OUTPUT:
  //    - true if the information is correct and the connection can be made
  //    - false if the information is not correct or the connection cannot be done
  public static boolean connect(String[] inputs_) {
    if (inputs_ == null || inputs_.length <= 5) {
      return false;
    }

    // parse the input into the correct locations
    String port   = "";
    String server = "";

    if (inputs_[0].equals("-p")) {
      if (inputs_[2].equals("-s")) {
        port = inputs_[1];
        server = inputs_[3];
      } else {
        return false;
      }
    } else if (inputs_[0].equals("-s")) {
      if (inputs_[2].equals("-p")) {
        port = inputs_[3];
        server = inputs_[1];
      } else {
        return false;
      }
    } else {
      return false;
    }

    if (!isInteger(port)) {
      return false;
    }

    int int_port = Integer.parseInt(port);
    String pass = inputs_[4];
    String user = inputs_[5];

    // try to connect
    // private static boolean login(String server, int port, String user, String password)
    if (!login(server, int_port, user, pass)) {
      return false;
    } else {
      if (inputs_.length > 6) {

      } else {
        return true;
      }
    }

    // automation for features comes after here
    // to do a get or put
    // --GetFile= <file name>
    // --PutFile= <file name>
    // lists can exist
    // --GetFiles= <file 1> <file 2>
    // no other automations can follow a list

    int idx = 6;

    if (inputs_[idx].equals("--GetFile=")) {
      // run a get file
      /*
      if (!getFile_args(inputs_[idx + 1])) {
        System.out.println("there was an error with your file get");
        return true;
      }
      */
      System.out.println("this feature does not exist");
      return true;
    } else if (inputs_[idx].equals("--PutFile=")) {
      if (!putFile_args(inputs_[idx + 1])) {
        System.out.println("there was an error with your file get");
        return true;
      }
    } else if(inputs_[idx].equals("--GetFiles=")) {
      /*
      */
      System.out.println("this feature does not exist");
    } else if(inputs_[idx].equals("--PutFiles=")) {
      // acquire array
      String[] inputs__ = Arrays.copyOfRange(inputs_, idx + 1, inputs_.length - 1);
      putFile_args_m(inputs__);
    } else {
      System.out.println("invalid argument");
      return true;
    }
    return true;
  }

  public static void main(String[] args) {
    //setupFTPClient(args);
    if (args.length == 0) {
      System.out.println("enter connection info: ");
      Scanner input = new Scanner ( System.in );
      String input_str = input.nextLine();
      String[] input_arr = input_str.split(" ");
      if (!connect(input_arr)) {
        System.out.println("there was a problem with your connection");
        System.exit(0);
      }
    } else {
      if (!connect(args)) {
        System.out.println("there was a problem with your connection");
        System.exit(0);
      }
    }
    String command;
    String localDir = System.getProperty("user.dir");

    while(ftpClient.isConnected()) {
      command = getVar("command");
      switch(command) {
      	case "list remote":
      	case "lsr":
      		listRemote(null);
      	break;

        case "list local":
        case "lsl":
          listLocal(localDir);
        break;

        case "make directory":
        case "mk dir":
        case "mkdir":
          createDirectory();
          break;

        case "delete directory":
        case "delete dir":
        case "rm dir":
        case "rmd":
        case "remove directory":
          removeDirectory();
          break;


        case "delete file":
        case "rm file":
        case "remove file":
        case "rmf":
          removeFile();
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
        	//change directory - cd local/cdl
          if(command.matches("cd local (.*)") || command.matches("cdl (.*)")) {
            localDir = changeDirectory(localDir, command);
          }
          else
          //put file - put/p
          if(command.matches("put (.*)") || command.matches("p (.*)")) {
          	//put the given file to the remote server
          	putFile(localDir, command);
          } else
          //put multiple putm/pm
          if(command.matches("putm (.*)") || command.matches("pm (.*)")) {
          	putMultiple(localDir, command);
          } else {
            System.out.println("\tCommand not found. For help input \"help\"");
          }
      }
    }

    logout();
    System.exit(0);
  }

  /***** Command Functions *****/

  //Creates directory on remote server, verifying that the directory doesn't already exist first
  private static boolean createDirectory() {
    try {
      int returnCode;
      Scanner in = new Scanner(System.in);
      String toCreate = getVar("Please enter the directory name");
      do {
        ftpClient.changeWorkingDirectory("/" + toCreate); //Tries to see if that directory alraedy exists
        returnCode = ftpClient.getReplyCode();
        //If the directory exists, then the reply code is unavailable (giving a return code other than 550)
        if(returnCode != 550) {
          System.out.println("The directory '" + toCreate + "' already exists.");
          System.out.println("Do you want to enter another directory? (Y/N)");
          char reply = in.next().charAt(0);
          in.nextLine();
          reply = Character.toUpperCase(reply);
          if(reply == 'Y') {
            toCreate = getVar("Please enter another directory name");
          }
          else {
            ftpClient.changeWorkingDirectory("/");
            return false;
          }
        }
      } while(returnCode != 550); //Prompts user for another directory name if the one they enter already exists
      ftpClient.changeWorkingDirectory("/");
      boolean success = ftpClient.makeDirectory(toCreate);
      //showServerReply(ftpClient);
      if (success) {
        System.out.println("Successfully created directory: " + toCreate);
        return true;
      } else {
        System.out.println("Failed to create directory: " + toCreate);
        return false;
      }
    } catch (IOException ex) {
      System.out.println("Something went wrong. Failed to create directory.");
      ex.printStackTrace();
      return false;
    }
  }

  //Removes directory if it is empty
  private static boolean removeDirectory() {
    try {
      Scanner in = new Scanner(System.in);
      int returnCode;
      String toRemove = getVar("Please enter the directory to delete");
      do {
      ftpClient.changeWorkingDirectory("/" + toRemove);
      returnCode = ftpClient.getReplyCode();
        //Return code is 550 if the directory we tried to change into doesn't exist
      if(returnCode == 550) {
        System.out.println("The directory '" + toRemove + "' doesn't exist.");
        System.out.println("Do you want to enter another directory? (Y/N)");
        char reply = in.next().charAt(0);
        in.nextLine();
        reply = Character.toUpperCase(reply);
        if (reply == 'Y') {
          toRemove = getVar("Please enter another directory to delete");
        }
        else
          return false;
      }
      }while(returnCode == 550);
      ftpClient.changeWorkingDirectory("/");
      boolean deleted = ftpClient.removeDirectory("/" + toRemove);
      if(deleted){
        System.out.println("The directory " + toRemove + " was removed");
        return true;
      }
      else
      {
       System.out.println("Couldn't delete directory. It may not be empty.");
        return false;
      }
    }catch(IOException ex){
      System.out.println("There was an error.");
      return false;
    }
    }

  //Remove file from server
  private static boolean removeFile() {
    try {
      Scanner in = new Scanner(System.in);
      int returnCode;
      InputStream inputStream;
      String toDelete = getVar("File to delete (with extension and path if file is in subdirectory)");
      do {
        ftpClient.changeWorkingDirectory("/");
        inputStream = ftpClient.retrieveFileStream(toDelete);
        returnCode = ftpClient.getReplyCode();
        //Return code is 550 if the directory we tried to change into doesn't exist
        if (inputStream == null || returnCode == 550) {
          System.out.println(toDelete + " is not a valid file. Could not delete.");
          System.out.println("Do you want to enter another file? (Y/N)");
          char reply = in.next().charAt(0);
          in.nextLine();
          reply = Character.toUpperCase(reply);
          if (reply == 'Y') {
            toDelete = getVar("Please enter another file to delete");
          } else
            return false;
        }
      } while (inputStream == null || returnCode == 550);
      boolean wasDeleted = ftpClient.deleteFile(toDelete);
      ftpClient.changeWorkingDirectory("/");
      if(wasDeleted){
        System.out.println(toDelete + " was successfully deleted.");
        return true;
      }
      else{
        System.out.println("Couldn't delete " + toDelete);
        return false;
      }
    } catch (IOException ex) {
      System.out.println("There was an error.");
      return false;
    }
  }


  //Puts all specified files from lcoal server to specified location on the remote server
  private static boolean putMultiple(String localDir, String input) {
  	//The file to put
    File file;
  	String outFile;

  	//remove "put"/"p" from input, get rid of blank spaces
  	input = input.replace("putm","");
    input = input.replace("pm","");
    input = input.trim();
    //split on whitespaces
    String[] inputList = input.split(" "); //need to change, should match more than one whitespace
    //put file for each file
    for(String i: inputList) {
    	i = "put".concat(i);
    	//if any of the putFiles go wrong, return false
    	if(putFile(localDir, i) == false)
    		return false;
    }
    //if all putFiles go correctly, return true
    return true;
  }

  //Puts the specified file from the local server to the specified location on the remote server
  protected static boolean putFile(String localDir, String input) {
  	//The file to put, the location to put
    File file;
  	String outFile;
  	//remove "put"/"p" from input, get rid of blank spaces
  	input = input.replace("put","");
    input = input.replace("p","");
    input = input.trim();
    //split on whitespaces
    String[] inputList = input.split(" "); //need to change, should match more than one whitespace
    //get the provided file
    outFile = localDir;
    outFile = outFile.concat("/");
    outFile = outFile.concat(inputList[0]);
    file = new File(outFile);
    //check the file actually exists
    try {
      if (file.exists()) {
        fileInputStream = new FileInputStream(outFile);
        ftpClient.storeFile(outFile, fileInputStream);
        //verify the file is there
        FTPFile[] remoteFile = ftpClient.listFiles(inputList[0]);
        if(remoteFile.length == 1)
        	return true;
        else
        	return false;
      }
      else {
        System.out.println("File not found :(");
        return false;
      }
    }
    catch(Exception e) {
      System.out.println("Something went wrong :(");
      return false;
    }
  }

  public static boolean putFile_args_m(String [] names) {
    for(String name : names) {
      putFile_args(name);
    }

    return true;
  }

  public static boolean putFile_args(String name) {
    File file;
    String outFile;
    String[] inputList = name.split(" ");
    String localDir = System.getProperty("user.dir");

    outFile = localDir;
    outFile = outFile.concat("/");
    outFile = outFile.concat(inputList[0]);
    file = new File(outFile);

    try {
      if (file.exists()) {
        //if so, put the file out
        fileInputStream = new FileInputStream(outFile);
        ftpClient.storeFile(outFile, fileInputStream);
        //verify the file is there
        FTPFile[] remoteFile = ftpClient.listFiles(inputList[0]);
        if(remoteFile.length == 1)
          return true;
        else
          return false;
        }
    } catch (Exception e) {
      System.out.println("Something went wrong :(");
      return false;
    }

    return true;
  }

  //Lists the remote files/folders in the provided directory
  static boolean listRemote(String dir) {
  	try {
			FTPFile[] fileList = ftpClient.listFiles(dir);

			//get the full path before printing
			dir = ftpClient.getLocalAddress().toString();
			System.out.println("Remote Directory: " + dir);
      for (int i = 0; i < fileList.length; ++i) {
        if(fileList[i].isFile()) {
          System.out.println("\t" + fileList[i].getName());
        }
        else if(fileList[i].isDirectory()) {
          System.out.println("\t" + fileList[i].getName());
        }
      }
		} catch (IOException e) {
			return false;
			//e.printStackTrace();
		}
		return true;
  }

  //Lists the local files/folders in the provided directory
  protected static boolean listLocal(String dir) {
    System.out.println();
    try {
      File folder = new File(dir);
      File[] fileList = folder.listFiles();
      System.out.println("Current Directory: " + dir);
      for (int i = 0; i < fileList.length; ++i) {
        if(fileList[i].isFile()) {
          System.out.println("\t" + fileList[i].getName());
        }
        else if(fileList[i].isDirectory()) {
          System.out.println("\t" + fileList[i].getName());
        }
      }
      System.out.println();
    }
    catch(Exception e) {
      return false;
    }
    return true;
  }

  private static String changeDirectory(String dir, String input) {
    input = input.replace("cdl","");
    input = input.replace("cd local","");
    input = input.trim();
    File newDir = new File(dir, input);
    try {
      if (newDir.exists()) {
        System.out.println("Changed Directory to: " + newDir.toPath().normalize().toString());
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
    System.out.print("\033[H\033[2J");  //clear then home
    System.out.flush();
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

  static boolean logout() {
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
  //Bugfix for eclipse?
  private static String getVar(String request) {
    System.out.print(request + ": ");
    if(console != null) {
      return console.readLine();
    }
    else {
      try {
        return in.readLine();
      }
      catch(Exception ex) {
        return null;
      }
    }
  }

  //Prompts user and returns a string w/o outputing it
  //Does not work in eclipse so redirects to getVar()
  private static String getPrivateVar(String request) {
	  if(console != null) {
      System.out.print(request + ": ");
      return new String(console.readPassword());
    }
    else
      return getVar(request);
  }
}
