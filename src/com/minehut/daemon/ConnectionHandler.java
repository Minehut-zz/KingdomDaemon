package com.minehut.daemon;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import com.minehut.daemon.protocol.PayloadType;
import com.minehut.daemon.protocol.create.CreatePayload;
import com.minehut.daemon.protocol.start.StartPayload;
import com.minehut.daemon.protocol.status.StatusPayload;
import com.minehut.daemon.protocol.status.StatusType;
import com.minehut.daemon.protocol.status.out.StatusPlayerKingdomsList;
import com.minehut.daemon.protocol.status.out.StatusSampleList;
import com.minehut.daemon.tools.FileUtil;
import com.minehut.daemon.tools.LogType;
import com.minehut.daemon.tools.mc.MCPlayer;

public class ConnectionHandler extends Thread implements Runnable {

	private KingdomsDaemon daemon;
	/*
	private BufferedReader buffReader;
	
	private OutputStream outStream;
    
	private PrintStream printStream;*/
	
	
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
		
		if (type == PayloadType.STATUS) {
			StatusPayload payload = this.daemon.gson.fromJson(request.get(1), StatusPayload.class);
			if (payload.getStatusType() == StatusType.SAMPLE_LIST) {
				this.response = this.daemon.gson.toJson(new StatusSampleList().setSampleList(this.daemon.getSampleKingdoms()));
			} else 
			if (payload.getStatusType() == StatusType.PLAYER_LIST) {
				//this.response = this.daemon.gson.toJson(this.daemon.initPlayerKingdoms());
			} else
			if (payload.getStatusType() == StatusType.PLAYERS_KINGDOM_LIST) {
				if (request.get(2)!=null) {
					MCPlayer player = this.daemon.gson.fromJson(request.get(2), MCPlayer.class);
					StatusPlayerKingdomsList out = new StatusPlayerKingdomsList();
					
					if (this.daemon.hasKingdom(player.playerUUID)) {
						out.setHasKingdom(true);
						//out.setPlayerKingdomsList(this.getP)
					} else {
						out.setHasKingdom(false);
					}
					this.response = this.daemon.gson.toJson(out);
				}
			}
			System.out.println("Status payload: " + payload);
		} else 
		if (type == PayloadType.CREATE) {
			CreatePayload payload = this.daemon.gson.fromJson(request.get(1), CreatePayload.class);
			Kingdom kingdom = new Kingdom(payload.owner, payload.sample);
			FileUtil.installKingdom(kingdom);
		} else
		if (type == PayloadType.START) {
			StartPayload payload = this.daemon.gson.fromJson(request.get(1), StartPayload.class);
			
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
