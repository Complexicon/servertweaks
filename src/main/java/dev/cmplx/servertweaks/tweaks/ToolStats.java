package dev.cmplx.servertweaks.tweaks;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.i18n;

public class ToolStats implements Listener {
	
	static NamespacedKey statsKey = new NamespacedKey(Main.pluginRef, "toolStat");

	public static void incrementStat(ItemStack curItem, i18n prefix) {
		ItemMeta itemMeta = curItem.getItemMeta();

		Integer stats = Util.getPersistentInt(itemMeta, statsKey);
		if(stats == null) stats = 0;
		stats += 1;
		Util.setPersistent(itemMeta, statsKey, stats);

		var newKilled = prefix.fmt(i18n.param("count", stats.toString()));
		var newLore = Arrays.asList(newKilled);
		
		if(itemMeta.hasLore()) {
			var lore = itemMeta.getLore();
			var unprefixed = ChatColor.stripColor(prefix.fmt(i18n.param("count", "")));
			Optional<String> toReplace = lore.stream().filter(v -> v.contains(unprefixed)).findFirst();
			if(toReplace.isPresent()) {
				newLore = lore;
				newLore.set(lore.indexOf(toReplace.get()), newKilled);
			} else {
				lore.addAll(newLore);
				newLore = lore;
			}
		}

		itemMeta.setLore(newLore);
		curItem.setItemMeta(itemMeta);
	}

	@EventHandler
	void onCraft(CraftItemEvent e) {
		if(e.getWhoClicked() == null) return;
		if(!(e.getWhoClicked() instanceof Player p)) return;

		var curItem = e.getCurrentItem();

		boolean isSword = EnchantmentTarget.WEAPON.includes(curItem);
		boolean isBow = EnchantmentTarget.BOW.includes(curItem);
		boolean isCrossbow = EnchantmentTarget.CROSSBOW.includes(curItem);
		boolean isTrident = EnchantmentTarget.TRIDENT.includes(curItem);
		boolean isTool = EnchantmentTarget.TOOL.includes(curItem);

		if(!(isBow || isCrossbow || isTrident || isSword || isTool)) return;

		var meta = curItem.getItemMeta();
		meta.setLore(Arrays.asList(i18n.TOOLSTATS_CRAFTED_BY.fmt(i18n.param("player", p.getName()))));
		curItem.setItemMeta(meta);
	}

	@EventHandler
	public void onEntityKill(EntityDeathEvent e) {

		if(!(e.getEntity() instanceof LivingEntity)) return;
		if(e.getEntity().getKiller() == null) return;

		ItemStack curItem = e.getEntity().getKiller().getInventory().getItemInMainHand();

		boolean isSword = EnchantmentTarget.WEAPON.includes(curItem);
		boolean isBow = EnchantmentTarget.BOW.includes(curItem);
		boolean isCrossbow = EnchantmentTarget.CROSSBOW.includes(curItem);
		boolean isTrident = EnchantmentTarget.TRIDENT.includes(curItem);

		if(!(isBow || isCrossbow || isTrident || isSword)) return;

		incrementStat(curItem, i18n.TOOLSTATS_KILLS);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {

		ItemStack curItem = e.getPlayer().getInventory().getItemInMainHand();

		if(!EnchantmentTarget.TOOL.includes(curItem)) return;

		incrementStat(curItem, i18n.TOOLSTATS_BROKEN_BLOCKS);

	}

}
