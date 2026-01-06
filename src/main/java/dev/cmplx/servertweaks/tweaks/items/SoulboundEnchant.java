package dev.cmplx.servertweaks.tweaks.items;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;

public class SoulboundEnchant implements Listener {

	public static final NamespacedKey enchantKey = new NamespacedKey(Main.pluginRef, "soulbound");
	public static final ItemStack book = 
		new ItemStackBuilder(Material.ENCHANTED_BOOK)
		.setLore("&5Seelengebunden")
		.setPersistent(enchantKey, true)
		.build();

	static { DebugItemsCommand.DebugItems.add(book); }

	@EventHandler
	public void onAnvilCraft(PrepareAnvilEvent e) {
		var anvil = e.getInventory();
		var first = anvil.getItem(0);
		var second = anvil.getItem(1);

		if (first == null || second == null)
			return;

		if(first.getAmount() != 1)
			return;

		if(!Util.getPersistentBool(second.getItemMeta(), enchantKey))
			return;

		Util.applyCustomBook(e, enchantKey, "&5Seelengebunden");
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		var soulboundItems = new ArrayList<ItemStack>();
		var unboundItems = new ArrayList<ItemStack>();
		for (var i : e.getDrops()) {
			if (Util.getPersistentBool(i.getItemMeta(), enchantKey)) {
				soulboundItems.add(i);
			} else {
				unboundItems.add(i);
			}
		}

		e.getDrops().clear();
		e.getDrops().addAll(unboundItems);

		Util.setMetadata(e.getEntity(), "soulboundItems", soulboundItems);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (e.getRespawnReason() != RespawnReason.DEATH) return; // prevent duping of soulbound items when going through end portal

		@SuppressWarnings("unchecked")
		var items = (ArrayList<ItemStack>) Util.getMetadata(e.getPlayer(), "soulboundItems", new ArrayList<ItemStack>().getClass());
		if (items == null) return;
		e.getPlayer().getInventory().addItem(items.toArray(new ItemStack[]{}));
	}

}
