package dev.cmplx.servertweaks.tweaks;

import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;

import dev.cmplx.servertweaks.Config;
import dev.cmplx.servertweaks.tweaks.blocks.TeleportAnchor;
import dev.cmplx.servertweaks.tweaks.items.SoulboundEnchant;
import dev.cmplx.servertweaks.tweaks.items.TimberEnchant;

public class LootGenerateHook implements Listener {

	Random rng;

	public LootGenerateHook(){
		rng = new Random();
	}

	boolean isLootTable(LootGenerateEvent e, LootTables table) {
		return e.getLootTable().equals(table.getLootTable());
	}

	void addItemWithChance(LootGenerateEvent e, int percent, ItemStack item) {
		if(rng.nextInt(100) + 1 <= percent) {
			e.getLoot().add(item);
		}
	}

	@EventHandler
	void onLootGen(LootGenerateEvent e) {
		if (isLootTable(e, LootTables.ANCIENT_CITY)) {
			if(Config.teleportAnchors)
				addItemWithChance(e, Config.chanceTeleportBook, TeleportAnchor.teleportBook);
				
			if(Config.timberMod)
				addItemWithChance(e, Config.chanceTimberEnchant, TimberEnchant.timberBook);
		}

		if (isLootTable(e, LootTables.END_CITY_TREASURE)) {
			if (Config.soulboundBook)
				addItemWithChance(e, Config.chanceSoulbound, SoulboundEnchant.book);
		}

	}

}
