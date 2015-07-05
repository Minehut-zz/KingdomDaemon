package com.minehut.daemon.test;

import java.util.List;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.SampleKingdom;
import com.minehut.daemon.protocol.api.DaemonFactory;
import com.minehut.daemon.tools.mc.MCPlayer;

public class Start {
	
	public Start() {
		DaemonFactory daemonFactory = new DaemonFactory("199.187.182.168", 10420);
		MCPlayer sq = new MCPlayer().setPlayerUUID("squeecksUUID").setPlayerName("Squeecks").setPlayerRank("admin");
		if (daemonFactory.hasKingdom(sq)) {
			for (Kingdom kd : daemonFactory.getPlayerKingdoms(sq)) {
				daemonFactory.startKingdom(kd);
				System.out.println(kd.getName());
			}
		} else {
			List<SampleKingdom> samples = daemonFactory.getStatusSampleList().sampleList;
			for (SampleKingdom skd : samples) {
				System.out.println("Found sample kingdom: " + skd.getName());
				if (skd.getType().equals("default")) {
					daemonFactory.createKingdom(sq, skd);
				}
			}
			System.out.println("No kingdoms found for player");
		}
	}
	
	public static void main(String[] args) {
		new Start();
	}
	
}
