package dev.cmplx.servertweaks.tweaks.entities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.MerchantRecipe;

import dev.cmplx.servertweaks.Config;
import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.tweaks.items.TimberEnchant;

public class WanderingTraderModifier implements Listener {

	void addWanderingTraderRecipes(List<MerchantRecipe> recipes) {
		if (Config.timberMod) {
			var timber = new MerchantRecipe(TimberEnchant.timberBook, 9999);
			timber.addIngredient(new ItemStackBuilder(Material.EMERALD).setAmount(64).build());
			recipes.add(timber);
		}
	}

	@EventHandler
	void onWanderingTraderSpawn(CreatureSpawnEvent e) {
		if (!(e.getEntity() instanceof WanderingTrader wt)) return;
		var recipes = new ArrayList<>(wt.getRecipes()); // create mutable list from immutable
		addWanderingTraderRecipes(recipes);
		wt.setRecipes(recipes);
	}

}
