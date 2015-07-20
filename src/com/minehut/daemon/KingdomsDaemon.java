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
import com.minehut.daemon.protocol.addon.Addon;
import com.minehut.daemon.server.KingdomServer;
import com.minehut.daemon.tools.LogType;
import com.minehut.daemon.tools.Utils;
import com.minehut.daemon.tools.mc.MCPlayer;
import com.mongodb.*;

public class KingdomsDaemon extends Thread implements Runnable {

	private int port = 10420;
	
	public static int defaultPort = 40000;
	
	private ServerSocket serverSocket;
	
	private Utils utils;
	
	public Gson gson;
	
	private List<SampleKingdom> samples;
	
	private List<Addon> addons;
	
	private List<KingdomServer> servers;
	
	private List<String> ports;

	private static KingdomsDaemon instance;

	//public StatusManager statusManager;

	/* Database */
	private MongoClient mongo;
	private DB db;
	private DBCollection kingdomsCollection;
	private DBCollection serversCollection;
	
	public KingdomsDaemon() {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					for (KingdomServer server : KingdomsDaemon.getInstance().servers) {
						server.shutdown(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		instance = this;

		this.utils = new Utils();
		this.gson = new Gson();
		
		this.ports = new ArrayList<String>();
		this.servers = new ArrayList<KingdomServer>();
		this.samples = this.initSampleKingdoms();
		this.addons = this.initAddons();
		
		this.connect();
		
		this.initDirs();
		this.initServerSocket();

		//this.statusManager = new StatusManager();
	}
	
	private void connect() {
		try {
			this.mongo = new MongoClient("localhost", 27017);
			this.db = mongo.getDB("minehut");
			this.kingdomsCollection = db.getCollection("kingdoms");
			this.serversCollection = db.getCollection("servers");

			if (this.db == null) {
				System.out.println("Couldn't connect to database, enabling offline mode.");
				return;
			} else {
				System.out.println("Successfully connected to database :)");
			}

		} catch (Exception e) {
			System.out.println("Couldn't connect to database, enabling offline mode.");
		}
	}

	public void insertKingdomInDatabase(Kingdom kingdom) {
		DBObject insert = new BasicDBObject("name", kingdom.getName());
		insert.put("ownerUUID", kingdom.getOwner().playerUUID);
		insert.put("type", kingdom.getSampleKingdom().getName());

		kingdomsCollection.insert(insert);
	}

	public String getUUIDFromDatabase(String kingdomName) {
		DBObject r = new BasicDBObject("name", kingdomName);
        DBObject found = kingdomsCollection.findOne(r);

        if (found != null) {
            String ownerUUID = (String) found.get("ownerUUID");
            return ownerUUID;
        } else {
        /* Player not found, return default */
            return "null";
        }
	}
	
	public boolean isKingdom(String kingdomName) {
		return !this.getUUIDFromDatabase(kingdomName).equals("null");
	}
	
	public Kingdom getKingdom(String kingdomName) {
		return this.getPlayerKingdom(this.getUUIDFromDatabase(kingdomName), kingdomName);
	}
	
	public Kingdom getPlayerKingdom(String uuid, String kingdomName) {
		for (Kingdom kingdom : this.getPlayerKingdoms(uuid)) {
			if (kingdom.getName().equals(kingdomName)) {
				return kingdom;
			}
		}

		return null;
	}
	
	public void changeKingdomNameInDatabase(String oldName, Kingdom kingdom) {
		DBObject key = new BasicDBObject("name", oldName);
		DBObject found = kingdomsCollection.findOne(key);

		found.put("name", kingdom.getName());
		kingdomsCollection.findAndModify(key, found);
		System.out.println("Updated kingdom name from (" + oldName + ") to (" + kingdom.getName() + ")");
	}
	
	public List<Addon> getAddons() {
		return this.addons;
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
				ports.add(Integer.toString(i));
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
	
	public KingdomServer getServer(Kingdom kingdom) {
		for (KingdomServer server : this.servers) {
			if (server.kingdom.getOwner().playerUUID.equals(kingdom.getOwner().playerUUID)) {
				if (server.kingdom.id == kingdom.id) {
					return server;
				}
			}
		}
		return null;
	}
	
	public List<Kingdom> getPlayerKingdoms(String UUID) {
		List<Kingdom> playerKingdoms = new ArrayList<Kingdom>();
		for (File s : this.playerKingdoms()) {
			if (s.getName().equals(UUID)) {
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
			}
		}
		
		
		return playerKingdoms;
	}
	
	public List<Kingdom> getPlayerKingdoms(MCPlayer player) {
		return this.getPlayerKingdoms(player.playerUUID);
	}
	
	public List<Addon> initAddons() {
		List<Addon> addonList = new ArrayList<Addon>();
		String baseDir = "./addons";
		File file = new File(baseDir);
		File[] folders = file.listFiles();
		for (File s : folders) {
			Addon addon;
			BufferedReader br = null ;
			try {
				br = new BufferedReader(new FileReader(s + "/data.json"));
				addon = this.gson.fromJson(br, Addon.class);
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
			addonList.add(addon);
		}
		return addonList;
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
		List<String> dirs = Arrays.asList("kingdoms", "sample-kingdoms", "addons");
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
			this.utils.logLine(LogType.DEBUG, sock.toString());
			
			ConnectionHandler connectionHandler = new ConnectionHandler(this, sock);
			connectionHandler.start();
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

	public static KingdomsDaemon getInstance() {
		return instance;
	}

	public DBCollection getKingdomsCollection() {
		return kingdomsCollection;
	}

	public DBCollection getServersCollection() {
		return serversCollection;
	}

	/*public StatusManager getStatusManager() {
		return statusManager;
	}*/

	public List<KingdomServer> getServers() {
		return servers;
	}

	public List<String> getPorts() {
		return ports;
	}
}
