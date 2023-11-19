package dev.cmplx.servertweaks;

import java.util.Arrays;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ToolStats implements Listener {
	
	NamespacedKey statsKey = new NamespacedKey(Main.pluginRef, "toolStat");

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {

		ItemStack curItem = e.getPlayer().getInventory().getItemInMainHand();

		if(!EnchantmentTarget.TOOL.includes(curItem)) return;

		ItemMeta itemMeta = curItem.getItemMeta();
		PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

		if(!dataContainer.has(statsKey, PersistentDataType.INTEGER)) {
			dataContainer.set(statsKey, PersistentDataType.INTEGER, 0);
		}

		int blocksBroken = dataContainer.get(statsKey, PersistentDataType.INTEGER);
		dataContainer.set(statsKey, PersistentDataType.INTEGER, ++blocksBroken);

		itemMeta.setLore(Arrays.asList("Broken Blocks: " + blocksBroken));

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

		ItemMeta itemMeta = curItem.getItemMeta();
		PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

		if(!dataContainer.has(statsKey, PersistentDataType.INTEGER)) {
			dataContainer.set(statsKey, PersistentDataType.INTEGER, 0);
		}

		int entitiesKilled = dataContainer.get(statsKey, PersistentDataType.INTEGER);
		dataContainer.set(statsKey, PersistentDataType.INTEGER, ++entitiesKilled);

		itemMeta.setLore(Arrays.asList("Entities Killed: " + entitiesKilled));

		curItem.setItemMeta(itemMeta);
	}

}
