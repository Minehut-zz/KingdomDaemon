package com.minehut.daemon.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.KingdomsDaemon;
import com.minehut.daemon.tools.FileUtil;
import com.minehut.daemon.tools.LogType;

public class KingdomServer extends Thread {
	
	private int id = -1, port = -1;
	
	public String startup = "0%"; //TODO: startup = 73%
	
	public Kingdom kingdom;
	
	public ServerState state;
	
	private File log;
	
	public boolean old = false;
	
	public int playerCount = 0;
	public String motd = ""; //todo
	public int maxPlayers = 10; //todo
	
	private int emptyTick = 0;
	
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
		init();
	}
	
	public KingdomServer(Kingdom kingdom, int id, boolean old) {
		this.kingdom = kingdom;
		this.id = id - KingdomsDaemon.defaultPort;
		this.port = id;
		this.old = old;
		init();
	}
	
	private void init() {
		this.motd = FileUtil.getKingdomMOTD(this.kingdom);
	}
	
	public int getID() {
		return this.id;
	}
	
	public int getPort() {
		return port;
	}
	
	
	private long lastTick = 0L, pointer, lastChecked = 0L;
	
	public int getMemory() { //TODO: Should Famous/other ranks have different memeory
		String rank = this.kingdom.getOwner().rank.toLowerCase();
		if (rank.equals("admin")||rank.equals("owner")||rank.equals("dev")) {
			return 4096;
		} else
		if (rank.equals("champ") || rank.equals("mod")) {
			return 3072;
		} else
		if (rank.equals("legend")) {
			return 2048;
		} else 
		if (rank.equals("super")) {
			return 1792;
		} else
		if (rank.equals("mega")) {
			return 1024;
		} else {
			return 768;
		}
	}
	
	
	@Override
	public void run() {
		if (id==-1) 
			return;
		
		try {
			KingdomsDaemon.getInstance().getUtils().logLine(LogType.INFO, "Starting kingdom" + this.id + " on port " + this.port + " @ " + System.currentTimeMillis());
			this.setState(ServerState.STARTING);
			FileUtil.editServerProperties(this.kingdom, this.port);

			log = new File("/home/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id + "/screenlog.0");
			
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "screen -dmLS kingdom" + this.id + " bash -c 'java -XX:MaxPermSize=128M -Xmx" + this.getMemory() + "M -Xms" + this.getMemory() + "M -jar spigot.jar nogui'");
			pb.directory(new File("/home/daemon/kingdoms/" + this.kingdom.getOwner().playerUUID + "/kingdom" + this.kingdom.id));
			pb.start().waitFor();
			//this.setState(ServerState.STARTING);
			
			FileWriter writer = new FileWriter(log);
			writer.write("STARTING KINGDOM - (KINGDOM DAEMON v4.2.0)\n");
			writer.write("kingdomID:" + this.id + "\n");//TODO: Daemon should use this to grab kingdom data after restarting
			writer.close();

		    while (true) {
		    	if (this.lastTick == 0)
		    		this.lastTick = System.currentTimeMillis();
		    	if (this.lastChecked == 0)
		    		this.lastChecked = System.currentTimeMillis();
		    	if (((System.currentTimeMillis() - this.lastTick) / 1000) >= 10) {
		    		if (this.emptyTick >= 9) { //After 6 * (10 seconds) checks with no players online the server will shutdown
			    		if (this.state!=ServerState.SHUTDOWN) {
			    			this.setState(ServerState.SHUTDOWN);
			    		}
			    	}
		    		if (this.state == ServerState.SHUTDOWN) {
		    			this.shutdown(false);
						break;
		    		}
		    		System.out.println("Current player count for kingdom" + this.id + " is " + this.playerCount);
		    		if (this.playerCount<=0 && this.state == ServerState.RUNNING) {
						emptyTick++; //If no players are online tick this up by 1 until it hits 6
					} else {
						emptyTick=0;//Reset if the count is not less than or 0
					}
		    		this.lastTick = System.currentTimeMillis();
		    	}
		    	
		    	if ((System.currentTimeMillis() - this.lastChecked) / 1000 >= 1) { //We can change this up to ms/s/minute or whatever we need
					try {
						RandomAccessFile raf = new RandomAccessFile(log, "r");
						long length = raf.length();
						if (length < pointer) {
							raf.close();
							raf = new RandomAccessFile(log, "r");
							pointer = 0;
						}
						if (length > pointer) {
							raf.seek(pointer);
							String line = raf.readLine();
							while(line != null) {
								this.parseLine(line);
								line = raf.readLine();
							}
			            	pointer = raf.getFilePointer();
						}
						raf.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					lastChecked = System.currentTimeMillis();
				}
		    }
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown(boolean daemon) throws InterruptedException, IOException {
		new ProcessBuilder("/bin/bash", "-c", "screen -X -S kingdom" + this.id + " quit").start().waitFor(); //Should kill the screen after the kingdom shuts down
		log.delete();
		if (!daemon) {
			KingdomsDaemon.getInstance().getServers().remove(this);
			KingdomsDaemon.getInstance().getPorts().remove(Integer.toString(this.id));
		}
	}
	
	private long previousListSendTime = 0L;
	public long lastReadTime;
	
	public void parseLine(String line) {
		if (previousListSendTime==0L) {
			previousListSendTime = System.currentTimeMillis();
		}
			/* Retrieve Player Count */
			if (line.contains("There are ") && line.contains(" players online:")) {
				String[] firstPart = line.split("There are ");
				String[] secondPart = firstPart[1].split(" players online:");
				String countString[] = secondPart[0].split("/");
				int count = Integer.parseInt(countString[0]);
				//TODO: countString[1] is the max players
				this.playerCount = count;
				System.out.println("New player count for kingdom" + this.id + " has been set to " + this.playerCount);
			} else {
				if (((System.currentTimeMillis() - this.previousListSendTime) / 1000) >= 3 && this.state == ServerState.RUNNING) {
					if (!line.startsWith(">")) {
						this.sendScreenCommand("list");
						this.previousListSendTime = System.currentTimeMillis();
					}
				}
			}
		
		this.lastReadTime = System.currentTimeMillis();
		if (line.contains("INFO]: Stopping server")) {
			this.setState(ServerState.SHUTDOWN);
		} else if (line.contains(" INFO]: Done (")) {
			this.startup = "100%";
			this.setState(ServerState.RUNNING);
		} else if (line.contains("FAILED TO BIND TO PORT!")) {
			this.setState(ServerState.SHUTDOWN);
		} else if (line.contains("Preparing spawn area: ")) {
			String[] startupArray = line.split("Preparing spawn area: ");
			this.startup = startupArray[1];
		}
	}
	
	public void setState(ServerState state) {
		this.state = state;
	}
	
	public Process sendScreenCommand(String cmd) {
		Process process = null;
		try {
			process =  new ProcessBuilder("/bin/bash", "-c", "screen -S kingdom" + this.id + " -p 0 -X stuff '" + cmd + "^M'").start();
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
