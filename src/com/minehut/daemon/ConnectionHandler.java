package com.minehut.daemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import com.minehut.daemon.protocol.PayloadType;
import com.minehut.daemon.protocol.create.CreatePayload;
import com.minehut.daemon.protocol.start.StartPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload.KingdomDataType;
import com.minehut.daemon.protocol.status.PlayerKingdomsListPayload;
import com.minehut.daemon.protocol.status.out.StatusPlayerKingdomsList;
import com.minehut.daemon.protocol.status.out.StatusSampleList;
import com.minehut.daemon.tools.FileUtil;
import com.minehut.daemon.tools.LogType;

public class ConnectionHandler extends Thread implements Runnable {

	private KingdomsDaemon daemon;
	
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	
	private boolean isFinished = false;
	
	private Socket sock;
	
	private String response;
	
	public ConnectionHandler(KingdomsDaemon daemon, Socket sock) {
		this.daemon = daemon;
		this.sock = sock;
	}
	
	@Override
	public void run() {
		while(!this.isFinished) {
			try {
				this.initStreams(sock);
				
				String line = (String)this.objectInputStream.readObject();
				
				if (line == null)
					return;
				
				
				ArrayList<String> requestLines = new ArrayList<String>(Arrays.asList(line.split("\n")));
				
				if (requestLines.size()==0 || requestLines.get(0).equals("")) {
					//TODO: Bad request page
					this.daemon.getUtils().logLine(LogType.ERROR, "Bad request from client. No data found!");
					return;
				}
				
				for (String l : requestLines) {
					this.daemon.getUtils().logLine(LogType.INFO, l);
				}
				
				this.parseRequest(requestLines);
				
				this.objectOutputStream.writeObject(this.response);
				
			} catch (Exception e) {
				e.printStackTrace();
				this.finish();
			}
		
			this.finish();
		}
	}
	
	private void parseRequest(ArrayList<String> request) {
		PayloadType type = PayloadType.valueOf(request.get(0));
		
		if (type == PayloadType.PLAYER_KINGDOMS_LIST) {
			PlayerKingdomsListPayload payload = this.daemon.gson.fromJson(request.get(1), PlayerKingdomsListPayload.class);
			
				StatusPlayerKingdomsList out = new StatusPlayerKingdomsList();
				
				if (this.daemon.hasKingdom(payload.player.playerUUID)) {
					out.setHasKingdom(true);
					out.setPlayerKingdomsList(this.daemon.getPlayerKingdoms(payload.player));
				} else {
					out.setHasKingdom(false);
				}
				this.response = this.daemon.gson.toJson(out);
			System.out.println("Status payload: " + payload);
		} else 
		if (type == PayloadType.SAMPLE_KINGDOMS_LIST) {
			this.response = this.daemon.gson.toJson(new StatusSampleList().setSampleList(this.daemon.getSampleKingdoms()));
			
		} else
		if (type == PayloadType.CREATE) {
			CreatePayload payload = this.daemon.gson.fromJson(request.get(1), CreatePayload.class);
			Kingdom kingdom = new Kingdom(payload.owner, payload.sample);
			kingdom.setName(payload.name);
			FileUtil.installKingdom(kingdom);
		} else
		if (type == PayloadType.START) {
			StartPayload payload = this.daemon.gson.fromJson(request.get(1), StartPayload.class);
			int port = this.daemon.getFreePort();
			if (port!=-1) {
				System.out.println("Found free port: " + port);
				this.response = "{port:" + port + "}";
				this.daemon.addKingdomServer(new KingdomServer(payload.kingdom, port));
			} else {
				System.out.println("No free port found");
			}
		} else 
		if (type == PayloadType.STOP) {
			//StopPayload payload = this.daemon.gson.fromJson(request.get(1), StopPayload.class);
		} else
		if (type == PayloadType.KINGDOM_DATA) {
			KingdomDataPayload payload = this.daemon.gson.fromJson(request.get(1), KingdomDataPayload.class);
			if (payload.type == KingdomDataType.STARTUP) {
				this.response = "{startup:"+ this.daemon.getServer(payload.kingdom).startup + "}";
			}
		}
		
		System.out.println("FOUND PAYLOAD TYPE: " + type);
	}

	public void finish() {
		this.isFinished = true;
		try {
			if (this.objectInputStream!=null)
				this.objectInputStream.close();
			if (this.objectOutputStream!=null)
				this.objectOutputStream.close();
			if (this.sock!=null)
				this.sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initStreams(Socket sock) {
		try {
			this.objectInputStream = new ObjectInputStream(sock.getInputStream());
			this.objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			this.daemon.getUtils().logLine(LogType.ERROR, "Error creating streams!");
			this.finish();
		}
	}
	
}
