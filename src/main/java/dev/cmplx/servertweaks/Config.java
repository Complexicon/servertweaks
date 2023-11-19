package dev.cmplx.servertweaks;

import java.util.ArrayList;
import java.util.List;

public class Config {

	public static int weeklyLimit = 60 * 60 * 24;
	public static boolean enableWeeklyLimit = true;
	public static String playtimeResetCronjob = "0 1 * * MON";
	public static String playtimeLimitReachedMessage = "§4You have reached your weekly playtime limit.";

	public static String joinMessage = "&a{player} joined.";
	public static String leaveMessage = "&c{player} left.";
	public static String deathMessage = "&4{deathMessage}";
	public static String chatFormat = "&b{player}&f: {message}";

	public static int afkTime = 60 * 5;
	public static String afkMessage = "&7{player} is now AFK";
	public static String afkReturnMessage = "&7{player} is no longer AFK";

	public static boolean hopperFilter = true;
	public static boolean chunkloader = true;
	public static boolean shiftOpenEnder = true;
	public static boolean shiftOpenCraft = true;
	public static boolean rightClickHarvest = true;
	public static boolean allRecipes = true;
	public static boolean blockMobExplosion = true;
	public static boolean blockEnderGrief = true;
	public static boolean doubleShulkerDrop = true;
	public static boolean cauldronConcrete = true;
	public static boolean timberMod = true;
	public static boolean multiplayerSleep = true;
	public static boolean sneakyMobs = true;
	public static boolean armoredElytra = true;

	public static String motdHeader = "&cExample Message of the &bDay";
	public static List<String> motdList = new ArrayList<>(){{
		add("&cTest");
	}};

}
