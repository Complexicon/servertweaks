package dev.cmplx.servertweaks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryGUI implements InventoryHolder {

	static class InventoryEventHandler implements Listener {
		@EventHandler
		private void onInvInteract(InventoryClickEvent e) {
			if (!(e.getInventory().getHolder() instanceof InventoryGUI g))
				return;
			if (g.hasPager() && g.handlePager(e))
				return;
			if (g.handlers.containsKey(e.getCurrentItem()))
				g.handlers.get(e.getCurrentItem()).accept(e);
		}
	}

	Map<ItemStack, Consumer<InventoryClickEvent>> handlers;
	Map<Integer, ItemStack> allItems;
	Inventory inv;
	int page = 1;
	String label;

	private int maxPage() {
		if (allItems.size() == 0)
			return 1;

		return (int) Math.ceil((double) Collections.max(allItems.keySet()) / 45);
	}

	private boolean handlePager(InventoryClickEvent e) {
		if (e.getSlot() < 45)
			return false;

		if (e.getCurrentItem().equals(Util.symbols.get("ARROW_LEFT")) && page != 1) {
			page--;
			assemblePage();
		} else if (e.getCurrentItem().equals(Util.symbols.get("ARROW_RIGHT")) && page < maxPage()) {
			page++;
			assemblePage();
		}

		e.setCancelled(true);
		return true;
	}

	final ItemStack AIR = new ItemStack(Material.AIR);

	protected void assemblePage() {
		int from = 45 * (page - 1);
		int to = 45 * page;

		var itemsOnPage = allItems.entrySet().stream().filter(v -> v.getKey() >= from && v.getKey() < to).toList();

		int toClear = hasPager() ? 45 : inv.getSize();

		for (int i = 0; i < toClear; i++) {
			inv.setItem(i, AIR);
		}

		for (var entry : itemsOnPage) {
			inv.setItem(entry.getKey() - from, entry.getValue());
		}

		if (!hasPager()) return;

		inv.setItem(45, Util.numbers[page / 10]);
		inv.setItem(46, Util.numbers[page % 10]);
	}

	private int lastUsedSlot() {
		return Collections.max(allItems.keySet());
	}

	private boolean hasPager() {
		return lastUsedSlot() >= 45;
	}

	public InventoryGUI(String guiLabel) {
		handlers = new HashMap<>();
		allItems = new HashMap<>();
		label = guiLabel;
	}

	public static void init() {
		Bukkit.getPluginManager().registerEvents(new InventoryEventHandler(), Main.pluginRef);
	}

	protected void addItem(ItemStack item, int slot, Consumer<InventoryClickEvent> callback) throws Exception {
		handlers.put(item, callback);
		allItems.put(slot, item);
	}

	protected void addItem(ItemStack item, int page, int slot, Consumer<InventoryClickEvent> callback)
			throws Exception {
		addItem(item, (page * 45) + slot, callback);
	}

	private int firstFree() {
		if (allItems.size() == 0)
			return 0;

		return IntStream.range(0, Collections.max(allItems.keySet()) + 2)
				.filter(v -> !allItems.containsKey(v))
				.findFirst().orElseGet(() -> -1);
	}

	protected void addItem(ItemStack item, Consumer<InventoryClickEvent> callback) throws Exception {
		addItem(item, firstFree(), callback);
	}

	@Override
	public Inventory getInventory() {

		if (hasPager()) {
			inv = Bukkit.createInventory(this, 54, label);
			inv.setItem(47, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
			inv.setItem(48, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
			inv.setItem(49, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
			inv.setItem(50, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
			inv.setItem(51, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
			inv.setItem(52, Util.symbols.get("ARROW_LEFT"));
			inv.setItem(53, Util.symbols.get("ARROW_RIGHT"));
			assemblePage();
		} else {
			inv = Bukkit.createInventory(this, 9 * (int) Math.ceil((double) lastUsedSlot() / 9), label);
			assemblePage();
		}


		return inv;
	}

}
