package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;

import dev.cmplx.servertweaks.Main;

public class CraftingCustomizer {

	public static void init() {
		/* Cobble -> Gravel */
		NamespacedKey cobble_gravel_key = new NamespacedKey(Main.pluginRef, "cobble_gravel");
		SmithingRecipe cobble_gravel = new SmithingTransformRecipe(
				cobble_gravel_key,
				new ItemStack(Material.GRAVEL),
				new RecipeChoice.MaterialChoice(Material.COBBLESTONE),
				new RecipeChoice.MaterialChoice(Material.COBBLESTONE),
				new RecipeChoice.MaterialChoice(Material.COBBLESTONE));

		Bukkit.addRecipe(cobble_gravel);

		/* Gravel -> Sand */
		NamespacedKey gravel_sand_key = new NamespacedKey(Main.pluginRef, "gravel_sand");
		SmithingRecipe gravel_sand = new SmithingTransformRecipe(
				gravel_sand_key,
				new ItemStack(Material.SAND),
				new RecipeChoice.MaterialChoice(Material.GRAVEL),
				new RecipeChoice.MaterialChoice(Material.GRAVEL),
				new RecipeChoice.MaterialChoice(Material.GRAVEL));

		Bukkit.addRecipe(gravel_sand);
	}

}
