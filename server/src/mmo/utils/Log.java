package mmo.utils;

public class Log {
	
	public static void print(String message)
	{
		System.out.println(message);
	}	
	
	public static void error(String message)
	{
		System.out.println("Error: "+message);
	}

	public static void warning(String message)
	{
		System.out.println("Warning: "+message);
	}
}
