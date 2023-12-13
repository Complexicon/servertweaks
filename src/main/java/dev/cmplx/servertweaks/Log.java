package dev.cmplx.servertweaks;

import java.util.logging.Level;

import org.bukkit.Bukkit;

public class Log {
	public static void info    (String message) { Main.pluginRef.getLogger().info(message); }
	public static void error   (String message) { Main.pluginRef.getLogger().log(Level.SEVERE, message); }
	public static void warning (String message) { Main.pluginRef.getLogger().log(Level.WARNING, message); }
	public static void debug   (String message) { Bukkit.broadcastMessage("[DEBUG]: " + message); }
}
