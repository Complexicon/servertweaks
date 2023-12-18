package dev.cmplx.servertweaks.tweaks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;

public class LockableChest implements Listener {

	static NamespacedKey chestLock = new NamespacedKey(Main.pluginRef, "allowedPlayers");
	static NamespacedKey chestPin = new NamespacedKey(Main.pluginRef, "pin");

	class PinView implements InventoryHolder {

		Inventory pinView;
		String targetPin = "0000";
		String firstPass = "";
		String currentPin = "";
		boolean isSetup = false;

		Chest left;
		Chest right;

		Player viewingPlayer;

		final ItemStack blank = Util.createHead("473fd8a06e6ea820794cda214fc46b4c329fa9af324e44eab4496c2d9f5ba6fd",
				"leer");
		final ItemStack confirm = withName(Material.GREEN_WOOL, "Bestätigen");
		final ItemStack delete = withName(Material.RED_WOOL, "Löschen");

		void setItem(int x, int y, ItemStack stack) {
			pinView.setItem(y * 9 + x, stack);
		}

		int entered = 0;

		static ItemStack withName(Material m, String name) {
			var item = new ItemStack(m);
			var meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
			return item;
		}

		void reset() {
			setItem(2, 0, blank);
			setItem(3, 0, blank);
			setItem(4, 0, blank);
			setItem(5, 0, blank);

			if (isSetup) {
				setItem(6, 0, delete);
			} else {
				setItem(6, 0, new ItemStack(Material.GRAY_WOOL));
			}

			entered = 0;
			currentPin = "";
		}

		public PinView(Player displayFor, boolean isSetup, Chest left, Chest right) {
			pinView = Bukkit.createInventory(this, 9 * 6, "Pinfeld" + (isSetup ? " - Einrichtung" : ""));
			this.isSetup = isSetup;
			this.left = left;
			this.right = right;
			targetPin = Util.getPersistentString(left, chestPin);
			viewingPlayer = displayFor;
			var grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
			for (int i = 0; i < pinView.getSize(); i++) {
				pinView.setItem(i, grayPane);
			}

			reset();

			setItem(4, 5, Util.numbers[0]);

			setItem(5, 4, Util.numbers[9]);
			setItem(4, 4, Util.numbers[8]);
			setItem(3, 4, Util.numbers[7]);

			setItem(5, 3, Util.numbers[6]);
			setItem(4, 3, Util.numbers[5]);
			setItem(3, 3, Util.numbers[4]);

			setItem(5, 2, Util.numbers[3]);
			setItem(4, 2, Util.numbers[2]);
			setItem(3, 2, Util.numbers[1]);

			if (isSetup) {
				setItem(8, 5, confirm);
			}

		}

		@Override
		public Inventory getInventory() {
			return pinView;
		}

		final List<String> num = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

		public void click(ItemStack clicked) {

			if (isSetup && clicked.isSimilar(delete)) {
				if (entered == 0)
					return;
				setItem(2 + --entered, 0, blank);
				currentPin = currentPin.substring(0, currentPin.length() - 1);
			}

			if (isSetup && clicked.isSimilar(confirm)) {
				if (entered != 4)
					return;
				if (!firstPass.isEmpty()) {

					if (!firstPass.equals(currentPin)) {
						viewingPlayer.playSound(viewingPlayer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						viewingPlayer.sendMessage(Util.fixColor("&cPin stimmt nicht mit vorheriger eingabe überein!"));
						return;
					}

					viewingPlayer.playSound(viewingPlayer.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1, 1);
					viewingPlayer.sendMessage(Util.fixColor("&aPin erfolgreich gesetzt"));

					Util.setPersistent(left, chestLock, Arrays.asList(viewingPlayer.getUniqueId().toString()));
					Util.setPersistent(left, chestPin, currentPin);
					left.update();
					if (right != null) {
						Util.setPersistent(right, chestLock, Arrays.asList(viewingPlayer.getUniqueId().toString()));
						Util.setPersistent(right, chestPin, currentPin);
						right.update();
					}

					viewingPlayer.openInventory(left.getInventory());

					return;
				}
				viewingPlayer.sendMessage(Util.fixColor("&bPin erneut eingeben zum bestätigen!"));
				viewingPlayer.playSound(viewingPlayer.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 1);
				firstPass = currentPin;
				reset();
			}

			if (entered == 4)
				return;
			if (!num.contains(clicked.getItemMeta().getDisplayName()))
				return;

			setItem(2 + entered++, 0, clicked);
			currentPin += clicked.getItemMeta().getDisplayName();

			if (entered == 4 && !isSetup) {
				if (currentPin.equals(targetPin)) {
					setItem(6, 0, new ItemStack(Material.GREEN_WOOL));
					viewingPlayer.playSound(viewingPlayer.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1, 1);
					viewingPlayer.sendMessage(Util.fixColor("&aDu hast nun zugriff auf diese Kiste!"));

					var allowed = new ArrayList<String>(Util.getPersistentStringList(left, chestLock));
					allowed.add(viewingPlayer.getUniqueId().toString());

					Util.setPersistent(left, chestLock, allowed);
					left.update();
					if (right != null) {
						Util.setPersistent(right, chestLock, allowed);
						right.update();
					}

					viewingPlayer.openInventory(left.getInventory());

				} else {
					setItem(6, 0, new ItemStack(Material.RED_WOOL));
					viewingPlayer.playSound(viewingPlayer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					viewingPlayer.sendMessage(Util.fixColor("&cPin ist inkorrekt!"));
					Util.scheduler.runTaskLater(Main.pluginRef, () -> reset(), 60L);
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getInventory().getHolder() instanceof PinView))
			return;

		e.setCancelled(true);
		
		if (e.getClick() != ClickType.LEFT)
			return;

		if (e.getCurrentItem() == null)
			return;

		PinView pinview = (PinView) e.getInventory().getHolder();
		pinview.click(e.getCurrentItem());

	}

	@EventHandler
	public void onLockChest(PlayerInteractEvent e) {
		if (!e.hasBlock() || !e.hasItem())
			return;
		if (e.getClickedBlock().getType() != Material.CHEST)
			return;
		if (e.getItem().getType() != Material.GOLD_NUGGET)
			return;
		if (!e.getPlayer().isSneaking())
			return;

		var holder = ((Chest) e.getClickedBlock().getState()).getInventory().getHolder();

		Chest left;
		Chest right = null;

		if (holder instanceof Chest) {
			left = (Chest) holder;
		} else if (holder instanceof DoubleChest) {
			DoubleChest c = (DoubleChest) holder;
			left = (Chest) c.getLeftSide();
			right = (Chest) c.getRightSide();
		} else {
			return; // ????
		}

		List<String> allowedPlayers = Util.getPersistentStringList(left, chestLock);
		if (allowedPlayers != null && !allowedPlayers.contains(e.getPlayer().getUniqueId().toString())) {
			e.getPlayer().sendMessage(Util.fixColor("&cKeine Berechtigung"));
			return; // has password and player has no permission
		}


		e.setCancelled(true);
		var view = new PinView((Player) e.getPlayer(), true, left, right);
		e.getPlayer().openInventory(view.getInventory());

	}

	// TODO: TEST!
	void checkDoubleChest(BlockPlaceEvent e) {
		if(!((((Chest) e.getBlock().getState()).getInventory().getHolder()) instanceof DoubleChest dc)) return;
		
		boolean isLeftNewPlaced = dc.getLeftSide().equals((Chest)e.getBlock().getState());

		List<String> allowedPlayers;
		String currentPin;

		Chest left = (Chest)dc.getLeftSide();
		Chest right = (Chest)dc.getRightSide();

		if(isLeftNewPlaced) {
			allowedPlayers = Util.getPersistentStringList(right, chestLock);
			currentPin = Util.getPersistentString(right, chestPin);
		} else {
			allowedPlayers = Util.getPersistentStringList(left, chestLock);
			currentPin = Util.getPersistentString(left, chestPin);
		}

		if (allowedPlayers == null)
			return; // has no password



		Util.setPersistent(left, chestLock, allowedPlayers);
		Util.setPersistent(left, chestPin, currentPin);
		left.update();
		Util.setPersistent(right, chestLock, allowedPlayers);
		Util.setPersistent(right, chestPin, currentPin);
		right.update();

		if (allowedPlayers.contains(e.getPlayer().getUniqueId().toString()))
			return; // has access

		// Log.debug("created double chest, placed left side" + isLeft);
		e.getPlayer().sendMessage(Util.fixColor("&cDiese Kiste ist gesperrt und du hast keine Berechtigung!"));
		e.getBlock().breakNaturally();
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(e.getBlockPlaced().getType() != Material.CHEST) return;

		// must check next tick if chest merged to double chest
		Util.scheduler.runTask(Main.pluginRef, () -> checkDoubleChest(e));
	}

	@EventHandler
	public void onChestBreak(BlockBreakEvent e) {
		if(!(e.getBlock().getState() instanceof Chest)) return;

		var holder = ((Chest) e.getBlock().getState()).getInventory().getHolder();
		Chest left;

		if (holder instanceof Chest) {
			left = (Chest) holder;
		} else if (holder instanceof DoubleChest) {
			DoubleChest c = (DoubleChest) holder;
			left = (Chest) c.getLeftSide();
		} else {
			return; // ????
		}

		List<String> allowedPlayers = Util.getPersistentStringList(left, chestLock);
		if (allowedPlayers == null)
			return; // has no password

		if (allowedPlayers.contains(e.getPlayer().getUniqueId().toString()))
			return; // has access
		
		e.setCancelled(true);
		e.getPlayer().sendMessage(Util.fixColor("&cDiese Kiste ist gesperrt und du hast keine Berechtigung!"));
	}

	@EventHandler
	public void onChestOpen(InventoryOpenEvent e) {
		if (e.getInventory().getType() != InventoryType.CHEST || e.getInventory().getHolder() instanceof PinView)
			return;

		var holder = e.getInventory().getHolder();
		Chest left;
		Chest right = null;

		if (holder instanceof Chest) {
			left = (Chest) holder;
		} else if (holder instanceof DoubleChest) {
			DoubleChest c = (DoubleChest) holder;
			left = (Chest) c.getLeftSide();
			right = (Chest) c.getRightSide();
		} else {
			return; // ????
		}

		List<String> allowedPlayers = Util.getPersistentStringList(left, chestLock);
		if (allowedPlayers == null)
			return; // has no password

		if (allowedPlayers.contains(e.getPlayer().getUniqueId().toString()))
			return; // has access

		e.setCancelled(true);
		var view = new PinView((Player) e.getPlayer(), false, left, right);
		e.getPlayer().openInventory(view.getInventory());

	}

}
