package dev.cmplx.servertweaks.tweaks.entities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent.ChangeReason;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import dev.cmplx.servertweaks.Main;

public class LibrarianHelper implements Listener {

	@EventHandler
	void onVillagerJobChange(VillagerCareerChangeEvent e) {
		if (e.getReason() != ChangeReason.EMPLOYED) return;
		if (e.getProfession() != Profession.LIBRARIAN) return;

		var nearbyPlayers = e.getEntity().getNearbyEntities(4, 2, 4).stream().filter(entity -> entity instanceof Player).toArray(Player[]::new);
		if (nearbyPlayers.length == 0) return;

		Bukkit.getScheduler().runTask(Main.pluginRef, () -> {
			var bookTrades = e.getEntity()
				.getRecipes()
				.stream()
				.filter(trade -> trade.getResult().getType().equals(Material.ENCHANTED_BOOK))
				.map(trade -> ((EnchantmentStorageMeta)trade.getResult().getItemMeta()).getStoredEnchants().entrySet().stream().findFirst().get())
				.map(enchantment -> formatEnchantment(enchantment.getKey(), enchantment.getValue()))
				.toList();
			
			var displayText = String.join(", ", bookTrades);

			if (bookTrades.size() == 0) {
				displayText = "No Enchantment Books";
			}

			for (var player : nearbyPlayers) {
				player.sendTitle("", displayText, 0, 50, 0);
			}
		});
	}

	// ChatGPT Trash (i was too lazy)

	private static String formatEnchantment(Enchantment enchantment, int level) {

			// Handle special case where level should be omitted (e.g., Mending)
        if (level == 1 && enchantment.getMaxLevel() == 1) {
            return formatEnchantmentName(enchantment);
        }

        // Otherwise, format with the Roman numeral level
        return formatEnchantmentName(enchantment) + " " + toRoman(level);
    }

    private static String formatEnchantmentName(Enchantment enchantment) {
        String name = enchantment.getKey().getKey();
        name = name.replace("_", " ").toLowerCase();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    // Convert integer to Roman numeral
    private static String toRoman(int number) {
        if (number == 1) return "I";
        if (number == 2) return "II";
        if (number == 3) return "III";
        if (number == 4) return "IV";
        if (number == 5) return "V";
        if (number == 6) return "VI";
        if (number == 7) return "VII";
        if (number == 8) return "VIII";
        if (number == 9) return "IX";
        if (number == 10) return "X";
        return Integer.toString(number); // If a higher level is needed
    }

}
