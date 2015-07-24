package com.minehut.daemon.status;

import com.minehut.daemon.KingdomsDaemon;
import com.minehut.daemon.server.KingdomServer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by luke on 7/9/15.
 */
public class StatusManager {
    public ArrayList<KingdomUploadInfo> uploadInfos;

    public StatusManager() {
        this.uploadInfos = new ArrayList<>();

        Timer timer = new Timer();
        timer.schedule(new Upload(), 0, 2000);
    }

    class Upload extends TimerTask {
        ArrayList<KingdomUploadInfo> localUploadInfos;

        public Upload() {
            this.localUploadInfos = new ArrayList<>();
        }

        public void run() {
            this.localUploadInfos.clear();

            for (KingdomServer kingdomServer : KingdomsDaemon.getInstance().getServers()) {
                this.localUploadInfos.add(new KingdomUploadInfo(kingdomServer.getKingdom().getName(), kingdomServer.getMotd(), kingdomServer.kingdom.getOwner().rank, kingdomServer.getPort(), kingdomServer.getPlayerCount(), kingdomServer.getMaxPlayers()));
            }

            if (!localUploadInfos.isEmpty()) {
                for (KingdomUploadInfo kingdom : localUploadInfos) {

                    DBObject query = new BasicDBObject("name", "kingdom" + kingdom.getName());
                    DBObject found = KingdomsDaemon.getInstance().getServersCollection().findOne(query);

                    if (found == null) {
//                        System.out.println("Inserting Kingdom Status: " + kingdom.getName());
                        KingdomsDaemon.getInstance().getServersCollection().insert(createDBObject(kingdom));
                    } else {
//                        System.out.println("Found and updating Kingdom Status: " + kingdom.getName());
                        KingdomsDaemon.getInstance().getServersCollection().findAndModify(query, createDBObject(kingdom));
                    }

                }
            }
        }

        public DBObject createDBObject(KingdomUploadInfo kingdom) {
            String bungee = "k" + (kingdom.port - 40000);
            String motd = ""; //todo: get MOTD

            DBObject obj = new BasicDBObject("name", "kingdom" + kingdom.getName());
            obj.put("kingdomName", kingdom.getName());
            obj.put("type", "kingdom");
            obj.put("bungee", bungee);
            obj.put("motd", motd);
            obj.put("rank", kingdom.rank);

            try {
                obj.put("ip", InetAddress.getLocalHost().toString());
            } catch (UnknownHostException e) {
                obj.put("ip", "unknown");
                e.printStackTrace();
            }

            obj.put("port", kingdom.port);
            obj.put("playersOnline", kingdom.getPlayersOnline());
            obj.put("maxPlayers", 10); //todo: get max players
            obj.put("lastOnline", System.currentTimeMillis());

            return obj;
        }
    }
}
