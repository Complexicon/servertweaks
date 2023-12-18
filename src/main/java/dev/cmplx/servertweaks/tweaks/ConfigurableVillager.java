package dev.cmplx.servertweaks.tweaks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import dev.cmplx.servertweaks.Log;

public class ConfigurableVillager implements Listener {

	class VillagerConfig implements InventoryHolder {

		Inventory config;
		Merchant toCofigure;

		Map<ItemStack, Runnable> handlers = new HashMap<>();

		public VillagerConfig(Villager v) {
			toCofigure = v;
			config = Bukkit.createInventory(this, 9 * 6, "Villager Konfigurieren");

			var glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

			handlers.put(glass, () -> test());

			var rec = new MerchantRecipe(new ItemStack(Material.DIAMOND), 9999);
			rec.addIngredient(new ItemStack(Material.DIAMOND));
			toCofigure.setRecipes(List.of(rec));
			v.setAI(false);
			v.setProfession(Profession.CARTOGRAPHER);
			v.setRemoveWhenFarAway(false);
			v.setCustomName("Herbert");
			v.setInvulnerable(true);

			// for(int i = 0; i < config.getSize(); i++) {
			// 	config.setItem(i, glass);
			// }

		}

		@Override
		public Inventory getInventory() {
			return config;
		}

		void test() {
			Log.debug("Debug.");
		}

		public void handleClick(ItemStack currentItem) {
			var runnable = handlers.get(currentItem);
			if(runnable != null) runnable.run();
		}

		public boolean moveItem(ItemStack s, int slot) {
			return false;
		} 

	}

	@EventHandler
	public void onInventoryMove(InventoryClickEvent e) {
		if(!(e.getInventory().getHolder() instanceof VillagerConfig c)) return;
		e.setCancelled(true);
		if(e.getAction() != InventoryAction.HOTBAR_SWAP) return;

	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(!(e.getInventory().getHolder() instanceof VillagerConfig c)) return;

		if (e.getClick() != ClickType.LEFT)
			return;

		if (e.getCurrentItem() == null)
			return;

		e.setCancelled(true);
		c.handleClick(e.getCurrentItem());

	}

	@EventHandler
	public void onVillagerInteract(PlayerInteractEntityEvent e) {
		if(!e.getPlayer().isSneaking()) return;
		if(e.getPlayer().getGameMode() != GameMode.CREATIVE) return;
		if(!e.getPlayer().isOp()) return;
		if(e.getPlayer().getInventory().getItem(e.getHand()).getType() != Material.DIAMOND) return;
		if(!(e.getRightClicked() instanceof Villager)) return;

		Log.debug("right click villager with diamond");
		e.setCancelled(true);
		e.getPlayer().openInventory(new VillagerConfig((Villager)e.getRightClicked()).getInventory());
	}

}
