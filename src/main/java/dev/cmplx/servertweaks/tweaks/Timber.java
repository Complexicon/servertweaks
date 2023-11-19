package dev.cmplx.servertweaks.tweaks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class Timber implements Listener {

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

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent e) {
		ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
		// 
		if(e.getPlayer().isSneaking() && e.getBlock().getType().toString().endsWith("_LOG") && e.getBlock().isPreferredTool(tool)) {

			Damageable axe = (Damageable) tool.getItemMeta();
			int unbreakingLevel = axe.getEnchantLevel(Enchantment.DURABILITY);

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

			for(Block b : blocks) {
				if(axe.hasDamage() && axe.getDamage() == tool.getType().getMaxDurability()) break;

				//x.breakNaturally();
				b.breakNaturally(tool);

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
		}
	}

}