package dev.cmplx.servertweaks;

import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class MOTDHelper implements Listener {

	@EventHandler
	void onServerPing(ServerListPingEvent e) {
		e.setMotd(Util.fixColor(Config.motdHeader + "\n" + Config.motdList.get(new Random().nextInt(Config.motdList.size()))));
	}
	
}
