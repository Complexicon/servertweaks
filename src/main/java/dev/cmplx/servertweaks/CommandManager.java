package dev.cmplx.servertweaks;

import dev.cmplx.servertweaks.commands.AfkCommand;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;
import dev.cmplx.servertweaks.commands.PartyCommand;
import dev.cmplx.servertweaks.commands.TestCommand;

public class CommandManager {

	public static void init() {
		var partyCommand = new PartyCommand();
		Main.pluginRef.getCommand("party").setExecutor(partyCommand);
		Main.pluginRef.getCommand("party").setTabCompleter(partyCommand);

		Main.pluginRef.getCommand("debugitems").setExecutor(new DebugItemsCommand());
		Main.pluginRef.getCommand("testcmd").setExecutor(new TestCommand());

		Main.pluginRef.getCommand("afk").setExecutor(new AfkCommand());

	}

}
