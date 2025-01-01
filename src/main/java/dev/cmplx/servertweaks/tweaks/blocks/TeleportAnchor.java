package dev.cmplx.servertweaks.tweaks.blocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.serializable.NamedLocation;

public class TeleportAnchor implements Listener {

	public static NamespacedKey teleportAnchor = new NamespacedKey(Main.pluginRef, "teleportAnchor");

	public static final ItemStack teleportBook = new ItemStack(Material.ENCHANTED_BOOK);

	static {
		var meta = teleportBook.getItemMeta();
		meta.setLore(Arrays.asList("Teleportation Book"));
		Util.setPersistent(meta, teleportAnchor, true);
		teleportBook.setItemMeta(meta);
		DebugItemsCommand.DebugItems.add(teleportBook);
	}

	World primaryWorld;

	public TeleportAnchor() {
		primaryWorld = Bukkit.getWorlds().get(0);
	}

	Map<UUID, NamedLocation> getAnchors() {
		var empty = new HashMap<UUID, NamedLocation>();
		@SuppressWarnings("unchecked")
		Map<UUID, NamedLocation> data = Util.getPersistentSerializable(primaryWorld, teleportAnchor, empty.getClass());
		if (data == null) return empty;
		return data;
	}

	public void setAnchors(Map<UUID, NamedLocation> newData) {
		try {
			Util.setPersistentSerialized(primaryWorld, teleportAnchor, (HashMap<UUID, NamedLocation>)newData);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} 

	class WaypointGUI extends InventoryGUI {

		public WaypointGUI(UUID origin) {
			super("Waypoints");

			for (var entry : getAnchors().entrySet()) {
				var anchorID = entry.getKey();
				var info = entry.getValue();

				var pearl = new ItemStack(anchorID.equals(origin) ? Material.ENDER_EYE : Material.ENDER_PEARL);
				var meta = pearl.getItemMeta();

				meta.setDisplayName(info.name + (anchorID.equals(origin) ? Util.fixColor("&b (Currently Here)") : ""));
				meta.setLore(Arrays.asList(Bukkit.getWorld(info.dimension).getName(), "X: " + info.x + " Y: " + info.y + " Z: " + info.z));

				pearl.setItemMeta(meta);

				addItem(pearl, e -> {
					var player = (Player) e.getWhoClicked();

					player.closeInventory();
					player.teleport(info.asLocation());
					player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2);
					player.spawnParticle(Particle.TOTEM, player.getLocation(), 100, 1,1,1);
				});
			}
		}
		
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
		l.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, l.getLocation(), 40, 1,1,1);
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

		e.setCancelled(true);
		e.getPlayer().openInventory(new WaypointGUI(id).getInventory());
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
