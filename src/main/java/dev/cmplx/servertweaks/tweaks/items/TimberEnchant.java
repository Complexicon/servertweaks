package dev.cmplx.servertweaks.tweaks.items;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;

import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;

public class TimberEnchant implements Listener {

	public static final NamespacedKey timberEntchant = new NamespacedKey(Main.pluginRef, "timber");
	public static final ItemStack timberBook = new ItemStackBuilder(Material.ENCHANTED_BOOK)
			.setLore("&6Timber Verzauberung")
			.setPersistent(timberEntchant, true)
			.build();

	static {
		DebugItemsCommand.DebugItems.add(timberBook);
	}

	@EventHandler
	public void onAnvilCraft(PrepareAnvilEvent e) {
		var anvil = e.getInventory();
		var first = anvil.getItem(0);
		var second = anvil.getItem(1);

		if (first == null || second == null)
			return;

		if (!first.getType().toString().endsWith("_AXE"))
			return;

		if (!Util.getPersistentBool(second.getItemMeta(), timberEntchant))
			return;

		Util.applyCustomBook(e, timberEntchant, "&6Timber Verzauberung");
	}

	// rewritten as "flood fill" algo
	private List<Block> findConnectedBlocks(Block start, Material searchFor, int limit) {
		List<Block> found = new ArrayList<>();
		Queue<Block> queue = new ArrayDeque<>();
		Set<Block> visited = new HashSet<>();

		queue.add(start);
		visited.add(start);

		while (!queue.isEmpty() && found.size() < limit) {
			Block current = queue.poll();
			found.add(current);

			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (x == 0 && y == 0 && z == 0)
							continue; // skip self

						Block neighbor = current.getWorld().getBlockAt(
								current.getX() + x,
								current.getY() + y,
								current.getZ() + z);

						if (searchFor != neighbor.getType())
							continue;
						if (!visited.add(neighbor))
							continue;
						queue.add(neighbor);
					}
				}
			}
		}

		return found;
	}

	public class TimberBlockBreakEvent extends BlockBreakEvent {
		public TimberBlockBreakEvent(Block block, Player player) {
			super(block, player);
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent e) {
		if (e instanceof TimberBlockBreakEvent) {
			return;
		}

		ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
		if (tool == null)
			return;
		if (!tool.hasItemMeta())
			return;
		if (!(e.getBlock().getType().toString().endsWith("_LOG")
				&& Util.getPersistentBool(tool.getItemMeta(), TimberEnchant.timberEntchant)))
			return;

		Random r = new Random();

		var woodType = e.getBlock().getType();

		Bukkit.getScheduler().runTaskAsynchronously(Main.pluginRef, () -> {
			Queue<Block> blocks = new LinkedList<>(findConnectedBlocks(e.getBlock(), woodType, (tool.getType().getMaxDurability() - ((Damageable) tool.getItemMeta()).getDamage()) * 3));
			blocks.poll();

			new BukkitRunnable() {

				Damageable axe = (Damageable) tool.getItemMeta();
				int unbreakingLevel = axe.getEnchantLevel(Enchantment.UNBREAKING);

				@Override
				public void run() {
					int broken = 0;
					while (!blocks.isEmpty() && broken < 4) {
						axe = (Damageable) tool.getItemMeta();
						if (axe.hasDamage() && axe.getDamage() == tool.getType().getMaxDurability()) {
							cancel();
							return;
						}

						var b = blocks.poll();

						b.breakNaturally(tool);
						Bukkit.getPluginManager().callEvent(new TimberBlockBreakEvent(b, e.getPlayer()));
						axe = (Damageable) tool.getItemMeta();
						broken++;

						int rng = r.nextInt(100) + 1;
						switch (unbreakingLevel) {
							case 1:
								if (rng > 50)
									continue;
								break;
							case 2:
								if (rng > 33)
									continue;
								break;
							case 3:
								if (rng > 25)
									continue;
								break;
						}

						axe.setDamage(axe.getDamage() + 1);
						tool.setItemMeta(axe);
					}

					if (blocks.isEmpty())
						cancel();
				}

			}.runTaskTimer(Main.pluginRef, 0, 1);
		});
	}

}
