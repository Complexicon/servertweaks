package dev.cmplx.servertweaks.tweaks;

import java.util.Arrays;

import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

public class HopperFilter implements Listener {

    boolean filterMatch(String filterString, String itemName) {

        String[] filter = filterString.split(",");

        return Arrays.asList(filter).stream().anyMatch((filter_i) -> {
            if (filter_i.endsWith("*")) {
                return itemName.startsWith(filter_i.substring(0, filter_i.length() - 1));
            } else if (filter_i.startsWith("*")) {
                return itemName.endsWith(filter_i.substring(1));
            } else {
                return filter_i.equalsIgnoreCase(itemName);
            }
        });

    }

    @EventHandler
    void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {

        if (event.getDestination().getType().equals(InventoryType.HOPPER)
                && event.getDestination().getHolder() instanceof Container) {

            String customName = ((Container) event.getDestination().getHolder()).getCustomName();

            if (customName != null) {

                String itemName = event.getItem().getType().getKey().getKey();
                if (!filterMatch(customName, itemName)) {
                    event.setCancelled(true);
                }
            }

        }
    }

    @EventHandler
    void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
        if (event.getInventory().getHolder() instanceof Container) {

            String customName = ((Container) event.getInventory().getHolder()).getCustomName();

            if (customName != null) {
                String itemName = event.getItem().getItemStack().getType().getKey().getKey();
                if (!filterMatch(customName, itemName)) {
                    event.setCancelled(true);
                }

            }
        }
    }
}