import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ftp_clientTest {

	ftp_client client;
	String localDir;
	
	@Before
	public void setup() {
		//Setup client
		localDir = System.getProperty("user.dir");
	}
	
	@Test
	public void listLocal() {    
		//list local current directory
		assertTrue(ftp_client.listLocal(localDir));
		//list local dirrectory one up
		assertTrue(ftp_client.listLocal(localDir.concat("/..")));
		//list local root directory
		assertTrue(ftp_client.listLocal("/"));
		//list directory that doesn't work -- should return FALSE
		assertFalse(ftp_client.listLocal(localDir.concat("asdfasdfasdfadf")));
	}

}
