package com.minehut.daemon.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.KingdomsDaemon;
import com.minehut.daemon.tools.FileUtil;

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
	
	
	private long lastTick = 0L, pointer, lastChecked = 0L;
	
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
			
			this.sendScreenCommand("java -XX:MaxPermSize=128M -Xmx768M -Xms768M -jar spigot.jar nogui").waitFor();

			this.setState(ServerState.RUNNING);
			
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
		    		System.out.println("KingdomServer Thread ticking at " + System.currentTimeMillis() + " for kingdom" + this.id);
		    		if (this.state == ServerState.SHUTDOWN) {
		    			this.shutdown(false);
						break;
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
			KingdomsDaemon.getInstance().getPorts().remove(Integer.toString(this.port));
		}
	}
	
	private long previousListSendTime = 0L;
	public long lastReadTime;
	
	public void parseLine(String line) {
		if (((System.currentTimeMillis() - this.previousListSendTime) / 1000) >= 3) {
			/* Retrieve Player Count */
			if (line.contains("There are ") && line.contains(" players online:")) {
				String[] firstPart = line.split("There are ");
				String[] secondPart = firstPart[1].split(" players online:");
				String countString[] = secondPart[0].split("//");
				int count = Integer.parseInt(countString[0]);
				//TODO: countString[1] is the max players
				this.playerCount = count;
			} else {
				if (!line.startsWith(">")) {
					this.sendScreenCommand("list");
					this.previousListSendTime = System.currentTimeMillis();
				}
			}
		}
		this.lastReadTime = System.currentTimeMillis();
		if (line.contains("INFO]: Stopping server")) {
			this.setState(ServerState.SHUTDOWN);
			System.out.println(line);
		} else if (line.contains(" INFO]: Done (")) {
			this.startup = "100%";
			System.out.println(line);
		} else if (line.contains("FAILED TO BIND TO PORT!")) {
			this.setState(ServerState.SHUTDOWN);
			System.out.println(line);
		} else if (line.contains("Preparing spawn area: ")) {
			String[] startupArray = line.split("Preparing spawn area: ");
			this.startup = startupArray[1];
			System.out.println("Updated Startup Status: " + startupArray[1]);
		}
	}
	
	public void setState(ServerState state) {
		this.state = state;
		System.out.println("kingdom" + this.id + " state set to " + state.getState());
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
