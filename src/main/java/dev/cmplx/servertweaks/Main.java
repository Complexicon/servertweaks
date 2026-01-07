package dev.cmplx.servertweaks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;

import dev.cmplx.servertweaks.tweaks.AnvilRename;
import dev.cmplx.servertweaks.tweaks.CauldronConcrete;
import dev.cmplx.servertweaks.tweaks.CraftingCustomizer;
import dev.cmplx.servertweaks.tweaks.CropTrample;
import dev.cmplx.servertweaks.tweaks.DeathCoords;
import dev.cmplx.servertweaks.tweaks.DiscordIntegration;
import dev.cmplx.servertweaks.tweaks.FastLeafDecay;
import dev.cmplx.servertweaks.tweaks.LeashableVillagers;
import dev.cmplx.servertweaks.tweaks.LootGenerateHook;
import dev.cmplx.servertweaks.tweaks.MultiplayerSleep;
import dev.cmplx.servertweaks.tweaks.PerPlayerLoot;
import dev.cmplx.servertweaks.tweaks.QuickOpen;
import dev.cmplx.servertweaks.tweaks.RightClickHarvest;
import dev.cmplx.servertweaks.tweaks.ToolStats;
import dev.cmplx.servertweaks.tweaks.UnlockAll;
import dev.cmplx.servertweaks.tweaks.blocks.DualDoor;
import dev.cmplx.servertweaks.tweaks.blocks.Elevator;
import dev.cmplx.servertweaks.tweaks.blocks.HopperFilter;
import dev.cmplx.servertweaks.tweaks.blocks.Loadstone;
import dev.cmplx.servertweaks.tweaks.blocks.LockableChest;
import dev.cmplx.servertweaks.tweaks.blocks.TeleportAnchor;
import dev.cmplx.servertweaks.tweaks.entities.ConfigurableVillager;
import dev.cmplx.servertweaks.tweaks.entities.DoubleShulker;
import dev.cmplx.servertweaks.tweaks.entities.MobGriefing;
import dev.cmplx.servertweaks.tweaks.entities.SneakyMobs;
import dev.cmplx.servertweaks.tweaks.entities.WanderingTraderModifier;
import dev.cmplx.servertweaks.tweaks.items.ArmoredElytra;
import dev.cmplx.servertweaks.tweaks.items.DebugStick;
import dev.cmplx.servertweaks.tweaks.items.GPSCompass;
import dev.cmplx.servertweaks.tweaks.items.RefillableRocket;
import dev.cmplx.servertweaks.tweaks.items.SoulboundEnchant;
import dev.cmplx.servertweaks.tweaks.items.TimberEnchant;

public class Main extends JavaPlugin {

	public static JavaPlugin pluginRef;
	
	void registerWhen(boolean when, Class<? extends Listener> listener) {
		try {
			if(when) {
				Log.info("Enabling Tweak: " + listener.getSimpleName());
				Bukkit.getPluginManager().registerEvents(listener.getDeclaredConstructor().newInstance(), this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		pluginRef = this;

		Util.init();
		Util.setupConfig();
		Cron.init();

		PlaytimeTracker.init();
		CraftingCustomizer.init();
		InventoryGUI.init();

		Util.getObjectiveSafe("deathCounter", "Tode", Criteria.DEATH_COUNT, DisplaySlot.PLAYER_LIST);

		CommandManager.init();

		// these are necessary
		registerWhen(true, LootGenerateHook.class);
		registerWhen(true, MOTDHelper.class);
		registerWhen(true, PlaytimeTracker.class);
		registerWhen(true, ChatEvents.class);
		registerWhen(true, TabListHelper.class);

		// DEV
		registerWhen(false, ConfigurableVillager.class);
		registerWhen(true, 	DebugStick.class);

		// optional events
		registerWhen(true, WanderingTraderModifier.class); // handled internally
		registerWhen(true,				QuickOpen.class); // handled internally
		registerWhen(true,				MobGriefing.class); // handled internally
		registerWhen(true, 			DualDoor.class);
		registerWhen(true, 			ToolStats.class);
		registerWhen(Config.allRecipes,			UnlockAll.class);
		registerWhen(Config.chunkloader,		Loadstone.class);
		registerWhen(Config.multiplayerSleep,	MultiplayerSleep.class);
		registerWhen(Config.armoredElytra,		ArmoredElytra.class);
		registerWhen(Config.timberMod,			TimberEnchant.class);
		registerWhen(Config.hopperFilter,		HopperFilter.class);
		registerWhen(Config.sneakyMobs,			SneakyMobs.class);
		registerWhen(Config.rightClickHarvest,	RightClickHarvest.class);
		registerWhen(Config.doubleShulkerDrop,	DoubleShulker.class);
		registerWhen(Config.cauldronConcrete,	CauldronConcrete.class);
		registerWhen(Config.discordIntegration,	DiscordIntegration.class);
		registerWhen(Config.lockableChests, 	LockableChest.class);
		registerWhen(Config.anvilColorCodes, 	AnvilRename.class);
		registerWhen(Config.teleportAnchors, 	TeleportAnchor.class);
		registerWhen(Config.gpsCompass, 		GPSCompass.class);
		registerWhen(Config.soulboundBook, 		SoulboundEnchant.class);
		registerWhen(Config.leashableVillagers, LeashableVillagers.class);
		registerWhen(Config.refillableRocket, 	RefillableRocket.class);
		registerWhen(Config.deathCoords, 		DeathCoords.class);
		// registerWhen(Config.perPlayerLoot, 		PerPlayerLoot.class);
		// registerWhen(Config.elevatorBlock, 		Elevator.class);
		registerWhen(Config.preventCropTrample, CropTrample.class);
		registerWhen(Config.fastLeafDecay, 		FastLeafDecay.class);

		Log.info("Startup Complete");
	}

}
