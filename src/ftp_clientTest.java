import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.After;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Process;
import java.lang.Runtime;
import java.net.SocketException;

public class ftp_clientTest {
  ftp_client client;
  FTPClient testClient = new FTPClient();
  String localDir;
  
  PrintStream dummy = new PrintStream(new ByteArrayOutputStream());
  //PrintStream consoleOut = new PrintStream(new FileOutputStream(FileDescriptor.out)); -- geting an error on this
  
  Process ftpServer;
  
  @Before
  public void setup() {
    localDir = System.getProperty("user.dir");

    //Setup a local FTP client for testing
    try {
      Runtime rt = Runtime.getRuntime();
      ftpServer = rt.exec("sfk-windows.exe ftpserv -port=7777 -user=user -pw=password -rw");
    } 
    catch (Exception ex) {
      System.out.println("Failed to setup local FTP");
      ex.printStackTrace();
    }
    
  	connect();
    
  }
  
  @Test
  public void connectTest() {
    System.out.println("Testing Login");
    //redirect output to our dummy stream
    System.setOut(dummy);
    
    ftp_client tempClient = new ftp_client();
    
    assertFalse(ftp_client.connect(null)); //null
    assertFalse(ftp_client.connect(new String[0])); //blank
    assertFalse(ftp_client.connect("SOS HELP!".split(" "))); //too short
    assertFalse(ftp_client.connect("Help, WTF do I put here?".split(" "))); //malformed
    assertFalse(ftp_client.connect("-p 7777 -s localhost BadPW user".split(" "))); //bad PW
    //Good
    assertTrue(ftp_client.connect("-p 7777 -s localhost password user".split(" ")));
    assertTrue(ftp_client.connect("-s localhost -p 7777 password user".split(" ")));
    
    ftp_client.logout();
    //restore the output stream
    //System.setOut(consoleOut);
  }
  
  @Test
  public void listLocalTest() {    
    System.out.println("Testing List Local");
    
    //redirect output to our dummy stream
    //System.setOut(dummy);
    
    //list local current directory
    assertTrue(ftp_client.listLocal(localDir));
    //list local dirrectory one up
    assertTrue(ftp_client.listLocal(localDir.concat("/..")));
    //list local root directory
    assertTrue(ftp_client.listLocal("/"));
    //list directory that doesn't work -- should return FALSE
    assertFalse(ftp_client.listLocal(localDir.concat("asdfasdfasdfadf")));
    
    //restore the output stream
    //System.setOut(consoleOut);
  }

  @Test
  public void listRemoteTest() {    
    System.out.println("Testing List Remote");
    
    //redirect output to our dummy stream
    //System.setOut(dummy);

    //list local current directory
    assertTrue(ftp_client.listRemote(""));
    
    //restore the output stream
    //System.setOut(consoleOut);
  }
  
  @Test
  public void createDirectoryTest() {
  	System.out.println("Testing Create Directory");
  	
  	
  	//delete newDir if it exists already
  	FTPFile[] dirs = null;
		try {
			dirs = testClient.listDirectories();
			for (FTPFile i: dirs) {
				if(i.getName().equals("newDir"))
					testClient.removeDirectory("newDir");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  	
  	assertTrue(ftp_client.createDirectory("newDir")); //try making a new dir
  	//get list of directories and check if any of them are "newDir"
  	boolean dirExists = false;
		try {
			dirs = testClient.listDirectories();
			for (FTPFile i: dirs) {
				if(i.getName().equals("newDir"))
					dirExists = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			fail();
		}
  	assertTrue(dirExists);	//if the directory was found in the previous loop, this should be true
  }
  
  @Test
  public void removeDirectoryTest() {
  		System.out.println("Testing Remove Directory");
  	
  	//cannot test - requires user input
  	//TODO - edit removeDirectory to use args and not user input
  }
  
  @Test
  public void putFileTest() {
    
  	System.out.println("Testing Put File");

  	//make sure the file doesn't already exist
		try {
			FTPFile[] remoteFile = testClient.listFiles("test.txt");
			//if the file is found, delete it
			if(remoteFile.length == 1)
				testClient.deleteFile("test.txt");

			//now that that the test file is guaranteed not to exist...
			//...put the file
			ftp_client.putFile(localDir, "test.txt");
			//check it exists
		
			remoteFile = testClient.listFiles("test.txt");
			assertTrue(remoteFile.length == 1);							//file should be found
			
		} catch (IOException e) {
			//e.printStackTrace();
			fail();
		}
  }
  

  @After
  public void shutdown() {
    if(ftpServer != null) {
      ftpServer.destroy();
    }
  }
  
  //Connects for tests that need to be connected
  public void connect() {
    ftp_client.connect("-p 7777 -s localhost password user".split(" "));
    try {
			testClient.connect("localhost", 7777);
			testClient.login("user", "password");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
}