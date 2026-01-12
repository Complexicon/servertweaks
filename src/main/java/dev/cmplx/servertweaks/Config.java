package dev.cmplx.servertweaks;

import java.util.ArrayList;
import java.util.List;

public class Config {

	private Config() {} // disable instance creation

	public static int weeklyLimit = 60 * 60 * 24;
	public static boolean enableWeeklyLimit = true;
	public static String playtimeResetCronjob = "0 1 * * MON";
	
	public static String deathMessageFormat = "&4{deathMessage}";
	public static String chatMessageFormat = "&b{player}&f: {message}";

	public static int afkTime = 60 * 5;

	public static String discordBotToken = "replaceme";
	public static String discordChannelId = "replaceme";
	public static boolean discordIntegration = false;
	
	public static boolean hopperFilter = true;
	public static boolean lockableChests = true;
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
	public static boolean leashableVillagers = true;
	public static boolean multiplayerSleep = true;
	public static boolean sneakyMobs = true;
	public static boolean armoredElytra = true;
	public static boolean gpsCompass = true;
	public static boolean teleportAnchors = true;
	public static boolean anvilColorCodes = true;
	public static boolean soulboundBook = true;
	public static boolean refillableRocket = true;
	public static boolean deathCoords = true;
	// public static boolean perPlayerLoot = true; // experimental
	public static boolean mapFromPicture = true;
	// public static boolean elevatorBlock = true;
	public static boolean preventCropTrample = true;
	public static boolean fastLeafDecay = true;
	public static boolean librarianHelper = true;

	public static int chanceTimberEnchant = 25;
	public static int chanceSoulbound = 10;
	public static int chanceTeleportBook = 25;

	public static String motdHeader = "&cExample Message of the &bDay";
	public static List<String> motdList = new ArrayList<>(){{
		add("&cTest");
	}};

}
