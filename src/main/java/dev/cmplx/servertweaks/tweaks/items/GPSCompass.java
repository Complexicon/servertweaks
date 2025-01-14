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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import dev.cmplx.servertweaks.InventoryGUI;
import dev.cmplx.servertweaks.ItemStackBuilder;
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

	void stopTracking(Player p) {
		var compass = trackingPlayers.get(p);
		if(compass == null) return;
		var compassMeta = (CompassMeta) compass.getItemMeta();
		trackingPlayers.remove(p);
		compassMeta.setLodestone(null);
		compassMeta.setLodestoneTracked(true);
		compass.setItemMeta(compassMeta);
	}

	void updateTrackings() {
		for (var entry : trackingPlayers.entrySet()) {
			var p = entry.getKey();
			var compass = entry.getValue();
			var compassMeta = (CompassMeta) compass.getItemMeta();
			var targetLoc = compassMeta.getLodestone();
			var waypointName = Util.getPersistentString(compassMeta, gpsCurrentTracking);

			if (!p.getLocation().getWorld().getUID().equals(targetLoc.getWorld().getUID())) {
				stopTracking(p);
				return;
			}

			var dist = p.getLocation().distance(targetLoc);

			if (dist < 200) {
				
				for (int i = targetLoc.getBlockY(); i < targetLoc.getWorld().getMaxHeight(); i++) {
					p.spawnParticle(
						Particle.FIREWORK,
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
				stopTracking(p);
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.0f);
			}

			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(Util.fixColor("&6"+ (int)dist + "m away from " + waypointName)));

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

	void openWaypoints(Player p, ItemStack openingCompass) {

		var curWaypoints = new ArrayList<>(getWaypoints(p)
			.stream()
			.filter(v->p.getWorld().getUID().equals(v.dimension))  // dont display waypoints that are not in the current dimension 
			.toList());

		if (curWaypoints.size() == 0) {
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			p.sendMessage(Util.fixColor("&cKeine Wegpunkte gespeichert!"));
			return;
		}

		var gui = new InventoryGUI("Waypoints");

		for (var info : curWaypoints) {

			var lore = new ArrayList<>(info.prettyPrint());
			lore.add("&d&o(Q zum Entfernen drücken)");

			var displayItem = new ItemStackBuilder(Material.PAPER)
				.setName(info.name)
				.setLore(lore)
				.build();

			gui.addItem(displayItem, e -> {
				var player = (Player) e.getWhoClicked();
				
				if (e.getAction() == InventoryAction.DROP_ONE_SLOT) {
					var waypoints = getWaypoints(p);
					waypoints.remove(waypoints.indexOf(info));
					setWaypoints(p, waypoints);
					player.sendMessage(Util.fixColor("&7Wegpunkt " + info.name + " &7Entfernt."));
					player.closeInventory();
					return;
				}

				var compassMeta = (CompassMeta) openingCompass.getItemMeta();
				Util.setPersistent(compassMeta, gpsCurrentTracking, info.name);
				compassMeta.setLodestoneTracked(false);
				compassMeta.setLodestone(info.asLocation());
				openingCompass.setItemMeta(compassMeta);

				player.closeInventory();
				player.sendMessage(Util.fixColor("&6Google Maps berechnet die Route zu: " + info.name));
				
				trackingPlayers.put(p, openingCompass);
			});
		}

		p.openInventory(gui.getInventory());
	}

	boolean checkUntrack(ItemStack item, Player p) {
		if (item == null) return false;
		if (item.getType() != Material.COMPASS) return false;
		if (!Util.getPersistentBool(item.getItemMeta(), gpsEnabled)) return false;
		if (((CompassMeta)item.getItemMeta()).getLodestone() == null) return false; // has no tracked location
		
		trackingPlayers.remove(p);
		return true;
	}

	@EventHandler
	void onSwitchFromGPS(PlayerItemHeldEvent e) {
		checkUntrack(e.getPlayer().getInventory().getItem(e.getPreviousSlot()), e.getPlayer());
	}

	@EventHandler
	void onDropGPS(PlayerDropItemEvent e) {
		var item = e.getItemDrop().getItemStack();
		if(checkUntrack(item, e.getPlayer())) { // clear tracking on item drop
			var meta = (CompassMeta)item.getItemMeta();
			meta.setLodestone(null);
			item.setItemMeta(meta);
		}
	}

	void checkTrack(ItemStack item, Player p) {
		if (item == null) return;
		if (item.getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(item.getItemMeta(), gpsEnabled)) return;
		if (((CompassMeta)item.getItemMeta()).getLodestone() == null) return; // has no tracked location

		trackingPlayers.put(p, item);
	}

	@EventHandler
	void onHoldingGPS(PlayerItemHeldEvent e) {
		checkTrack(e.getPlayer().getInventory().getItem(e.getNewSlot()), e.getPlayer());
	}

	@EventHandler
	public void onOpenGPS(PlayerInteractEvent e) {
		if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		if (e.getItem() == null) return;
		if (e.getItem().getType() != Material.COMPASS) return;
		if (!Util.getPersistentBool(e.getItem().getItemMeta(), gpsEnabled)) return;
		
		Player p = e.getPlayer();

		openWaypoints(p, e.getItem());
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

		if (chatColor.equals("&0")) { // invert black with white, default sign color, cant read shit with black text
			chatColor = "&f";
		}
		
		signText = Util.fixColor(chatColor + signText);

		var existingWaypoint = waypoints.stream().filter(loc -> loc.asLocation().equals(s.getLocation())).findFirst();

		if (existingWaypoint.isPresent()) {

			if (!existingWaypoint.get().name.equals(signText)) {
				var newName = NamedLocation.fromLocation(existingWaypoint.get().asLocation(), signText);

				waypoints.set(waypoints.indexOf(existingWaypoint.get()), newName);
				e.getPlayer().sendMessage(Util.fixColor("&aWegpunktname geupdated!"));

				setWaypoints(e.getPlayer(), waypoints);
			}

			return;
		}

		waypoints.add(NamedLocation.fromLocation(s.getLocation(), signText));
		setWaypoints(e.getPlayer(), waypoints);
		e.getPlayer().sendMessage(Util.fixColor("&aWegpunkt hinzugefügt!"));
		
	}

}
