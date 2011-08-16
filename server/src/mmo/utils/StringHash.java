package mmo.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringHash {

	public static String stringToSHA1(String s) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("sha1");
		
		md.update(s.getBytes());
		byte[] out = md.digest();
		return bytesToHex(out);
	}

	private static String bytesToHex(byte[] b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
}
