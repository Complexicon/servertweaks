package dev.cmplx.servertweaks;

import dev.cmplx.servertweaks.commands.AfkCommand;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.commands.PartyCommand;
import dev.cmplx.servertweaks.commands.PictureMap;
import dev.cmplx.servertweaks.commands.PlaytimeCommand;
import dev.cmplx.servertweaks.commands.ScheduleShutdown;
import dev.cmplx.servertweaks.commands.TestCommand;

public class CommandManager {

	public static void init() {
		var plugin = Main.pluginRef;
		var partyCommand = new PartyCommand();
		plugin.getCommand("party").setExecutor(partyCommand);
		plugin.getCommand("party").setTabCompleter(partyCommand);

		plugin.getCommand("debugitems").setExecutor(new DebugItemsCommand());
		plugin.getCommand("testcmd").setExecutor(new TestCommand());

		plugin.getCommand("afk").setExecutor(new AfkCommand());

		plugin.getCommand("playtime").setExecutor(new PlaytimeCommand());

		var scheduleShutdownCommand = new ScheduleShutdown();
		plugin.getCommand("scheduleshutdown").setExecutor(scheduleShutdownCommand);
		plugin.getCommand("scheduleshutdown").setTabCompleter(scheduleShutdownCommand);

		if (Config.mapFromPicture) plugin.getCommand("picturemap").setExecutor(new PictureMap());
	}

}
