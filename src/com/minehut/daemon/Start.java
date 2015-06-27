package com.minehut.daemon;

import com.google.gson.Gson;

public class Start {

	
	
	public static void main(String[] args) {
	
		Gson gson = new Gson();
		
		SampleKingdom sample = new SampleKingdom();
		sample.name = "Default";
		sample.type = "Default";
		sample.version = "1.8.7";
		
		//System.out.println(gson.toJson(sample));
		
		new KingdomsDaemon().start();
	}
	
}
