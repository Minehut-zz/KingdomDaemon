package com.minehut.daemon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.minehut.daemon.tools.LogType;
import com.minehut.daemon.tools.Utils;
import com.minehut.daemon.tools.mc.MCPlayer;

public class KingdomsDaemon extends Thread implements Runnable {

	private int port = 10420;
	
	public static int defaultPort = 40000;
	
	private String homeDir = ".";
	
	private ServerSocket serverSocket;
	
	private Utils utils;
	
	public Gson gson;
	
	private List<SampleKingdom> samples;
	
	private List<KingdomServer> servers;
	
	private List<Integer> ports;
	
	public KingdomsDaemon() {
		this.utils = new Utils();
		this.gson = new Gson();
		
		this.ports = new ArrayList<Integer>();
		this.servers = new ArrayList<KingdomServer>();
		this.samples = this.initSampleKingdoms();
		
		this.initDirs();
		this.initServerSocket();
	}
	
	public void addKingdomServer(KingdomServer server) {
		this.servers.add(server);
		server.start();
	}
	
	public boolean usedPort(int port) {
		for (KingdomServer server : this.servers) {
			if (server.getPort()==port)
				return true;
		}
		return false;
	}
	
	public void clearPort(int port) {
		this.ports.remove(port);
	}
	
	public int getFreePort() {
		int port = -1;
		for (int i = 1; i < 66; i++) {
			if (!this.usedPort(i)&&!ports.contains(i)) {
				port = defaultPort + i;
				ports.add(i);
				break;
			}
		}
		return port;
	}
	
	
	/*
	 * kingdoms
	 * 	* PLAYERUUID
	 * 		* kingdom1
	 * 		* kingdom2
	 * 	* PLAYER2UUID
	 * 		* kingdom1
	 * sample-kingdoms
	 * 	* original
	 * 		* plugins
	 * 			* pluginname1
	 * 				* install
	 * 					* plugins
	 * 						* plugin.jar
	 * 					* config
	 * 						* config.yml
	 * 				* data.json
	 * 					* PluginName/Version/Creator/Desc/etc
	 * 		* mods
	 * 		* install
	 * 			* eula.txt
	 * 			* minecraft_server.jar (etc, etc)
	 * 		* data.json
	 * 	* ftb-1.8
	 * 		* plugins
	 * 		* mods
	 * 			* modname1
	 * 				* install
	 * 				* data.json
	 * 		* install
	 * 		* data.json	
	 * daemon.jar
	 * 
	 */
	
	public MCPlayer getMCPlayer(String UUID) {
		MCPlayer player = new MCPlayer();
		player.playerUUID = UUID;
		player.rank = "default";
		return player;
	}
	
	public Kingdom newKingdom(MCPlayer player, SampleKingdom sample) {
		Kingdom kingdom = new Kingdom(player, sample);
		return kingdom;
	}
	
	public boolean hasKingdom(String UUID) {
		for (File s : this.playerKingdoms()) {
			if (s.getName().equals(UUID)) {
				return true;
			}
		}
		return false;
	}
	
	public File[] playerKingdoms() {
		return new File("./kingdoms").listFiles();
	}
	
	public List<Kingdom> getPlayerKingdoms(MCPlayer player) {
		List<Kingdom> playerKingdoms = new ArrayList<Kingdom>();
		for (File s : this.playerKingdoms()) {
			if (s.getName().equals(player.playerUUID)) {
				
				System.out.println(s.getName());
				for (File kd : s.listFiles()) {
					Kingdom kingdom;
					try {
						System.out.println(kd.getPath() + "/data.json");
						BufferedReader br = new BufferedReader(new FileReader(kd.getPath() + "/data.json"));
						kingdom = this.gson.fromJson(br, Kingdom.class);
						
					} catch (IOException e) {
						e.printStackTrace();
						this.utils.logLine(LogType.ERROR, "Error loading samplekingdom's data.json!");
						continue;
					}
					if (kingdom!=null)
						playerKingdoms.add(kingdom);
				}
				/*
						
					*/
			}
		}
		
		
		return playerKingdoms;
	}
	
	public List<SampleKingdom> initSampleKingdoms() {
		List<SampleKingdom> samples = new ArrayList<SampleKingdom>();
		String baseDir = "./sample-kingdoms";
		File file = new File(baseDir);
		File[] folders = file.listFiles();
		for (File s : folders) {
			SampleKingdom sample;
			BufferedReader br = null ;
			try {
				br = new BufferedReader(new FileReader(s + "/data.json"));
				sample = this.gson.fromJson(br, SampleKingdom.class);
			} catch (IOException e) {
				e.printStackTrace();
				this.utils.logLine(LogType.ERROR, "Error loading samplekingdom's data.json!");
				continue;
			} finally {
				if (br!=null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			samples.add(sample);
		}
		return samples;
	}
	
	
	private void initDirs() {
		List<String> dirs = Arrays.asList("kingdoms", "sample-kingdoms");
		for (String dir : dirs) {
			File file = new File("./" + dir);
			if (!file.exists())
				file.mkdir();
		}
	}
	
	@Override
	public void run() {
		
		while(true) {
			try {
				tick();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void tick() throws Exception {
		Socket sock = null;

		sock = this.serverSocket.accept();
		
		if (sock!=null) {
			//TODO: Parse socket connection
			this.utils.logLine(LogType.DEBUG, sock.toString());
			
			ConnectionHandler connectionHandler = new ConnectionHandler(this, sock);
			connectionHandler.start();
			
			//sock.close();
		}
	}
	
	private void initServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (Exception e) {
			System.err.println("Could not start server: " + e);
			this.utils.logLine(LogType.ERROR, "Error creating server socket, is the port used?  Please try to restart the server!");
            System.exit(-1);
		}
	}
	
	public List<SampleKingdom> getSampleKingdoms() {
		return this.samples;
	}
	
	public Utils getUtils() {
		return this.utils;
	}
	
	
}
