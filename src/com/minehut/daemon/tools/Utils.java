package com.minehut.daemon.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Utils {
	
	public Utils() { }
	
	public static ArrayList<String> tokens = new ArrayList<String>(Arrays.asList("6WGFmOYQByGlXMS"));

	public static boolean checkToken(String token) {
		return tokens.contains(token);
	}
	
	public static String getToken() {
		return "SECRETKEY";
	}
	
	public void logLine(LogType type, String line) {
		System.out.println(
				"[" + type.getType() + "] " + 
				"[" + new Date() + "] " +
				/*"[" + this.webServer.getSocket().getInetAddress().getHostAddress() + ":" + this.webServer.getSocket().getPort() + "] " +*/
				line);
		//TODO: Log to file based on LogType. Use switch?
	}
	
}
