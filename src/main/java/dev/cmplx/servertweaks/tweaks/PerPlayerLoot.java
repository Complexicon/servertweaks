package dev.cmplx.servertweaks.tweaks;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;

public class PerPlayerLoot implements Listener {

	static final NamespacedKey per_player_loot = new NamespacedKey(Main.pluginRef, "per_player_loot");

	static final Random rng = new Random();
	static final Gson gson = new Gson();
	static final Type lootMap = new TypeToken<Map<UUID, Object[]>>(){}.getType();

	LootTable getLootTable(BlockState blockstate) {
		if (!((blockstate instanceof Chest) || (blockstate instanceof Barrel))) return null;
		return ((Lootable) blockstate).getLootTable();
	}

	boolean isLootable(Block b) {
		return getLootTable(b.getState()) != null;
	}

	@EventHandler
	void onTryLoot(PlayerInteractEvent e) {
		if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		var blockstate = e.getClickedBlock().getState();
		var lootTable = getLootTable(blockstate);
		if (lootTable == null) return;

		var container = (Container) blockstate;

		e.setCancelled(true);

		var inventories = Util.getPersistentString(container, per_player_loot);

		if (inventories == null) {
			inventories = "{}";
		}

		var invToShow = Bukkit.createInventory(null, container.getSnapshotInventory().getSize());

		Map<UUID, Object[]> perPlayerLoot = gson.fromJson(inventories, lootMap);

		var playerEntry = perPlayerLoot.get(e.getPlayer().getUniqueId());

		if (playerEntry != null) {

			@SuppressWarnings("unchecked")
			var contents = Arrays.asList(playerEntry).stream().map(o -> o != null ? ItemStack.deserialize((Map<String, Object>)o) : null).toArray(ItemStack[]::new);
			invToShow.setContents(contents);
		
		} else {
		
			lootTable.fillInventory(invToShow, rng, new LootContext.Builder(container.getLocation()).build());
			perPlayerLoot.put(e.getPlayer().getUniqueId(), Arrays.asList(invToShow.getContents()).stream().map(v -> v != null ? v.serialize() : null).toArray());
			Util.setPersistent(container, per_player_loot, gson.toJson(perPlayerLoot));
			container.update(true);
		
		}

		e.getPlayer().openInventory(invToShow);

	}

	@EventHandler
	void onTryBreakLootChest(BlockBreakEvent e) {
		if (!isLootable(e.getBlock())) return;
		
		if (!e.getPlayer().isSneaking()) {
			e.getPlayer().sendMessage("nix da loot kiste kaput machen");
			e.setCancelled(true);
		} else {
			e.setDropItems(false);
		}
	}

}