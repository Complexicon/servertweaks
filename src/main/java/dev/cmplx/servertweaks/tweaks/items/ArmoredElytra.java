package dev.cmplx.servertweaks.tweaks.items;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import dev.cmplx.servertweaks.Main;

public class ArmoredElytra implements Listener {

	static final NamespacedKey armoredElytraKey = new NamespacedKey(Main.pluginRef, "armored_elytra");
	
	public ArmoredElytra() {


		/* ARMORED ELYTRA */
		SmithingRecipe armoredElytra = new SmithingTransformRecipe(
				armoredElytraKey,
				new ItemStack(Material.ELYTRA),
				new RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
				new RecipeChoice.MaterialChoice(Material.ELYTRA),
				new RecipeChoice.MaterialChoice((Material.NETHERITE_INGOT)));

		Bukkit.addRecipe(armoredElytra);

	}

	@EventHandler
	void onItemBurn(EntityDamageEvent e) {
		if(e.getCause() == DamageCause.LAVA  || e.getCause() == DamageCause.FIRE) {
			if(e.getEntityType() == EntityType.ITEM) {
				Item dropped = (Item) e.getEntity();
				ItemMeta droppedMeta = dropped.getItemStack().getItemMeta();
				if(droppedMeta.hasLore() && droppedMeta.getLore().get(0).contains("Netherite Reinforced")) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	void onItemBurn(EntityCombustEvent e) {
		if(e.getEntityType() == EntityType.ITEM) {
			Item dropped = (Item) e.getEntity();
			ItemMeta droppedMeta = dropped.getItemStack().getItemMeta();
			if(droppedMeta.hasLore() && droppedMeta.getLore().get(0).contains("Netherite Reinforced")) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	void onSmith(PrepareSmithingEvent e) {

		if (e.getInventory().getRecipe() == null) return;
		if (e.getResult() == null) return;

		SmithingRecipe r = (SmithingRecipe) e.getInventory().getRecipe();

		if(r.getKey().getKey() == "armored_elytra") {

			ItemMeta elytra = e.getResult().getItemMeta();
			if(elytra.hasAttributeModifiers()) return;

			AttributeModifier moreArmor = new AttributeModifier(armoredElytraKey, 7.0d, Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
			elytra.addAttributeModifier(Attribute.ARMOR, moreArmor);
			elytra.setLore(List.of("§5Netherite Reinforced"));
			e.getResult().setItemMeta(elytra);

		}

	}

}
