package com.minehut.daemon.test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.input.TailerListenerAdapter;

import com.google.gson.Gson;
import com.minehut.daemon.Kingdom;
import com.minehut.daemon.SampleKingdom;
import com.minehut.daemon.protocol.addon.Addon;
import com.minehut.daemon.protocol.api.DaemonFactory;
import com.minehut.daemon.tools.mc.MCPlayer;

public class Start {
	
	public Start() {
		/*
		Addon test = new Addon("plugin01", "Random Plugin", "The Author/s", Arrays.asList("Line 1 of description"));
		test.setAllowedBases(Arrays.asList(new SampleKingdom()));
		System.out.println(new Gson().toJson(test));
		*/
		
		DaemonFactory daemonFactory = new DaemonFactory("localhost", 10420);
		MCPlayer sq = new MCPlayer().setPlayerUUID("squeecksUUID").setPlayerName("Squeecks").setPlayerRank("admin");
		List<Addon> sampleAddons = daemonFactory.getSampleAddonList();
		for (Addon addon : sampleAddons) {
			System.out.println(addon.name + " | " + addon.systemName);
		}
		if (daemonFactory.hasKingdom(sq)) {
			for (Kingdom kd : daemonFactory.getPlayerKingdoms(sq)) {
				daemonFactory.installAddon(kd, sampleAddons.get(0));
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
		/*
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
		}*/
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
