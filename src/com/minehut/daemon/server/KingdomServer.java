package com.minehut.daemon.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.KingdomsDaemon;
import com.minehut.daemon.tools.FileUtil;

public class KingdomServer extends Thread {
	
	private int id = -1, port = -1;
	
	public String startup = "0%"; //TODO: startup = 73%
	
	public Kingdom kingdom;
	
	public ServerState state;
	
	private Tailer tailer;
	private Thread tailerThread;
	private File log;

	private KingdomServerLogListener tailerListener;
	
	public boolean old = false;

	

	public int playerCount = 0;
	public String motd = ""; //todo
	public int maxPlayers = 10; //todo
	
	public enum ServerState {
		SHUTDOWN("SHUTDOWN"), RUNNING("RUNNING"), STARTING("STARTING"), CRASHED("CRASHED");
		private String theState;
		ServerState(String state) {
			this.theState = state;
		}
		public String getState() {
			return this.theState;
		}
	}
	
	public KingdomServer(Kingdom kingdom, int id) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;
	}
	
	public KingdomServer(Kingdom kingdom, int id, boolean old) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;
		this.old = old;
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
			this.setState(ServerState.STARTING);
			FileUtil.editServerProperties(this.kingdom, this.port);

			log = new File("/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id + "/screenlog.0");
			
			
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "screen -dmLS kingdom" + this.id);
			pb.directory(new File("/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id));
			pb.start().waitFor();
			
			//String kdDir = "/home/rdillender/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id;
			
			//this.sendScreenCommand("cd " + kdDir).waitFor();
			this.sendScreenCommand("java -XX:MaxPermSize=128M -Xmx768M -Xms768M -jar spigot.jar nogui").waitFor();
			
			//this.sendScreenCommand("echo kingdom")
			
			this.setState(ServerState.RUNNING);
			
			System.out.println("Kingdom started: kingdom" + this.id + " port: " + this.port);
			
			
			FileWriter writer = new FileWriter(log);
			writer.write("STARTING KINGDOM - (KINGDOM DAEMON v4.2.0)\n");
			writer.write("kingdomID:" + this.id + "\n");//TODO: Daemon will use this to grab kingdom data after restarting
			writer.close();
			
			
			
		    
		    while (true) {
		    	if (this.lastTick == 0)
		    		this.lastTick = System.currentTimeMillis();

				if (!this.tailerStarted) {
					this.tailerStarted = true;
					this.tailerListener = new KingdomServerLogListener(this);
					tailer = new Tailer(log, tailerListener);
					tailerThread = new Thread(tailer);
					tailerThread.start();
					System.out.println("Starting TailerThread, hopefully at a delay.");
				}

				if ((this.tailerListener.getLastReadTime() - System.currentTimeMillis()) / 1000 >= 5) {
					System.out.println("Server hasn't responded for 5 seconds, executing force shutdown");
					this.setState(ServerState.SHUTDOWN);
				}

		    	if (((System.currentTimeMillis() - this.lastTick) / 1000) >= 10) {
		    		System.out.println("KingdomServer Thread ticking at " + System.currentTimeMillis() + " for kingdom" + this.id);
		    		
		    		if (this.state == ServerState.SHUTDOWN) {
		    			tailerThread.interrupt();
		    			tailer.stop();
		    			
		    			
		    			System.out.println("ServerState set to shutdown, shutting down the server!");
		    			new ProcessBuilder("/bin/bash", "-c", "screen -X -S kingdom" + this.id + " quit").start().waitFor(); //Should kill the screen after the kingdom shuts down
		   			 	log.delete();
						KingdomsDaemon.getInstance().getServers().remove(this);

						int portIndex = KingdomsDaemon.getInstance().getPorts().indexOf(this.port);
						KingdomsDaemon.getInstance().getPorts().remove(portIndex);

						break;
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
	
	public void setState(ServerState state) {
		this.state = state;
		System.out.println("kingdom" + this.id + " state set to " + state.getState());
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
