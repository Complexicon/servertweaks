package dev.cmplx.servertweaks.tweaks.items;

import java.util.Arrays;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.LoreUtil;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.i18n;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class RefillableRocket implements Listener {

	public static NamespacedKey refillable_rocket = new NamespacedKey(Main.pluginRef, "refillable_rocket");
	static NamespacedKey refillable_rocket_charges = new NamespacedKey(Main.pluginRef, "refillable_rocket_charges");

	public static ItemStack refillableRocket = new ItemStackBuilder(Material.FIREWORK_ROCKET)
			.setName(i18n.ITEM_REFILLABLE_ROCKET.fmt()).setPersistent(refillable_rocket, true).build();
	static {
		var m = refillableRocket.getItemMeta();
		m.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		m.setMaxStackSize(1);
		m.setLore(Arrays.asList(i18n.ROCKET_CHARGES_REMAINING.fmt(i18n.param("charges", "0"))));
		Util.setPersistent(m, refillable_rocket_charges, 0);
		refillableRocket.setItemMeta(m);

		DebugItemsCommand.DebugItems.add(refillableRocket);
	}

	int getCharge(ItemStack s) {
		return Util.getPersistentInt(s.getItemMeta(), refillable_rocket_charges);
	}

	void setCharge(Player p, ItemStack s, int charge) {
		
		var m = s.getItemMeta();
		Util.setPersistent(m, refillable_rocket_charges, charge);
		s.setItemMeta(m);

		var newChargesLore = i18n.ROCKET_CHARGES_REMAINING.fmt(i18n.param("charges", charge));

		LoreUtil.updateEntry(v -> v.contains(i18n.ROCKET_CHARGES_REMAINING.fmt(i18n.param("charges", ""))), newChargesLore, s);
		
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(newChargesLore));
	}

	@EventHandler
	void onRocketUse(PlayerInteractEvent e) {
		if (e.getItem() == null)
			return;
		if (e.getItem().getType() != Material.FIREWORK_ROCKET)
			return;
		Util.setMetadata(e.getPlayer(), "last_interacted_rocket", e.getItem());
	}

	@EventHandler
	void onRefillRocket(PlayerInteractEvent e) {
		if (e.getItem() == null)
			return;
		if (e.getItem().getType() != Material.FIREWORK_ROCKET)
			return;

		if (!Util.getPersistentBool(e.getItem().getItemMeta(), refillable_rocket))
			return;

		if (e.getHand() != EquipmentSlot.HAND)
			return;

		Player p = e.getPlayer();
		var offhand = p.getInventory().getItemInOffHand();

		if (offhand == null)
			return;

		if (offhand.getType() != Material.GUNPOWDER)
			return;

		if (!p.isSneaking())
			return;

		var chargeAmount = offhand.getAmount();

		setCharge(p, e.getItem(), getCharge(e.getItem()) + chargeAmount);

		p.playSound(p.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1, 1);

		offhand.setAmount(0);
	}

	@EventHandler
	public void onFireworkSpawn(ProjectileLaunchEvent e) {
		if (e.getEntity() instanceof Firework firework && firework.getShooter() instanceof Player p) {
			var rocketItem = Util.getMetadata(p, "last_interacted_rocket", ItemStack.class);

			if (!Util.getPersistentBool(rocketItem.getItemMeta(), refillable_rocket))
				return;

			if (!p.isGliding()) {
				e.setCancelled(true);
				return;
			}
			
			var remaining = getCharge(rocketItem);
			
			if (remaining <= 0) {
				e.setCancelled(true);
				return;
			}
			
			if (p.getGameMode() != GameMode.CREATIVE) {
				rocketItem.setAmount(2);
			}

			setCharge(p, rocketItem, remaining - 1);

			p.setCooldown(refillableRocket, 20);

		}
	}

	@EventHandler
	void onAnvilCraft(PrepareAnvilEvent e) {
		var anvil = e.getInventory();
		var first = anvil.getItem(0);
		var second = anvil.getItem(1);

		if (first == null || second == null)
			return;

		if (first.getType() != Material.FIREWORK_ROCKET)
			return;

		if (second.getType() != Material.ENCHANTED_BOOK)
			return;

		if (!(second.getItemMeta() instanceof EnchantmentStorageMeta m))
			return;

		if (!m.getStoredEnchants().keySet().contains(Enchantment.INFINITY))
			return;

		e.setResult(refillableRocket.clone());
	}

}
