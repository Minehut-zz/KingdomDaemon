package com.minehut.daemon.protocol.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.minehut.daemon.Kingdom;
import com.minehut.daemon.SampleKingdom;
import com.minehut.daemon.protocol.Payload;
import com.minehut.daemon.protocol.addon.Addon;
import com.minehut.daemon.protocol.addon.AddonListPayload;
import com.minehut.daemon.protocol.addon.AddonPayload;
import com.minehut.daemon.protocol.addon.AddonPayloadType;
import com.minehut.daemon.protocol.create.CreatePayload;
import com.minehut.daemon.protocol.motd.ModifyMOTDPayload;
import com.minehut.daemon.protocol.rename.RenamePayload;
import com.minehut.daemon.protocol.reset.ResetPayload;
import com.minehut.daemon.protocol.start.StartPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload.KingdomDataType;
import com.minehut.daemon.protocol.status.KingdomPayload;
import com.minehut.daemon.protocol.status.PlayerKingdomsListPayload;
import com.minehut.daemon.protocol.status.SampleKingdomListPayload;
import com.minehut.daemon.protocol.status.out.StatusPlayerKingdomsList;
import com.minehut.daemon.protocol.status.out.StatusSampleList;
import com.minehut.daemon.protocol.stop.StopPayload;
import com.minehut.daemon.tools.mc.MCPlayer;

public class DaemonFactory {
	
	private String daemonHost;
	private int daemonPort;
	
	private Gson gson;
	
	public DaemonFactory(String host, int port) {
		this.gson = new Gson();
		this.daemonHost = host;
		this.daemonPort = port;
	}
	
	private String writeToSocket(Payload payload) {
		Socket sock = null;
		boolean open = false;
		try {
			sock = new Socket(this.daemonHost, this.daemonPort);
			open = true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = "null";
		if (open) {
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
			try {
				if (sock!=null)
					sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	
	public SampleKingdom getSampleKingdom(String sampleType) {
		for (SampleKingdom sk : this.getSampleKingdoms()) {
			if (sk.getType().equalsIgnoreCase(sampleType)) {
				return sk;
			}
		}
		return null; //Should never happen, always call isSampleKingdom(String.class); first
	}
	
	public boolean isSampleKingdom(String sampleType) {
		for (SampleKingdom sk : this.getSampleKingdoms()) {
			if (sk.getType().equalsIgnoreCase(sampleType)) {
				return true;
			}
		}
		return false;
	}
	
	
	public List<SampleKingdom> getSampleKingdoms() {
		return this.getStatusSampleList().sampleList;
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
	
	public void renameKingdom(String oldName, String newName) {
		this.writeToSocket(new RenamePayload(oldName, newName));
	}
	
	public void createKingdom(MCPlayer player, SampleKingdom sample) {
		this.createKingdom(player, sample, player.playerName + "'s kingdom");
	}
	
	public void startKingdom(Kingdom kingdom) {
		StartPayload payload = new StartPayload(kingdom);
		this.writeToSocket(payload);
	}
	
	public void resetKingdom(Kingdom kingdom) {
		this.writeToSocket(new ResetPayload(kingdom));
	}
	
	public void stopKingdom(String kingdomName) {
		this.writeToSocket(new StopPayload(kingdomName));
	}
	
	public void updateAddon(Kingdom kingdom, Addon addon) {
		this.writeToSocket(new AddonPayload(kingdom, addon, AddonPayloadType.UPDATE));
	}
	
	public void removeAddon(Kingdom kingdom, Addon addon) {
		this.writeToSocket(new AddonPayload(kingdom, addon, AddonPayloadType.REMOVE));
	}
	
	public void installAddon(Kingdom kingdom, Addon addon) {
		this.writeToSocket(new AddonPayload(kingdom, addon, AddonPayloadType.INSTALL));
	}
	
	public List<Addon> getSampleAddonList() {
		String response = this.writeToSocket(new AddonListPayload());
		return this.gson.fromJson(response, new TypeToken<List<Addon>>(){}.getType());
	}
	
//	public class
	
	public boolean isKingdom(String name) {
		KingdomPayload payload = new KingdomPayload(name);
		String response = this.writeToSocket(payload);
		return !response.equalsIgnoreCase("null");
	}
	
	public Kingdom getKingdom(String name) {
		return this.gson.fromJson(this.writeToSocket(new KingdomPayload(name)), Kingdom.class);
	}
	
	public String getStartup(Kingdom kingdom) {
		return this.getKingdomData(kingdom, KingdomDataType.STARTUP);
	}
	
	public void setKingdomMOTD(Kingdom kingdom, String motd) {
		this.writeToSocket(new ModifyMOTDPayload(kingdom, motd));
	}
	
	public String getKingdomMOTD(Kingdom kingdom) {
		return this.writeToSocket(new KingdomDataPayload(kingdom, KingdomDataType.MOTD));
	}
	
	public String getKingdomData(Kingdom kingdom, KingdomDataType type) {
		KingdomDataPayload payload = new KingdomDataPayload(kingdom, type);
		String response = this.writeToSocket(payload);
		return response;
	}
	
}
