package dev.cmplx.servertweaks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;

import dev.cmplx.servertweaks.items.ArmoredElytra;
import dev.cmplx.servertweaks.items.TimberEnchant;
import dev.cmplx.servertweaks.tweaks.CauldronConcrete;
import dev.cmplx.servertweaks.tweaks.ConfigurableVillager;
import dev.cmplx.servertweaks.tweaks.CraftingCustomizer;
import dev.cmplx.servertweaks.tweaks.DiscordIntegration;
import dev.cmplx.servertweaks.tweaks.DoubleShulker;
import dev.cmplx.servertweaks.tweaks.HopperFilter;
import dev.cmplx.servertweaks.tweaks.Loadstone;
import dev.cmplx.servertweaks.tweaks.LockableChest;
import dev.cmplx.servertweaks.tweaks.MobGriefing;
import dev.cmplx.servertweaks.tweaks.MultiplayerSleep;
import dev.cmplx.servertweaks.tweaks.QuickOpen;
import dev.cmplx.servertweaks.tweaks.RightClickHarvest;
import dev.cmplx.servertweaks.tweaks.SneakyMobs;
import dev.cmplx.servertweaks.tweaks.Timber;
import dev.cmplx.servertweaks.tweaks.UnlockAll;

public class Main extends JavaPlugin {

	public static Plugin pluginRef;
	
	void registerWhen(boolean when, Class<? extends Listener> listener) {
		try { if(when) Bukkit.getPluginManager().registerEvents(listener.getDeclaredConstructor().newInstance(), this); } catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	public void onEnable() {
		pluginRef = this;

		Util.init();
		Util.setupConfig();
		Cron.init();

		PlaytimeTracker.init();
		CraftingCustomizer.init();

		Util.getObjectiveSafe("deathCounter", "Tode", Criteria.DEATH_COUNT, DisplaySlot.PLAYER_LIST);

		getCommand("party").setExecutor(new PartyCommand());
		// getCommand("party").setTabCompleter(party);

		// these are necessary
		registerWhen(true, MOTDHelper.class);
		registerWhen(true, PlaytimeTracker.class);
		registerWhen(true, ChatEvents.class);
		registerWhen(true, TabListHelper.class);
		registerWhen(true, ConfigurableVillager.class);

		// optional events
		registerWhen(true,				QuickOpen.class); // handled internally
		registerWhen(true,				MobGriefing.class); // handled internally
		registerWhen(true, 			ToolStats.class);
		registerWhen(Config.allRecipes,			UnlockAll.class);
		registerWhen(Config.chunkloader,		Loadstone.class);
		registerWhen(Config.multiplayerSleep,	MultiplayerSleep.class);
		registerWhen(Config.armoredElytra,		ArmoredElytra.class);
		registerWhen(Config.timberMod,			TimberEnchant.class);
		registerWhen(Config.timberMod,			Timber.class);
		registerWhen(Config.hopperFilter,		HopperFilter.class);
		registerWhen(Config.sneakyMobs,			SneakyMobs.class);
		registerWhen(Config.rightClickHarvest,	RightClickHarvest.class);
		registerWhen(Config.doubleShulkerDrop,	DoubleShulker.class);
		registerWhen(Config.cauldronConcrete,	CauldronConcrete.class);
		registerWhen(Config.discordIntegration, DiscordIntegration.class);
		registerWhen(Config.lockableChests, 	LockableChest.class);
	}

}
