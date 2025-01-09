package dev.cmplx.servertweaks.tweaks.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.InventoryGUI;
import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.serializable.NamedLocation;

public class TeleportAnchor implements Listener {

	public static final NamespacedKey teleportAnchor = new NamespacedKey(Main.pluginRef, "teleportAnchor");
	public static final ItemStack teleportBook = 
		new ItemStackBuilder(Material.ENCHANTED_BOOK)
		.setLore("&dBuch der Teleportation", "&7&oBenutz mich auf einem Lectern :)")
		.setPersistent(teleportAnchor, true)
		.build();

	static { DebugItemsCommand.DebugItems.add(teleportBook); }

	World primaryWorld;

	public TeleportAnchor() {
		primaryWorld = Bukkit.getWorlds().get(0);
	}

	Map<UUID, NamedLocation> getAnchors() {
		var empty = new HashMap<UUID, NamedLocation>();
		@SuppressWarnings("unchecked")
		Map<UUID, NamedLocation> data = Util.getPersistentSerializable(primaryWorld, teleportAnchor, empty.getClass());
		if (data == null) return empty;

		List<UUID> toRemove = new ArrayList<>();

		for (var anchor : data.entrySet()) {
			var uuid = anchor.getKey();
			var info = anchor.getValue();
			var loc = info.asLocation();
			loc.getWorld().getChunkAt(loc); // temp load chunk

			if (!isTeleportAnchor(loc.getWorld().getBlockAt(loc))) {
				toRemove.add(uuid);
				Log.info("Corrupted Teleport Anchor detected at: " + ChatColor.stripColor(Util.fixColor(String.join(", ", info.prettyPrint()))));
			}
		}

		for (UUID uuid : toRemove) {
			data.remove(uuid);
		}

		return data;
	}

	public void setAnchors(Map<UUID, NamedLocation> newData) {
		try {
			Util.setPersistentSerialized(primaryWorld, teleportAnchor, (HashMap<UUID, NamedLocation>)newData);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	Map<UUID, NamedLocation> getPlayerDiscoveredAnchors(Player p) {

		var empty = new ArrayList<UUID>();
		@SuppressWarnings("unchecked")
		List<UUID> data = Util.getPersistentSerializable(p, teleportAnchor, empty.getClass());
		if (data == null) return new HashMap<UUID, NamedLocation>();

		var allAnchors = getAnchors();

		return allAnchors
		.entrySet()
		.stream()
		.filter(v -> data.contains(v.getKey()))
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	void discoverAnchor(UUID anchor, Player p) {
		var playerAnchors = getPlayerDiscoveredAnchors(p);
		if (playerAnchors.containsKey(anchor)) return;

		var discoveredList = new ArrayList<>(playerAnchors.keySet());
		discoveredList.add(anchor);
		
		try {
			Util.setPersistentSerialized(p, teleportAnchor, discoveredList);
			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			p.sendMessage(Util.fixColor("&aNeuen Wegpunkt entdeckt!"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void openWaypoints(UUID origin, Player p) {
		var gui = new InventoryGUI("Waypoints");

		for (var entry : getPlayerDiscoveredAnchors(p).entrySet()) {
			var anchorID = entry.getKey();
			var info = entry.getValue();

			var pearl = new ItemStackBuilder(anchorID.equals(origin) ? Material.ENDER_EYE : Material.ENDER_PEARL)
				.setName(info.name + (anchorID.equals(origin) ? Util.fixColor("&b (Currently Here)") : ""))
				.setLore(info.prettyPrint())
				.build();

			gui.addItem(pearl, e -> {
				var player = (Player) e.getWhoClicked();

				player.closeInventory();
				player.teleport(info.asLocation());
				player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2);
				player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 100, 1,1,1);
			});
		}

		p.openInventory(gui.getInventory());

	}

	private boolean isTeleportAnchor(Block b) {
		if (!(b.getState() instanceof Lectern)) return false;

		Lectern lectern = (Lectern) b.getState();

		if (lectern.getInventory().getItem(0) == null) return false;
		
		return Util.getPersistentString(lectern, teleportAnchor) != null;
	}

	private void registerAnchor(Lectern l) throws Exception {
		var id = UUID.randomUUID();

		var anchors = getAnchors();
		anchors.put(id, NamedLocation.fromLocation(l.getLocation(), "Teleport Anchor"));
		setAnchors(anchors);

		Util.setPersistent(l, teleportAnchor, id.toString());
		l.update();
		l.getInventory().setItem(0, new ItemStack(Material.WRITABLE_BOOK));
		l.getWorld().spawnParticle(Particle.ENCHANT, l.getLocation(), 40, 1,1,1);
		l.getWorld().playSound(l.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);

		// Log.debug("Anchor Created");
	}

	private ArmorStand getLabelForAnchor(Lectern l) {
		var label = l.getWorld().getNearbyEntities(l.getLocation().clone().add(0.5, 0, 0.5), 0.25, 0.25, 0.25, e -> e instanceof ArmorStand);
		if(label.size() == 1) return (ArmorStand)label.stream().findFirst().get();
		return null;
	}

	private void deregisterAnchor(Lectern l) {
		l.getWorld().playSound(l.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);

		var label = getLabelForAnchor(l);
		
		if (label != null) {
			label.remove();
		}

		UUID id = UUID.fromString(Util.getPersistentString(l, teleportAnchor));

		var anchors = getAnchors();
		anchors.remove(id);
		setAnchors(anchors);

		// Log.debug("Anchor removed");
	}

	@EventHandler
	public void onCreateTeleportAnchor(PlayerInteractEvent e) throws Exception {

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!(e.getClickedBlock().getState() instanceof Lectern lectern)) return;
		if (e.getItem() == null) return;
		if (e.getItem().getType() != Material.ENCHANTED_BOOK) return;
		if (!Util.getPersistentBool(e.getItem().getItemMeta(), teleportAnchor)) return;

		if (lectern.getInventory().getItem(0) != null) return; // cant create on lectern that has book present

		registerAnchor(lectern);
		e.getItem().setAmount(0);

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onNameTeleportAnchor(InventoryOpenEvent e) {

		if (e.getInventory().getType() != InventoryType.LECTERN) return;
		if (!(e.getInventory().getHolder() instanceof Lectern l)) return;
		if (!isTeleportAnchor(l.getBlock())) return;
		if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.NAME_TAG) return;
		
		var nametag = e.getPlayer().getInventory().getItemInMainHand();
		
		if (!(nametag.hasItemMeta() && nametag.getItemMeta().hasDisplayName())) return;

		var newName = nametag.getItemMeta().getDisplayName();

		UUID id = UUID.fromString(Util.getPersistentString(l, teleportAnchor));

		var anchors = getAnchors();
		var namedLoc = anchors.get(id);
		anchors.put(id, NamedLocation.fromLocation(namedLoc.asLocation(), newName));
		setAnchors(anchors);

		e.getPlayer().sendMessage("Set Teleport Anchor Name to: " + newName);
		nametag.setAmount(nametag.getAmount() - 1);

		var label = getLabelForAnchor(l);
		
		if (label != null) {
			label.setCustomName(newName);
		} else {
			var armorStand = e.getPlayer().getWorld().spawn(
				l.getLocation().clone().add(0.5, 0.08, 0.5),
				ArmorStand.class
			);
			armorStand.setCustomName(newName);
			armorStand.setCustomNameVisible(true);
			armorStand.setGravity(false);
			armorStand.setCollidable(false);
			armorStand.setSmall(true);
		}
		
		e.setCancelled(true);
	}

	@EventHandler
	public void onUseTeleportAnchor(InventoryOpenEvent e) {
		if (e.getInventory().getType() != InventoryType.LECTERN) return;
		if (!(e.getInventory().getHolder() instanceof Lectern l)) return;
		if (!isTeleportAnchor(l.getBlock())) return;
		if(e.isCancelled()) return;

		UUID id = UUID.fromString(Util.getPersistentString(l, teleportAnchor));

		discoverAnchor(id, (Player)e.getPlayer());

		e.setCancelled(true);
		openWaypoints(id, (Player)e.getPlayer());
	}

	@EventHandler
	public void onTeleportAnchorBreak(BlockBreakEvent e) {
		if (!(e.getBlock().getState() instanceof Lectern l)) return;
		if (!isTeleportAnchor(e.getBlock())) return;

		deregisterAnchor(l);

		l.getInventory().setItem(0, null);
		e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(0.5, 0.5, 0.5), teleportBook);
	}

	@EventHandler
	public void onTeleportAnchorBreakExplode(EntityExplodeEvent e) {
		var teleporters = e.blockList().stream().filter(v -> isTeleportAnchor(v));
		
		teleporters.forEach(block -> {
			block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), teleportBook);
			var l = (Lectern) block.getState();
			l.getInventory().setItem(0, null);
			deregisterAnchor(l);
		});
	}

}
