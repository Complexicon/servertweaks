package dev.cmplx.servertweaks.items;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.InventoryGUI;
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.serializable.WaypointContainer;
import dev.cmplx.servertweaks.serializable.WaypointContainer.WaypointInfo;

public class GPSCompass implements Listener {

	public static NamespacedKey gpsEnabled = new NamespacedKey(Main.pluginRef, "gps_enabled");
	public static NamespacedKey gpsWaypoints = new NamespacedKey(Main.pluginRef, "gps_waypoints");

	static {
		ItemStack gpsCompass = new ItemStack(Material.COMPASS);
		var meta = gpsCompass.getItemMeta();
		meta.setDisplayName(Util.fixColor("&5GPS Compass"));
		Util.setPersistent(meta, gpsEnabled, true);
		gpsCompass.setItemMeta(meta);
		DebugItemsCommand.DebugItems.add(gpsCompass);
	}

	class WaypointGUI extends InventoryGUI {

		public WaypointGUI(Player p) {
			super("Waypoints");

			var container = Util.getPersistentSerializable(p, gpsWaypoints, WaypointContainer.class);
			if (container == null) container = new WaypointContainer();

			var waypoints = container.waypoints.entrySet().stream().toList();

			for (Entry<UUID, WaypointInfo> entry : waypoints) {
				var anchorID = entry.getKey();
				var info = entry.getValue();

				var pearl = new ItemStack(Material.COMPASS);
				var meta = pearl.getItemMeta();

				meta.setLore(Arrays.asList(Bukkit.getWorld(info.dim).getName(), "X: " + info.x + " Y: " + info.y + " Z: " + info.z));

				pearl.setItemMeta(meta);

				try {
					addItem(pearl, e -> {
						var player = (Player) e.getWhoClicked();

						player.closeInventory();
						Log.debug("targeting waypoint");
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	@EventHandler
	public void onOpenGPS(PlayerInteractEvent e) {
		if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		if (e.getItem() == null) return;
		if (e.getItem().getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(e.getItem().getItemMeta(), gpsEnabled)) return;

		e.getPlayer().openInventory(new WaypointGUI(e.getPlayer()).getInventory());
	}

	@EventHandler
	public void onAddWaypoint(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!e.getPlayer().isSneaking()) return;
		if (e.getItem() == null) return;
		if (!(e.getClickedBlock().getState() instanceof Sign s)) return;
		if (e.getItem().getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(e.getItem().getItemMeta(), gpsEnabled)) return;

		var container = Util.getPersistentSerializable(e.getPlayer(), gpsWaypoints, WaypointContainer.class);
		if (container == null) container = new WaypointContainer();

		container.waypoints.put(UUID.randomUUID(), new WaypointInfo(s));

		try {
			Util.setPersistentSerialized(e.getPlayer(), gpsWaypoints, container);
			e.getPlayer().sendMessage("added waypoint");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}
