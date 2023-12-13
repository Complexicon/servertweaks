package dev.cmplx.servertweaks.items;

import java.util.Arrays;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;

public class TimberEnchant implements Listener {

	public static NamespacedKey timberEntchant = new NamespacedKey(Main.pluginRef, "timber");

	@EventHandler
	public void onAnvilCraft(PrepareAnvilEvent e) {
		var anvil = e.getInventory();
		var first = anvil.getItem(0);
		var second = anvil.getItem(1);

		if (first == null || second == null)
			return;

		if(!first.getType().toString().endsWith("_AXE"))
			return;

		if(!Util.getPersistentBool(second.getItemMeta(), timberEntchant))
			return;

		var axe = first.clone();
		var meta = axe.getItemMeta();

		var lore = Arrays.asList(Util.fixColor("&6Timber Verzauberung"));

		if(meta.hasLore()) {
			lore = meta.getLore();
			lore.add(0, Util.fixColor("&6Timber Verzauberung"));
		}

		meta.setLore(lore);
		
		Util.setPersistent(meta, timberEntchant, true);

		axe.setItemMeta(meta);

		anvil.setRepairCost(0);
		e.setResult(axe);

	}

}
