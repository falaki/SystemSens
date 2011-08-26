package edu.ucla.cens.systemsens.util;

public class HashPrinter
{
	public static String hashString(byte[] byteKey)
	{
		StringBuffer hashAddr = new StringBuffer();
		for (int i=0; i < byteKey.length; i++) 
		{
			String hex = Integer.toHexString(0xFF & byteKey[i]);
			if (hex.length() == 1) // hexString won't preserve 0 in 0000xxxx
				hashAddr.append("0");
			hashAddr.append(hex);
		}
		return hashAddr.toString();
	}
	
}
