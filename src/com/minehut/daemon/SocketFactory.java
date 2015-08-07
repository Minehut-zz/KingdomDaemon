package com.minehut.daemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.minehut.daemon.protocol.PayloadType;
import com.minehut.daemon.protocol.addon.AddonPayload;
import com.minehut.daemon.protocol.addon.AddonPayloadType;
import com.minehut.daemon.protocol.create.CreatePayload;
import com.minehut.daemon.protocol.motd.ModifyMOTDPayload;
import com.minehut.daemon.protocol.rename.RenamePayload;
import com.minehut.daemon.protocol.reset.ResetPayload;
import com.minehut.daemon.protocol.start.StartPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload;
import com.minehut.daemon.protocol.status.KingdomPayload;
import com.minehut.daemon.protocol.status.PlayerKingdomsListPayload;
import com.minehut.daemon.protocol.status.KingdomDataPayload.KingdomDataType;
import com.minehut.daemon.protocol.status.out.StatusPlayerKingdomsList;
import com.minehut.daemon.protocol.status.out.StatusSampleList;
import com.minehut.daemon.protocol.stop.StopPayload;
import com.minehut.daemon.server.KingdomServer;
import com.minehut.daemon.server.KingdomServer.ServerState;
import com.minehut.daemon.tools.FileUtil;
import com.minehut.daemon.tools.LogType;

public class SocketFactory {

	public ArrayList<SocketHandler> handlerQueue;
	
	public SocketFactory() {
		this.handlerQueue = new ArrayList<SocketHandler>();
		
		Timer timer = new Timer(); //Grabbed from StatusManager, system is just the same with a few changes on getting the queue.
        timer.schedule(new SocketHandlerThread(), 0, 1000);
	}
	
	
	class SocketHandlerThread extends TimerTask {

		@Override
		public void run() {
			System.out.println("Starting SocketHandler");
			System.out.println("found " + handlerQueue.size() + " SocketHandler objects");
			ArrayList<SocketHandler> local = new ArrayList<SocketHandler>();//Local copy for timer
			
			Iterator<SocketHandler> i = handlerQueue.iterator(); //Clone queue
			while (i.hasNext()) {
				local.add(i.next());
			}
			
			handlerQueue.clear(); //Clear queue
			System.out.println("Cleared queue to " + handlerQueue.size() + " and local copy filled to " + local.size());
			
			if (!local.isEmpty()) {
				for (SocketHandler sh : local) { //Parse local queue
					String response = "NULL";
					
					
					try {
						
						ObjectInputStream objectInputStream = null;
						ObjectOutputStream objectOutputStream = null;
						
						try {
							objectInputStream = new ObjectInputStream(sh.getSocket().getInputStream());
							objectOutputStream = new ObjectOutputStream(sh.getSocket().getOutputStream());
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Error creating streams!");
							if (objectInputStream!=null)
								objectInputStream.close();
							if (objectOutputStream!=null)
								objectOutputStream.close();
							if (sh.getSocket()!=null)
								sh.getSocket().close();
							return;
						}
						
						String line = (String)objectInputStream.readObject();
						
						if (line == null)
							return;
						
						ArrayList<String> requestLines = new ArrayList<String>(Arrays.asList(line.split("\n")));
						
						if (requestLines.size()==0 || requestLines.get(0).equals("")) {
							//TODO: Bad requestLines page
							//KingdomsDaemon.getInstance().getUtils().logLine(LogType.ERROR, "Bad requestLines from client. No data found!");
							return;
						}

						PayloadType type = PayloadType.valueOf(requestLines.get(0));
						if (type == PayloadType.PLAYER_KINGDOMS_LIST) {
							PlayerKingdomsListPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), PlayerKingdomsListPayload.class);
							StatusPlayerKingdomsList out = new StatusPlayerKingdomsList();
							if (KingdomsDaemon.getInstance().hasKingdom(payload.player.playerUUID)) {
								out.setHasKingdom(true);
								out.setPlayerKingdomsList(KingdomsDaemon.getInstance().getPlayerKingdoms(payload.player));
							} else {
								out.setHasKingdom(false);
							}
							response = KingdomsDaemon.getInstance().gson.toJson(out);
						} else 
						if (type == PayloadType.SAMPLE_KINGDOMS_LIST) {
							response = KingdomsDaemon.getInstance().gson.toJson(new StatusSampleList().setSampleList(KingdomsDaemon.getInstance().getSampleKingdoms()));
						} else
						if (type == PayloadType.CREATE) {
							CreatePayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), CreatePayload.class);
							Kingdom kingdom = new Kingdom(payload.owner, payload.sample, KingdomsDaemon.getInstance().getPlayerKingdoms(payload.owner).size());
							kingdom.setName(payload.name);
							FileUtil.installKingdom(kingdom);
							KingdomsDaemon.getInstance().insertKingdomInDatabase(kingdom);
						} else
						if (type == PayloadType.RESET) {
							ResetPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), ResetPayload.class);
							FileUtil.resetKingdom(payload.kingdom);
							KingdomsDaemon.getInstance().insertKingdomInDatabase(payload.kingdom);
						} else
						if (type == PayloadType.RENAME) {
							RenamePayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), RenamePayload.class);
							Kingdom kd = KingdomsDaemon.getInstance().getKingdom(payload.getOldName());
							kd.setName(payload.getNewName());
							FileUtil.renameKingdom(kd);
							KingdomsDaemon.getInstance().changeKingdomNameInDatabase(payload.getOldName(), kd);
						} else
						if (type == PayloadType.START) {
							StartPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), StartPayload.class);
							if (KingdomsDaemon.getInstance().getServers().size() >= 65) {
								response = "KINGDOMS_FULL";
								return;
							}
							int port = KingdomsDaemon.getInstance().getFreePort();
							if (port!=-1) {
								response = "{port:" + port + "}";
								KingdomsDaemon.getInstance().addKingdomServer(new KingdomServer(payload.kingdom, port));
							}
						} else 
						if (type == PayloadType.STOP) {
							StopPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), StopPayload.class);
							KingdomsDaemon.getInstance().getServer(KingdomsDaemon.getInstance().getKingdom(payload.kingdomName)).setState(ServerState.SHUTDOWN);
							//TODO: Remove from database of active servers if needed.
						} else
						if (type == PayloadType.KINGDOM_DATA) {
							KingdomDataPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), KingdomDataPayload.class);
							if (payload.dataType == KingdomDataType.STARTUP) {
								KingdomServer server = KingdomsDaemon.getInstance().getServer(payload.kingdom);
								if (server != null) {
									response = server.startup;
								} else {
									response = "offline";
								}
							} else
							if (payload.dataType == KingdomDataType.MOTD) {
								response = FileUtil.getKingdomMOTD(payload.kingdom);
							}
						} else
						if (type == PayloadType.MODIFY_MOTD) {
							ModifyMOTDPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), ModifyMOTDPayload.class);
							FileUtil.editKingdomMOTD(payload.getKingdom(), payload.getMOTD());
						}
						if (type == PayloadType.KINGDOM) {
							KingdomPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), KingdomPayload.class);
							if (KingdomsDaemon.getInstance().isKingdom(payload.kingdomName)) {
								response = KingdomsDaemon.getInstance().gson.toJson(KingdomsDaemon.getInstance().getKingdom(payload.kingdomName));
							} else {
								response = "null";
							}
						} else
						if (type == PayloadType.ADDON) {
							AddonPayload payload = KingdomsDaemon.getInstance().gson.fromJson(requestLines.get(1), AddonPayload.class);
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
							response = KingdomsDaemon.getInstance().gson.toJson(KingdomsDaemon.getInstance().getAddons());
						}
						
						
						
						
						objectOutputStream.writeObject(response);
						
						if (objectInputStream!=null)
							objectInputStream.close();
						if (objectOutputStream!=null)
							objectOutputStream.close();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (sh.getSocket()!=null) {
						try {
							sh.getSocket().close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
