package com.minehut.daemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import com.minehut.daemon.protocol.PayloadType;
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
import com.minehut.daemon.protocol.status.out.StatusPlayerKingdomsList;
import com.minehut.daemon.protocol.status.out.StatusSampleList;
import com.minehut.daemon.protocol.stop.StopPayload;
import com.minehut.daemon.server.KingdomServer;
import com.minehut.daemon.server.KingdomServer.ServerState;
import com.minehut.daemon.tools.FileUtil;
import com.minehut.daemon.tools.LogType;

public class ConnectionHandler extends Thread implements Runnable {

	private KingdomsDaemon daemon;
	
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	
	private boolean isFinished = false;
	
	private Socket sock;
	
	private String response = "NULL";
	
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
				
				this.parseRequest(requestLines);
				this.objectOutputStream.writeObject(this.response);
				this.finish();
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
		} else 
		if (type == PayloadType.SAMPLE_KINGDOMS_LIST) {
			this.response = this.daemon.gson.toJson(new StatusSampleList().setSampleList(this.daemon.getSampleKingdoms()));
		} else
		if (type == PayloadType.CREATE) {
			CreatePayload payload = this.daemon.gson.fromJson(request.get(1), CreatePayload.class);
			Kingdom kingdom = new Kingdom(payload.owner, payload.sample, this.daemon.getPlayerKingdoms(payload.owner).size());
			kingdom.setName(payload.name);
			FileUtil.installKingdom(kingdom);
			this.daemon.insertKingdomInDatabase(kingdom);
		} else
		if (type == PayloadType.RESET) {
			ResetPayload payload = this.daemon.gson.fromJson(request.get(1), ResetPayload.class);
			FileUtil.resetKingdom(payload.kingdom);
			this.daemon.insertKingdomInDatabase(payload.kingdom);
		} else
		if (type == PayloadType.RENAME) {
			RenamePayload payload = this.daemon.gson.fromJson(request.get(1), RenamePayload.class);
			Kingdom kd = this.daemon.getKingdom(payload.getOldName());
			kd.setName(payload.getNewName());
			FileUtil.renameKingdom(kd);
			this.daemon.changeKingdomNameInDatabase(payload.getOldName(), kd);
		} else
		if (type == PayloadType.START) {
			StartPayload payload = this.daemon.gson.fromJson(request.get(1), StartPayload.class);
			if (this.daemon.getServers().size() >= 65) {
				this.response = "{errorMessage:'MAX KINGDOM COUNT'}";
				return;
			}
			int port = this.daemon.getFreePort();
			if (port!=-1) {
				this.response = "{port:" + port + "}";
				this.daemon.addKingdomServer(new KingdomServer(payload.kingdom, port));
			}
		} else 
		if (type == PayloadType.STOP) {
			StopPayload payload = this.daemon.gson.fromJson(request.get(1), StopPayload.class);
			this.daemon.getServer(this.daemon.getKingdom(payload.kingdomName)).setState(ServerState.SHUTDOWN);
			//TODO: Remove from database of active servers if needed.
		} else
		if (type == PayloadType.KINGDOM_DATA) {
			KingdomDataPayload payload = this.daemon.gson.fromJson(request.get(1), KingdomDataPayload.class);
			if (payload.dataType == KingdomDataType.STARTUP) {
				KingdomServer server = this.daemon.getServer(payload.kingdom);
				if (server != null) {
					this.response = server.startup;
				} else {
					this.response = "offline";
				}
			} else
			if (payload.dataType == KingdomDataType.MOTD) {
				this.response = FileUtil.getKingdomMOTD(payload.kingdom);
			}
		} else
		if (type == PayloadType.MODIFY_MOTD) {
			ModifyMOTDPayload payload = this.daemon.gson.fromJson(request.get(1), ModifyMOTDPayload.class);
			FileUtil.editKingdomMOTD(payload.getKingdom(), payload.getMOTD());
		}
		if (type == PayloadType.KINGDOM) {
			KingdomPayload payload = this.daemon.gson.fromJson(request.get(1), KingdomPayload.class);
			if (this.daemon.isKingdom(payload.kingdomName)) {
				this.response = this.daemon.gson.toJson(this.daemon.getKingdom(payload.kingdomName));
			} else {
				this.response = "null";
			}
		} else
		if (type == PayloadType.ADDON) {
			AddonPayload payload = this.daemon.gson.fromJson(request.get(1), AddonPayload.class);
			if (payload.addonPayloadType == AddonPayloadType.INSTALL) {
				FileUtil.installAddon(payload.kingdom, payload.addon);
			} else
			if (payload.addonPayloadType == AddonPayloadType.REMOVE) {
				FileUtil.removeAddon(payload.kingdom, payload.addon);
			} else
			if (payload.addonPayloadType == AddonPayloadType.UPDATE) {
				FileUtil.removeAddon(payload.kingdom, payload.addon);
				FileUtil.installAddon(payload.kingdom, payload.addon);
			}
		} else
		if (type == PayloadType.ADDON_LIST) {
			this.response = this.daemon.gson.toJson(this.daemon.getAddons());
		}
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
