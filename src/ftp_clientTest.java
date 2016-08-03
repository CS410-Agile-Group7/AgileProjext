import static org.junit.Assert.*;
import java.io.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.lang.Process;
import java.lang.Runtime;

public class ftp_clientTest {
  ftp_client client;
  String localDir;
  
  PrintStream dummy = new PrintStream(new ByteArrayOutputStream());
  PrintStream consoleOut = new PrintStream(new FileOutputStream(FileDescriptor.out));
  
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
  }
  
  @Test
  public void testConnect() {
    System.out.println("Testing Login");
    //redirect output to our dummy stream
    System.setOut(dummy);
    
    ftp_client tempClient = new ftp_client();
    
    assertFalse(tempClient.connect(null)); //null
    assertFalse(tempClient.connect(new String[0])); //blank
    assertFalse(tempClient.connect("SOS HELP!".split(" "))); //too short
    assertFalse(tempClient.connect("Help, WTF do I put here?".split(" "))); //malformed
    assertFalse(tempClient.connect("-p 7777 -s localhost BadPW user".split(" "))); //bad PW

    //Good
    assertTrue(tempClient.connect("-p 7777 -s localhost password user".split(" ")));
    assertTrue(tempClient.connect("-s localhost -p 7777 password user".split(" ")));
    
    tempClient.logout();
    //restore the output stream
    System.setOut(consoleOut);
  }
  
  @Test
  public void listLocal() {    
    System.out.println("Testing List Local");
    
    //redirect output to our dummy stream
    System.setOut(dummy);
    
    //list local current directory
    assertTrue(ftp_client.listLocal(localDir));
    //list local dirrectory one up
    assertTrue(ftp_client.listLocal(localDir.concat("/..")));
    //list local root directory
    assertTrue(ftp_client.listLocal("/"));
    //list directory that doesn't work -- should return FALSE
    assertFalse(ftp_client.listLocal(localDir.concat("asdfasdfasdfadf")));
    
    //restore the output stream
    System.setOut(consoleOut);
  }

  @Test
  public void listRemote() {    
    System.out.println("Testing List Remote");
    
    //redirect output to our dummy stream
    System.setOut(dummy);
    
    connect();
    //list local current directory
    assertTrue(ftp_client.listRemote("/"));
    assertTrue(ftp_client.listRemote("/fakeDir/"));
    
    //restore the output stream
    System.setOut(consoleOut);
  }

  @After
  public void shutdown() {
    if(ftpServer != null) {
      ftpServer.destroy();
    }
  }
  
  //Connects for tests that need to be connected
  public void connect() {
    client.connect("-p 7777 -s localhost password user".split(" "));
  }
}