package misc;

import java.io.FileInputStream;

import org.springframework.security.core.codec.Base64;

import de.fraunhofer.igd.klarschiff.util.StreamUtil;

public class BackendServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		byte[] image = StreamUtil.readInputStream(new FileInputStream("c:/audersch/_java/Klarschiff/klarschiff.webapp/src_test/misc/backendServiceTest.jpg"));
		image = Base64.encode(image);
		System.out.println(new String(image));
	}

}
