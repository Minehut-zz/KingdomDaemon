package com.minehut.daemon.server;

import org.apache.commons.io.input.TailerListenerAdapter;

import com.minehut.daemon.server.KingdomServer.ServerState;

public class KingdomServerLogListener extends TailerListenerAdapter {
	
	private KingdomServer server;
	private long previousListSendTime = 0L;
	
	public KingdomServerLogListener(KingdomServer server) {
		this.server = server;
	}
	
	@Override
	public void handle(String line) { //TODO: More advanced parsing, simple parsing methods for showcase
		/* List Command Cooldown */
		
		if (this.previousListSendTime==0)
			this.previousListSendTime = System.currentTimeMillis();
		if (((System.currentTimeMillis() - this.previousListSendTime) / 1000) >= 3) {

			/* Retrieve Player Count */
			if (line.contains("There are ") && line.contains(" players online:")) {

				String[] firstPart = line.split("There are ");
				String[] secondPart = firstPart[1].split(" players online:");

				System.out.println("secondPart[0]:\"" + secondPart[0] + "\"");
				
				String countString[] = secondPart[0].split("//");

				System.out.println("countString[0]:" + countString[0]);
				
				int count = Integer.parseInt(countString[0]);
				//TODO: countString[1] is the max players
				server.playerCount = count;
			} else {
				if (!line.startsWith(">")) {
					server.sendScreenCommand("list");
					this.previousListSendTime = System.currentTimeMillis();
				}
			}

			if (line.contains(" INFO]: Stopping server")) {
				server.setState(ServerState.SHUTDOWN);
				System.out.println(line);
			} else if (line.contains(" INFO]: Done (")) {
				server.startup = "100%";
				System.out.println(line);
			} else if (line.contains("FAILED TO BIND TO PORT!")) {
				server.setState(ServerState.SHUTDOWN);
				System.out.println(line);
			} else if (line.contains("Preparing spawn area: ")) {
				String[] startupArray = line.split("Preparing spawn area: ");
				server.startup = startupArray[1];
			}
		}
	}
}