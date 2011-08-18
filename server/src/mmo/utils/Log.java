package mmo.utils;

public class Log {
	
	private static boolean filter(LogMessageTypes type)
	{
		return type == LogMessageTypes.GENERAL;
	}
	
	public static void print(String message)
	{
		print(LogMessageTypes.GENERAL,message);
	}	
	
	public static void print(LogMessageTypes type, String message)
	{
		if(Log.filter(type))
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
