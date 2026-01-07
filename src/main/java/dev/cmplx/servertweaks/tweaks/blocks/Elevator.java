package dev.cmplx.servertweaks.tweaks.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.SculkCatalyst;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;

public class Elevator implements Listener {
	public static NamespacedKey elevator = new NamespacedKey(Main.pluginRef, "elevator");
	public static ItemStack elevatorBlock = new ItemStackBuilder(Material.SCULK_CATALYST).setName("&5Elevator").setPersistent(elevator, true).build();

	static { DebugItemsCommand.DebugItems.add(elevatorBlock); }

	boolean isElevator(ItemStack is) {
		if (!is.hasItemMeta()) return false;
		return Util.getPersistentBool(is.getItemMeta(), elevator);
	}

	@EventHandler
	void onElevatorPlace(BlockPlaceEvent e) {
		if (!isElevator(e.getItemInHand())) return;
		var bs = (SculkCatalyst) e.getBlockPlaced().getState();
		Util.setPersistent(bs, elevator, true);
		bs.update(true);
	}

	@EventHandler
	void onBlockBreak(BlockDropItemEvent e) {
		if (!(e.getBlockState() instanceof SculkCatalyst sc)) return;
		if (!Util.getPersistentBool(sc, elevator)) return;

		var item = e.getItems().stream().filter(v -> v.getItemStack().getType() == Material.SCULK_CATALYST).findFirst();
		if (!item.isPresent()) return;

		var itemstack = item.get().getItemStack();
		var meta = itemstack.getItemMeta();
		meta.setDisplayName(elevatorBlock.getItemMeta().getDisplayName());
		Util.setPersistent(meta, elevator, true);
		itemstack.setItemMeta(meta);
	}

	Block findNextElevator(int direction, Location from) {
		return null;
	}

	@EventHandler
	void onElevatorUp(PlayerInputEvent e) {
		if (!e.getInput().isJump()) return;

		var b = e.getPlayer().getLocation().add(0, -1, 0).getBlock();
		if (b.getType() != Material.SCULK_CATALYST) return;
		var sc = (SculkCatalyst) b.getState();
		Log.debug(Util.getPersistentBool(sc, elevator));

		var target = findNextElevator(1, b.getLocation());

		if (target == null) return;
	}


}
