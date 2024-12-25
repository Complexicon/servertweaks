package dev.cmplx.servertweaks.tweaks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import dev.cmplx.servertweaks.Util;

public class AnvilRename implements Listener {

	@EventHandler
	public void onAnvilPrepare(PrepareAnvilEvent e) {
		if (e.getResult() != null) {
			var meta = e.getResult().getItemMeta();
			if (meta.hasDisplayName()) {
				meta.setDisplayName(Util.fixColor(meta.getDisplayName()));
				e.getResult().setItemMeta(meta);
			}
		}
	}

}
