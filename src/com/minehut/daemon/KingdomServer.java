package com.minehut.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import com.minehut.daemon.tools.FileUtil;

public class KingdomServer extends Thread {
	
	private int id = -1, port = -1;
	
	public String startup = "0%"; //TODO: startup = 73%
	
	public Kingdom kingdom;
	
	public ServerState state;
	
	private Tailer tailer;
	private Thread tailerThread;
	private File log;
	
	
	public boolean old = false;

	private long previousListSendTime = 0L;

	public int playerCount = 0;
	public String motd = ""; //todo
	public int maxPlayers = 10; //todo
	
	public enum ServerState {
		SHUTDOWN, RUNNING, STARTING, CRASHED;
	}
	
	public KingdomServer(Kingdom kingdom, int id) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;

		this.previousListSendTime = System.currentTimeMillis();
	}
	
	public KingdomServer(Kingdom kingdom, int id, boolean old) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;
		this.old = old;

		this.previousListSendTime = System.currentTimeMillis();
	}
	
	public int getID() {
		return this.id;
	}
	
	public int getPort() {
		return port;
	}
	
	
	private long lastTick = 0L;
	
	private int tailerDelay = 0;
	private boolean tailerStarted = false;
	
	
	@Override
	public void run() {
		if (id==-1) 
			return;
		
		try {
			this.state = ServerState.STARTING;
			FileUtil.editServerProperties(this.kingdom, this.port);

			log = new File("/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id + "/screenlog.0");
			
			
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "screen -dmLS kingdom" + this.id);
			pb.directory(new File("/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id));
			pb.start().waitFor();
			
			//String kdDir = "/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id;
			
			//this.sendScreenCommand("cd " + kdDir).waitFor();
			this.sendScreenCommand("java -XX:MaxPermSize=128M -Xmx768M -Xms768M -jar spigot.jar nogui").waitFor();
			
			//this.sendScreenCommand("echo kingdom")
			
			this.state = ServerState.RUNNING;
			
			System.out.println("Kingdom started: kingdom" + this.id + " port: " + this.port);
			
			
			FileWriter writer = new FileWriter(log);
			writer.write("STARTING KINGDOM - (KINGDOM DAEMON v4.2.0)\n");
			writer.write("kingdomID:" + this.id + "\n");//TODO: Daemon will use this to grab kingdom data after restarting
			writer.close();
			
			
			
		    
		    while (true) {
		    	if (this.lastTick == 0)
		    		this.lastTick = System.currentTimeMillis();
		    	if (((System.currentTimeMillis() - this.lastTick) / 1000) >= 10) {
		    		System.out.println("KingdomServer Thread ticking at " + System.currentTimeMillis() + " for kingdom" + this.id);
		    		
		    		if (this.state == ServerState.SHUTDOWN) {
		    			System.out.println("ServerState set to shutdown, shutting down the server!");
		    			new ProcessBuilder("/bin/bash", "-c", "screen -X -S kingdom" + this.id + " quit").start().waitFor(); //Should kill the screen after the kingdom shuts down
		   			 	log.delete();
		    			break;
		    		}
		    		
		    		
		    		if (!this.tailerStarted) {
		    			if (tailerDelay == 1) {
		    				this.tailerStarted = true;
			    			TailerListener listener = new LogListener(this);
			    		    tailer = new Tailer(log, listener);
			    		    tailerThread = new Thread(tailer);
			    		    tailerThread.start();
			    		    System.out.println("Starting TailerThread, hopefully at a delay.");
			    		} else 
			    		if (tailerDelay <= 1) {
			    			tailerDelay++;
			    		}	
		    		}
		    				    		
		    		this.lastTick = System.currentTimeMillis();
		    	}
		    }
		    
		   
		    
			//TODO: upload to mongo about port?
			//TODO: open input stream to ./logs/latest.log for parsing server logs
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void parseLog(String line) {
		/* List Command Cooldown */
		
		
		if (this.previousListSendTime==0)
			this.previousListSendTime = System.currentTimeMillis();
		if (((System.currentTimeMillis() - this.previousListSendTime) / 1000) >= 3) {

			/* Retrieve Player Count */
			if (line.contains("There are ") && line.contains(" players online:")) {

				String[] firstPart = line.split("There are ");
				String[] secondPart = firstPart[1].split(" players online:");

				System.out.println("secondPart[0]:\"" + secondPart[0] + "\"");
				
				String countString[] = secondPart[0].split("//");

				System.out.println("countString[0]:" + countString[0]);
				
				int count = Integer.parseInt(countString[0]);
				//TODO: countString[1] is the max players
				this.playerCount = count;
			} else {
				if (!line.startsWith(">")) {
					this.sendScreenCommand("list");
					this.previousListSendTime = System.currentTimeMillis();
				}
			}

			/* Status Upload */
/*
			DBObject query = new BasicDBObject("name", this.kingdom.getName());
			DBObject found = KingdomsDaemon.getInstance().getServersCollection().findOne(query);

			if (found == null) {
				System.out.println("Inserting Kingdom Status: " + kingdom.getName());
				KingdomsDaemon.getInstance().getServersCollection().insert(createDBObject());
			} else {
				System.out.println("Found and updating Kingdom Status: " + kingdom.getName());
				KingdomsDaemon.getInstance().getServersCollection().findAndModify(query, createDBObject());
			}
*/
		}



		if (line.contains(" INFO]: Stopping server")) {
			this.state = ServerState.SHUTDOWN;
			System.out.println(line);
			tailerThread.interrupt();
			tailer.stop();
		} else if (line.contains(" INFO]: Done (")) {
			this.startup = "100%";
			System.out.println(line);
		} else if (line.contains("FAILED TO BIND TO PORT!")) {
			this.state = ServerState.SHUTDOWN;
			System.out.println(line);
			tailerThread.interrupt();
			tailer.stop();
		} else if (line.contains("Preparing spawn area: ")) {
			String[] startupArray = line.split("Preparing spawn area: ");
			this.startup = startupArray[1];
		}
	}

	
	public class LogListener extends TailerListenerAdapter {
		KingdomServer server;
		public LogListener(KingdomServer server) {
			this.server = server;
		}
		@Override
		public void handle(String line) { //TODO: More advanced parsing, simple parsing methods for showcase
			server.parseLog(line);
		}
	}
	
	public Process sendScreenCommand(String cmd) {
		Process process = null;
		try {
			process =  new ProcessBuilder("/bin/bash", "-c", "screen -S kingdom" + this.id + " -p 0 -X stuff '" + cmd + "'^M").start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return process;
	}

	public String getMotd() {
		return motd;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public Kingdom getKingdom() {
		return kingdom;
	}
}
