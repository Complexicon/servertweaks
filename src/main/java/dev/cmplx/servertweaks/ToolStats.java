package dev.cmplx.servertweaks;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ToolStats implements Listener {
	
	static NamespacedKey statsKey = new NamespacedKey(Main.pluginRef, "toolStat");

	public static void updateStats(ItemStack curItem, String prefix, int toAdd) {
		ItemMeta itemMeta = curItem.getItemMeta();

		Integer stats = Util.getPersistentInt(itemMeta, statsKey);
		if(stats == null) stats = 0;
		stats += toAdd;
		Util.setPersistent(itemMeta, statsKey, stats);

		var newKilled = Util.fixColor(prefix + stats);
		var newLore = Arrays.asList(newKilled);
		
		if(itemMeta.hasLore()) {
			var lore = itemMeta.getLore();
			var unprefixed = ChatColor.stripColor(Util.fixColor(prefix));
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
	public void onEntityKill(EntityDeathEvent e) {

		if(!(e.getEntity() instanceof LivingEntity)) return;
		if(e.getEntity().getKiller() == null) return;

		ItemStack curItem = e.getEntity().getKiller().getInventory().getItemInMainHand();

		boolean isSword = EnchantmentTarget.WEAPON.includes(curItem);
		boolean isBow = EnchantmentTarget.BOW.includes(curItem);
		boolean isCrossbow = EnchantmentTarget.CROSSBOW.includes(curItem);
		boolean isTrident = EnchantmentTarget.TRIDENT.includes(curItem);

		if(!(isBow || isCrossbow || isTrident || isSword)) return;

		updateStats(curItem, "&7Entities Killed: &a", 1);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {

		ItemStack curItem = e.getPlayer().getInventory().getItemInMainHand();

		if(!EnchantmentTarget.TOOL.includes(curItem)) return;

		updateStats(curItem, "&7Broken Blocks: &a", 1);

	}

}
