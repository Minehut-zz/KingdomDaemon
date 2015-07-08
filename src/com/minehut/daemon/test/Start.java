package com.minehut.daemon.test;

import java.io.File;
import java.util.List;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import com.minehut.daemon.Kingdom;
import com.minehut.daemon.KingdomServer;
import com.minehut.daemon.SampleKingdom;
import com.minehut.daemon.KingdomServer.LogListener;
import com.minehut.daemon.protocol.api.DaemonFactory;
import com.minehut.daemon.tools.mc.MCPlayer;

public class Start {
	
	public Start() {
		DaemonFactory daemonFactory = new DaemonFactory("199.187.182.168", 10420);
		MCPlayer sq = new MCPlayer().setPlayerUUID("snickUUID").setPlayerName("Snick").setPlayerRank("admin");
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
		/*DaemonFactory daemonFactory = new DaemonFactory("199.187.182.168", 10420);
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
		}*/
	}
	
	public class LogListener extends TailerListenerAdapter {
		@Override
		public void handle(String line) { //TODO: More advanced parsing, simple parsing methods for showcase
			System.out.println(line);
		}
	}
	
	public static void main(String[] args) {
		new Start();
	}
	
}
