package com.minehut.daemon.test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.minehut.daemon.Kingdom;
import com.minehut.daemon.SampleKingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.create.CreatePayload;
import com.minehut.daemon.protocol.status.PlayerKingdomsListPayload;
import com.minehut.daemon.protocol.status.out.StatusPlayerKingdomsList;
import com.minehut.daemon.tools.mc.MCPlayer;

public class Start {

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
			List<Kingdom> kingdoms = new ArrayList<Kingdom>();
			
			
			
			return kingdoms;
			
		}
		
		private StatusPlayerKingdomsList getStatusPlayersKingoms(MCPlayer player) {
			PlayerKingdomsListPayload statusPayload = new PlayerKingdomsListPayload(player);
			String response = this.writeToSocket(statusPayload);
			return this.gson.fromJson(response, StatusPlayerKingdomsList.class);
		}
		
	}
	
	
	public static void main(String[] args) {
		
		/*
		try {
			Socket sock = new Socket("localhost", 10420);
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
			objectOutputStream.writeObject(
					"STATUS\n" + 
			new Gson().toJson(new StatusPayload(StatusType.PLAYERS_KINGDOM_LIST)) + "\n" + 
			new Gson().toJson(new MCPlayer().setPlayerUUID("squeecks2").setPlayerRank("admin")));
			//objectOutputStream.writeObject("Test message from client #1");
			 * 
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
			
			objectOutputStream.writeObject(
					"CREATE\n" + 
			new Gson().toJson(
					new CreatePayload(new MCPlayer().setPlayerUUID("squeecks2").setPlayerRank("admin"), 
					new SampleKingdom().setType("default").setName("Default 1.8").setVersion("1.8.7"))));
			ObjectInputStream objectInputStream = new ObjectInputStream(sock.getInputStream());
			String message = (String)objectInputStream.readObject();
			System.out.println("Found message from SERVER: " + message);
			
			objectOutputStream.close();
			objectInputStream.close();
			sock.close();*/
		
	}
	
}
