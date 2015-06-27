package com.minehut.daemon.test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.google.gson.Gson;
import com.minehut.daemon.protocol.status.StatusPayload;
import com.minehut.daemon.protocol.status.StatusType;
import com.minehut.daemon.tools.mc.MCPlayer;

public class Start {

	public static void main(String[] args) {
		try {
			Socket sock = new Socket("localhost", 10420);
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
			objectOutputStream.writeObject(
					"STATUS\n" + 
			new Gson().toJson(new StatusPayload(StatusType.PLAYERS_KINGDOM_LIST)) + "\n" + 
			new Gson().toJson(new MCPlayer().setPlayerUUID("UUID").setPlayerRank("admin")));
			//objectOutputStream.writeObject("Test message from client #1");
			
			ObjectInputStream objectInputStream = new ObjectInputStream(sock.getInputStream());
			String message = (String)objectInputStream.readObject();
			System.out.println("Found message from SERVER: " + message);
			
			objectOutputStream.close();
			objectInputStream.close();
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
