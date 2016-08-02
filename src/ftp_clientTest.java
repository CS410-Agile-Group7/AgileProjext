import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ftp_clientTest {

	ftp_client client;
	String[] args = {"-p 21 -s sysnative.com m30wm30w! pdx"}; //edit to hold login info - MAKE SURE TO DELETE BEFORE COMMITING
	@Before
	public void setup() {
		//Setup client
		client = new ftp_client();
    //call main
		client.main(args);
	}
	
	@Test
	public void listLocal() {    
		//list local files in current folder
		assertTrue(ftp_client.listLocal(""));
		//list local files in folder one up
		assertTrue(ftp_client.listLocal(".."));
		//list local files in root folder
		assertTrue(ftp_client.listLocal("/"));
		//list local files in a folder that doesn't work -- should return FALSE
		assertFalse(ftp_client.listLocal("adfasdfasdfadf"));
	}

}
