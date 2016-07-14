import org.junit.Before;
import org.junit.Test;

public class ftp_clientTest {

	ftp_client client;
	
	@Before
	public void setup() {
		//Setup client
		client = new ftp_client();
    //call main
		client.main(null);
	}
	
	@Test
	public void listLocal() {    

	}

}
