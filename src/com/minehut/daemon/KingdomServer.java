package com.minehut.daemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class KingdomServer extends Thread {
	
	private int id = -1;
	
	InputStream is;
	
	ProcessBuilder slave;
	
	Process theProcess;
	
	PrintWriter writer;
	
	public KingdomServer(int id) {
		this.id = id;
	}
	
	public void runCommand(String cmd) {
		try {
			this.writer.println(cmd);
			this.writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		if (id==-1) 
			return;
		try {
			System.out.println("Starting server..");
			
			this.slave = new ProcessBuilder("slave" + this.id);
			this.theProcess = this.slave.start();
			
			this.writer = new PrintWriter(new OutputStreamWriter(this.theProcess.getOutputStream()));
			is = this.theProcess.getInputStream();
			
			System.out.println("Server started, getting output");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("server closed, thread finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
