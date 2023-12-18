package dev.cmplx.servertweaks.tweaks;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UnlockAll implements Listener {
	
	private static Collection<NamespacedKey> discoverable;

	public UnlockAll() {
		discoverable = new ArrayList<>();
		Bukkit.recipeIterator().forEachRemaining((r) -> {
			if(r instanceof Keyed) discoverable.add(((Keyed)r).getKey());
		});
	}

	@EventHandler
	public void onJoinRecipeUnlock(PlayerJoinEvent e) {
		e.getPlayer().discoverRecipes(discoverable);
	}

}
