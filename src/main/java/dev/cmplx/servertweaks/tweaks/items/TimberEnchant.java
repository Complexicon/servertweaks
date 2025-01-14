package dev.cmplx.servertweaks.tweaks.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.tweaks.ToolStats;

public class TimberEnchant implements Listener {

	public static final NamespacedKey timberEntchant = new NamespacedKey(Main.pluginRef, "timber");
	public static final ItemStack timberBook = 
		new ItemStackBuilder(Material.ENCHANTED_BOOK)
		.setLore("&6Timber Verzauberung")
		.setPersistent(timberEntchant, true)
		.build();

	static { DebugItemsCommand.DebugItems.add(timberBook); }

	@EventHandler
	public void onAnvilCraft(PrepareAnvilEvent e) {
		var anvil = e.getInventory();
		var first = anvil.getItem(0);
		var second = anvil.getItem(1);

		if (first == null || second == null)
			return;

		if(!first.getType().toString().endsWith("_AXE"))
			return;

		if(!Util.getPersistentBool(second.getItemMeta(), timberEntchant))
			return;

		if (e.getResult() == null) {
			var axe = first.clone();
			var meta = axe.getItemMeta();
	
			var lore = Arrays.asList(Util.fixColor("&6Timber Verzauberung"));
	
			if(meta.hasLore()) {
				lore = meta.getLore();
				lore.add(0, Util.fixColor("&6Timber Verzauberung"));
			}
	
			meta.setLore(lore);
			
			Util.setPersistent(meta, timberEntchant, true);
	
			axe.setItemMeta(meta);
	
			e.getView().setRepairCost(0);
	
			e.setResult(axe);
		} else { // fix crafting together 2 axes with timber "enchant"

			var resultAxe = e.getResult();

			var meta = resultAxe.getItemMeta();

			var lore = Arrays.asList(Util.fixColor("&6Timber Verzauberung"));
	
			if(meta.hasLore()) {
				lore = meta.getLore();
				lore.add(0, Util.fixColor("&6Timber Verzauberung"));
			}

			meta.setLore(lore);
			Util.setPersistent(meta, timberEntchant, true);

			resultAxe.setItemMeta(meta);

		}


	}

	/// ----- TIMBER LOGIC -----

	
	// private Material[] leafMaterials = {
	// 	Material.OAK_LEAVES,
	// 	Material.BIRCH_LEAVES,
	// 	Material.ACACIA_LEAVES,
	// 	Material.JUNGLE_LEAVES,
	// 	Material.SPRUCE_LEAVES,
	// 	Material.DARK_OAK_LEAVES,
	// };

	private List<Block> recursiveBlockLocator(Block l, List<Block> alreadyFound, int limit) {
		return recursiveBlockLocator(l, new Material[]{ l.getType() }, alreadyFound, limit);
	}

	private List<Block> recursiveBlockLocator(Block l, Material[] acceptable, List<Block> alreadyFound, int limit) {

		List<Block> newFound = new ArrayList<>();

		for(int x = -1; x < 2; x++) {
			for(int y = -1; y < 2; y++) {
				for(int z = -1; z < 2; z++) {
					if(alreadyFound.size() > limit - 1) break;
					
					Block checkMe =  l.getWorld().getBlockAt(l.getLocation().add(x, y, z));


					if(!Arrays.asList(acceptable).contains(checkMe.getType())) continue;
					if(alreadyFound.contains(checkMe)) continue;

					newFound.add(checkMe);
				}
			}
		}

		alreadyFound.addAll(newFound);

		for(int i = 0; i < newFound.size(); i++) {
			newFound.addAll(recursiveBlockLocator(newFound.get(i), alreadyFound, limit));
		}

		return newFound;
	}

	// TODO: Async
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent e) {
		ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
		if(tool == null) return;
		if(!tool.hasItemMeta()) return;
		if(e.getBlock().getType().toString().endsWith("_LOG") && Util.getPersistentBool(tool.getItemMeta(), TimberEnchant.timberEntchant)) {

			Damageable axe = (Damageable) tool.getItemMeta();
			int unbreakingLevel = axe.getEnchantLevel(Enchantment.UNBREAKING);

			List<Block> blocks = recursiveBlockLocator(e.getBlock(), new ArrayList<>(), 5000);

			Random r = new Random();

			// for(Block b : blocks) {
			// 	List<Block> leaves = recursiveBlockLocator(b, leafMaterials, new ArrayList<>(), 50);
			// 	if(leaves.size() > 0) {
			// 		for(Block leaf : leaves) {
			// 			leaf.breakNaturally();
			// 		}
			// 		break;
			// 	}
			// }

			int broken = 0;

			for(Block b : blocks) {
				if(axe.hasDamage() && axe.getDamage() == tool.getType().getMaxDurability()) break;

				//x.breakNaturally();
				b.breakNaturally(tool);
				broken++;

				int rng = r.nextInt(100) + 1;
				switch(unbreakingLevel) {
					case 1:
						if(rng > 50) continue;
						break;
					case 2:
						if(rng > 33) continue;
						break;
					case 3:
						if(rng > 25) continue;
						break;
				}

				axe.setDamage(axe.getDamage() + 1);
				tool.setItemMeta(axe);
			}

			ToolStats.updateStats(tool, "&7Broken Blocks: &a", broken);
		}
	}

}
