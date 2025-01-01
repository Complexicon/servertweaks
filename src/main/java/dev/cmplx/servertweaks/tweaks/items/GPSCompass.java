package dev.cmplx.servertweaks.tweaks.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import dev.cmplx.servertweaks.InventoryGUI;
import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.serializable.NamedLocation;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class GPSCompass implements Listener {

	public static NamespacedKey gpsEnabled = new NamespacedKey(Main.pluginRef, "gps_enabled");
	public static NamespacedKey gpsWaypoints = new NamespacedKey(Main.pluginRef, "gps_waypoints");
	public static NamespacedKey gpsCurrentTracking = new NamespacedKey(Main.pluginRef, "gps_current_tracking");
	public static ItemStack gpsCompass = new ItemStackBuilder(Material.COMPASS).setName("&5GPS Compass").setPersistent(gpsEnabled, true).build();

	static { DebugItemsCommand.DebugItems.add(gpsCompass); }

	Map<Player, ItemStack> trackingPlayers = new HashMap<>();

	public GPSCompass() {
		Bukkit.getScheduler().runTaskTimer(Main.pluginRef, () -> updateTrackings(), 0, 10); // update trackings every 500ms (10 ticks)
	}

	void updateTrackings() {
		for (var entry : trackingPlayers.entrySet()) {
			var p = entry.getKey();
			var compass = entry.getValue();
			var compassMeta = (CompassMeta) compass.getItemMeta();
			var targetLoc = compassMeta.getLodestone();
			var waypointName = Util.getPersistentString(compassMeta, gpsCurrentTracking);

			var dist = p.getLocation().distance(targetLoc);

			if (dist < 200) {
				
				for (int i = targetLoc.getBlockY(); i < targetLoc.getWorld().getMaxHeight(); i++) {
					p.spawnParticle(
						Particle.FIREWORKS_SPARK,
						new Location(targetLoc.getWorld(), targetLoc.getX(), i, targetLoc.getZ()),
						2,
						0.3D,
						0D,
						0.3D,
						0
					);
				}
			}

			if (dist < 10) { // auto stop tracking if in close proximity
				trackingPlayers.remove(p);
				compassMeta.setLodestone(null);
				compass.setItemMeta(compassMeta);
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.0f);
			}

			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Util.fixColor("&6"+ (int)dist + "m away from " + waypointName)));

		}
	}

	List<NamedLocation> getWaypoints(Player p) {
		var empty = new ArrayList<NamedLocation>();
		@SuppressWarnings("unchecked")
		List<NamedLocation> data = Util.getPersistentSerializable(p, gpsWaypoints, empty.getClass());
		if (data == null) return empty;
		return data;
	}

	public void setWaypoints(Player p, List<NamedLocation> newData) {
		try {
			Util.setPersistentSerialized(p, gpsWaypoints, (ArrayList<NamedLocation>)newData);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} 

	class WaypointGUI extends InventoryGUI {

		public WaypointGUI(Player p, ItemStack openingCompass) {
			super("Waypoints");

			for (var info : getWaypoints(p)) {

				if (!p.getWorld().getUID().equals(info.dimension)) {
					continue; // dont display waypoints that are not in the current dimension 
				}

				var displayItem = new ItemStackBuilder(Material.PAPER)
					.setName(info.name)
					.setLore(
						Bukkit.getWorld(info.dimension).getName(),
						"X: " + info.x + " Y: " + info.y + " Z: " + info.z
					)
					.build();

				addItem(displayItem, e -> {
					var player = (Player) e.getWhoClicked();
					
					if (e.getClick() == ClickType.MIDDLE) {
						var waypoints = getWaypoints(p);
						waypoints.remove(waypoints.indexOf(info));
						setWaypoints(p, waypoints);
						Log.debug("removed");
						player.closeInventory();
						return;
					}

					var compassMeta = (CompassMeta) openingCompass.getItemMeta();
					Util.setPersistent(compassMeta, gpsCurrentTracking, info.name);
					compassMeta.setLodestoneTracked(false);
					compassMeta.setLodestone(info.asLocation());
					openingCompass.setItemMeta(compassMeta);

					player.closeInventory();
					Log.debug("targeting waypoint");
					trackingPlayers.put(p, openingCompass);
				});
			}
		}

	}

	@EventHandler
	public void onSwitchFromGPS(PlayerItemHeldEvent e) {
		var oldItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
		if (oldItem == null) return;
		if (oldItem.getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(oldItem.getItemMeta(), gpsEnabled)) return;
		if (((CompassMeta)oldItem.getItemMeta()).getLodestone() == null) return; // has no tracked location
		
		trackingPlayers.remove(e.getPlayer());
		// Log.debug("no longer holding");
	}

	@EventHandler
	public void onHoldingGPS(PlayerItemHeldEvent e) {
		var newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
		if (newItem == null) return;
		if (newItem.getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(newItem.getItemMeta(), gpsEnabled)) return;
		if (((CompassMeta)newItem.getItemMeta()).getLodestone() == null) return; // has no tracked location

		trackingPlayers.put(e.getPlayer(), newItem);
		// Log.debug("now holding");
	}

	@EventHandler
	public void onOpenGPS(PlayerInteractEvent e) {
		if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		if (e.getItem() == null) return;
		if (e.getItem().getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(e.getItem().getItemMeta(), gpsEnabled)) return;
		
		if (getWaypoints(e.getPlayer()).size() == 0) {
			e.getPlayer().sendMessage("no waypoints");
			return;
		}

		e.getPlayer().openInventory(new WaypointGUI(e.getPlayer(), e.getItem()).getInventory());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onAddWaypoint(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!e.getPlayer().isSneaking()) return;
		if (e.getItem() == null) return;
		if (!(e.getClickedBlock().getState() instanceof Sign s)) return;
		if (e.getItem().getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(e.getItem().getItemMeta(), gpsEnabled)) return;

		var waypoints = getWaypoints(e.getPlayer());
		var signText = s.getSide(Side.FRONT).getLine(0);
		var chatColor = Util.toColorCode(s.getSide(Side.FRONT).getColor());

		if (chatColor == "&0") { // invert black with white, default sign color, cant read shit with black text
			chatColor = "&f";
		}
		
		signText = Util.fixColor(chatColor + signText);

		var existingWaypoint = waypoints.stream().filter(loc -> loc.asLocation().equals(s.getLocation())).findFirst();

		if (existingWaypoint.isPresent()) {

			if (!existingWaypoint.get().name.equals(signText)) {
				var newName = NamedLocation.fromLocation(existingWaypoint.get().asLocation(), signText);

				waypoints.set(waypoints.indexOf(existingWaypoint.get()), newName);
				e.getPlayer().sendMessage("updated waypoint name");

				setWaypoints(e.getPlayer(), waypoints);
			}

			return;
		}

		waypoints.add(NamedLocation.fromLocation(s.getLocation(), signText));
		setWaypoints(e.getPlayer(), waypoints);
		e.getPlayer().sendMessage("added waypoint");
		
	}

}
