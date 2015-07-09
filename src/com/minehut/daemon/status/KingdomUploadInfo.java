package com.minehut.daemon.status;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by luke on 7/9/15.
 */
public class KingdomUploadInfo {
    String name;
    String motd;
    String rank;
    String ip;
    int port;
    String bungee;
    int playersOnline;
    int maxPlayers;
    long lastOnline;

    public KingdomUploadInfo(String name, String motd, String rank, int port, int playersOnline, int maxPlayers) {
        this.name = name;
        this.motd = motd;
        this.rank = rank;
        this.port = port;
        this.playersOnline = playersOnline;
        this.maxPlayers = maxPlayers;

        try {
            this.ip = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            this.ip = "unknown";
        }

        this.bungee = "k" + (40000 - this.port);
        this.lastOnline = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public String getMotd() {
        return motd;
    }

    public String getRank() {
        return rank;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getBungee() {
        return bungee;
    }

    public int getPlayersOnline() {
        return playersOnline;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public long getLastOnline() {
        return lastOnline;
    }
}
