import org.junit.Test;

public class ftp_clientTest {

	//Setup client
	ftp_client client = new ftp_client();
	//server info
	String server = "ftp.sysnative.com";
  String username = "pdx@sysnative.com";
  String password = "P455w0rd!";
  //create args
  String args[] = {server, username, password};
	
	@Test
	public void correctArgsTest() {    
    //call setup
		client.main(args);	
	}
	
	@Test
	public void incorrectClientTest() {
		//incorrect servers
		server = "wrong_server.com";
	  //create args
	  String args[] = {server, username, password};
	  client.main(args);
	}

}
