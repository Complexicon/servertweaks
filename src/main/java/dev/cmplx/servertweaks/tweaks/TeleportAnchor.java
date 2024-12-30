package dev.cmplx.servertweaks.tweaks;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.serializable.TeleportAnchorContainer;
import dev.cmplx.servertweaks.serializable.TeleportAnchorContainer.TeleportAnchorInfo;

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

	TeleportAnchorContainer getContainer() {
		var container = Util.getPersistentSerializable(primaryWorld, teleportAnchor, TeleportAnchorContainer.class);
		if (container == null) container = new TeleportAnchorContainer();
		return container;
	}

	void updateContainer(Consumer<TeleportAnchorContainer> callback) {
		var container = getContainer();
		callback.accept(container);
		try {
			Util.setPersistentSerialized(primaryWorld, teleportAnchor, container);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	class WaypointGUI extends InventoryGUI {

		public WaypointGUI(UUID origin) {
			super("Waypoints");
			var anchors = getContainer().anchors.entrySet().stream().toList();

			for (Entry<UUID,TeleportAnchorInfo> entry : anchors) {
				var anchorID = entry.getKey();
				var info = entry.getValue();

				var pearl = new ItemStack(anchorID.equals(origin) ? Material.ENDER_EYE : Material.ENDER_PEARL);
				var meta = pearl.getItemMeta();

				meta.setDisplayName(info.name + (anchorID.equals(origin) ? Util.fixColor("&b (Currently Here)") : ""));
				meta.setLore(Arrays.asList(Bukkit.getWorld(info.dim).getName(), "X: " + info.x + " Y: " + info.y + " Z: " + info.z));

				pearl.setItemMeta(meta);

				try {
					addItem(pearl, e -> {
						var player = (Player) e.getWhoClicked();

						player.closeInventory();
						player.teleport(new Location(Bukkit.getWorld(info.dim), info.x, info.y, info.z));
						player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2);
						player.spawnParticle(Particle.TOTEM, player.getLocation(), 100, 1,1,1);
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		
	}

	// class WaypointChooser implements InventoryHolder {

	// 	Inventory inv;
	// 	List<Entry<UUID, TeleportAnchorInfo>> anchors;
		
	// 	public WaypointChooser(UUID opener) {

	// 		inv = Bukkit.createInventory(this, 27, "Waypoints");

	// 		anchors = getContainer().anchors.entrySet().stream().toList();

	// 		for (int i = 0; i < anchors.size(); i++) {
	// 			var anchorInfo = anchors.get(i);

	// 			var pearl = new ItemStack(anchorInfo.getKey().equals(opener) ? Material.ENDER_EYE : Material.ENDER_PEARL);
	// 			var meta = pearl.getItemMeta();

	// 			var info = anchorInfo.getValue();

	// 			meta.setDisplayName(info.name + (anchorInfo.getKey().equals(opener) ? Util.fixColor("&b (Currently Here)") : ""));
	// 			meta.setLore(Arrays.asList(Bukkit.getWorld(info.dim).getName(), "X: " + info.x + " Y: " + info.y + " Z: " + info.z));

	// 			pearl.setItemMeta(meta);

	// 			inv.setItem(i, pearl);
	// 		}
	// 	}

	// 	public void onClick(InventoryClickEvent e) {
	// 		if (e.getCurrentItem() == null) return;

	// 		var anchorInfo = anchors.get(e.getSlot());
	// 		var info = anchorInfo.getValue();
			
	// 		var player = (Player) e.getWhoClicked();

	// 		player.closeInventory();
	// 		player.teleport(new Location(Bukkit.getWorld(info.dim), info.x, info.y, info.z));
	// 		player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2);
	// 		player.spawnParticle(Particle.TOTEM, player.getLocation(), 100, 1,1,1);
	// 	}

	// 	@Override
	// 	public Inventory getInventory() {
	// 		return inv;
	// 	}
		
	// }

	// @EventHandler
	// public void onTPInvInteract(InventoryClickEvent e) {
	// 	if (e.getInventory().getHolder() instanceof WaypointChooser w) {
	// 		e.setCancelled(true);
	// 		w.onClick(e);
	// 	}
	// }

	private boolean isTeleportAnchor(Block b) {
		if (!(b.getState() instanceof Lectern)) return false;

		Lectern lectern = (Lectern) b.getState();

		if (lectern.getInventory().getItem(0) == null) return false;
		
		return Util.getPersistentString(lectern, teleportAnchor) != null;
	}

	private void registerAnchor(Lectern l) throws Exception {
		var id = UUID.randomUUID();

		updateContainer(container -> container.anchors.put(id, new TeleportAnchorInfo(l)));

		Util.setPersistent(l, teleportAnchor, id.toString());
		l.update();
		l.getInventory().setItem(0, new ItemStack(Material.WRITABLE_BOOK));
		l.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, l.getLocation(), 40, 1,1,1);
		l.getWorld().playSound(l.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);



		Log.debug("Anchor Created");
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

		updateContainer(container -> container.anchors.remove(id));

		Log.debug("Anchor removed");
	}

	@EventHandler
	public void onCreateTeleportAnchor(PlayerInteractEvent e) throws Exception {

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!(e.getClickedBlock().getState() instanceof Lectern)) return;
		if (e.getItem() == null) return;
		if (e.getItem().getType() != Material.ENCHANTED_BOOK) return;
		// if (!Util.getPersistentBool(e.getItem().getItemMeta(), teleportAnchor)) return;

		Lectern lectern = (Lectern) e.getClickedBlock().getState();

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
		if (!(
			e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() &&
			e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName())) return;

		var nametag = e.getPlayer().getInventory().getItemInMainHand();
		var newName = nametag.getItemMeta().getDisplayName();

		UUID id = UUID.fromString(Util.getPersistentString(l, teleportAnchor));

		updateContainer(container -> {
			var anchorMeta = container.anchors.get(id);
			anchorMeta.name = newName;
			container.anchors.put(id, anchorMeta);
		});

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
