package dev.cmplx.servertweaks;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.inventory.ItemStack;

public class LoreUtil {

	public static void addEntry(String text, ItemStack to) {
		var meta = to.getItemMeta();

		if (!meta.hasLore()) {
			meta.setLore(List.of(text));
		} else {
			var currentLore = meta.getLore();
			currentLore.add(text);
			meta.setLore(currentLore);
		}

		to.setItemMeta(meta);
	}

	public static void updateEntry(Predicate<? super String> matcher, String newText, ItemStack of) {
		var meta = of.getItemMeta();

		if (!meta.hasLore()) {
			addEntry(newText, of);
			return;
		}

		var currentLore = meta.getLore();

		var match = currentLore.stream().filter(matcher).findFirst();
		if (!match.isPresent()) {
			addEntry(newText, of);
			return;
		}

		currentLore.set(currentLore.indexOf(match.get()), newText);
		meta.setLore(currentLore);
		of.setItemMeta(meta);
	}

	public static void removeEntry(String text, ItemStack from) {
		removeEntry(s -> s.equals(text), from);
	}

	public static void removeEntryWithPrefix(String text, ItemStack from) {
		removeEntry(s -> s.startsWith(text), from);
	}

	
	public static void removeEntryContaining(String text, ItemStack from) {
		removeEntry(s -> s.contains(text), from);
	}

	public static void removeEntry(Predicate<? super String> matcher, ItemStack from) {
		var meta = from.getItemMeta();

		if (!meta.hasLore()) return;

		var currentLore = meta.getLore();

		var match = currentLore.stream().filter(matcher).findFirst();
		if (!match.isPresent()) return;

		currentLore.remove(currentLore.indexOf(match.get()));
		meta.setLore(currentLore);
		from.setItemMeta(meta);
	}

}
