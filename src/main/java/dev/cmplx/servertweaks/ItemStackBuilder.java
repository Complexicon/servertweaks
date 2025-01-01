package dev.cmplx.servertweaks;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackBuilder {

	private final ItemStack stack;
	private final ItemMeta meta;

	public ItemStackBuilder(Material m) {
		this.stack = new ItemStack(m);
		this.meta = this.stack.getItemMeta();
	}

	public ItemStack build() {
		this.stack.setItemMeta(meta);
		return this.stack;
	}

	public ItemStackBuilder setAmount(int amount) {
		this.stack.setAmount(amount);
		return this;
	}

	public ItemStackBuilder setName(String name) {
		this.meta.setDisplayName(Util.fixColor(name));
		return this;
	}

	public ItemStackBuilder setLore(String... lore) {
		this.meta.setLore(Arrays.asList(lore).stream().map(line -> Util.fixColor(line)).toList());
		return this;
	}

	public ItemStackBuilder setPersistent(NamespacedKey key, String value) {
		Util.setPersistent(meta, key, value);
		return this;
	}

	public ItemStackBuilder setPersistent(NamespacedKey key, List<String> value) {
		Util.setPersistent(meta, key, value);
		return this;
	}

	public ItemStackBuilder setPersistent(NamespacedKey key, int value) {
		Util.setPersistent(meta, key, value);
		return this;
	}

	public ItemStackBuilder setPersistent(NamespacedKey key, boolean value) {
		Util.setPersistent(meta, key, value);
		return this;
	}

	public ItemStackBuilder setPersistent(NamespacedKey key, Serializable value) {
		try {
			Util.setPersistentSerialized(meta, key, value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

}
