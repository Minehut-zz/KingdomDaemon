package com.minehut.daemon.protocol.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import com.google.gson.Gson;
import com.minehut.daemon.Kingdom;
import com.minehut.daemon.SampleKingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.create.CreatePayload;
import com.minehut.daemon.protocol.start.StartPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload.KingdomDataType;
import com.minehut.daemon.protocol.status.PlayerKingdomsListPayload;
import com.minehut.daemon.protocol.status.SampleKingdomListPayload;
import com.minehut.daemon.protocol.status.out.StatusPlayerKingdomsList;
import com.minehut.daemon.protocol.status.out.StatusSampleList;
import com.minehut.daemon.tools.mc.MCPlayer;

public class DaemonFactory {
	
	private String daemonHost;
	private int daemonPort;
	
	private Gson gson;
	
	private Socket sock;
	
	public DaemonFactory(String host, int port) {
		this.gson = new Gson();
		this.daemonHost = host;
		this.daemonPort = port;
	}
	
	private boolean openSocket() {
		try {
			this.sock = new Socket(this.daemonHost, this.daemonPort);
			return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean closeSocket() {
		try {
			this.sock.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private String writeToSocket(Payload payload) {
		String message = "null";
		if (this.openSocket()) {
			try {
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
				objectOutputStream.writeObject(payload.getPayloadType().toString() + "\n" + this.gson.toJson(payload));
				ObjectInputStream objectInputStream = new ObjectInputStream(sock.getInputStream());
				message = (String)objectInputStream.readObject();
				objectOutputStream.close();
				objectInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.closeSocket();
		}
		return message;
	}
	
	public List<Kingdom> getPlayerKingdoms(MCPlayer player) {
		return this.getStatusPlayersKingoms(player).kingdoms;
		
	}
	
	public boolean hasKingdom(MCPlayer player) {
		return this.getStatusPlayersKingoms(player).hasKingdom;
	}
	
	private StatusPlayerKingdomsList getStatusPlayersKingoms(MCPlayer player) {
		PlayerKingdomsListPayload statusPayload = new PlayerKingdomsListPayload(player);
		String response = this.writeToSocket(statusPayload);
		return this.gson.fromJson(response, StatusPlayerKingdomsList.class);
	}
	
	public StatusSampleList getStatusSampleList() {
		SampleKingdomListPayload payload = new SampleKingdomListPayload();
		String response = this.writeToSocket(payload);
		return this.gson.fromJson(response, StatusSampleList.class);
	}
	
	public void createKingdom(MCPlayer player, SampleKingdom sample, String name) {
		CreatePayload payload = new CreatePayload(player, sample, name);
		this.writeToSocket(payload);
	}
	
	public void createKingdom(MCPlayer player, SampleKingdom sample) {
		this.createKingdom(player, sample, player.playerName + "'s kingdom");
	}
	
	public void startKingdom(Kingdom kingdom) {
		StartPayload payload = new StartPayload(kingdom);
		this.writeToSocket(payload);
	}
	
//	public class
	
	public String getStartup(Kingdom kingdom) {
		return this.getKingdomData(kingdom, KingdomDataType.STARTUP);
	}
	
	public String getKingdomData(Kingdom kingdom, KingdomDataType type) {
		KingdomDataPayload payload = new KingdomDataPayload(kingdom, type);
		String response = this.writeToSocket(payload);
		return response;
	}
	
}
