package dev.cmplx.servertweaks;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.IntFunction;

import org.bukkit.Server;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class Util {

	private static Object minecraftServer;
	private static Field recentTps;
	private static Scoreboard globalScoreboard;

	private static List<Field> configFields;

	public static BukkitScheduler scheduler;

	public static void setupConfig() {
		FileConfiguration conf = Main.pluginRef.getConfig();

		configFields = Arrays.stream(Config.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers())).toList();

		try {
			for (Field f : configFields) {
				conf.addDefault(f.getName(), f.get(null));
			}
	
			conf.options().copyDefaults(true);
	
			Main.pluginRef.saveConfig();
	
			for(Field f : configFields) {
	
				if(ReflectionHelper.isAssignableTo(conf.get(f.getName()).getClass(), f.getType())) {
					f.set(null, conf.get(f.getName())); // load from config if matches type
				} else {
					conf.set(f.getName(), f.get(null)); // invalid type -> set default
				}
	
			}
		} catch(Exception e) {
			e.printStackTrace();
			Log.error("failed to setup config!");
		}



		Main.pluginRef.saveConfig();
	}

	public static void saveConfig() {

		FileConfiguration conf = Main.pluginRef.getConfig();

		try {
			for (Field f : configFields) {
				conf.set(f.getName(), f.get(null));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("failed to save config!");
		}

		Main.pluginRef.saveConfig();
	}

	public static void init() {
		Server server = Main.pluginRef.getServer();
		try {
			Field consoleField = server.getClass().getDeclaredField("console");
			consoleField.setAccessible(true);
			minecraftServer = consoleField.get(server);
			recentTps = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
			recentTps.setAccessible(true);
		} catch (Exception e) {
		}

		globalScoreboard = server.getScoreboardManager().getMainScoreboard();

		scheduler = server.getScheduler();
	}

	public static String fixColor(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public static Objective getObjectiveSafe(String name) {
		return getObjectiveSafe(name, name, Criteria.DUMMY);
	}

	public static Objective getObjectiveSafe(String name, String displayName) {
		return getObjectiveSafe(name, displayName, Criteria.DUMMY);
	}

	public static Objective getObjectiveSafe(String name, String displayName, Criteria criteria) {
		return getObjectiveSafe(name, displayName, criteria, null);
	}

	public static Objective getObjectiveSafe(String name, String displayName, Criteria criteria, DisplaySlot slot) {
		if (globalScoreboard.getObjective(name) == null) {
			Objective o = globalScoreboard.registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
			if (slot != null)
				o.setDisplaySlot(slot);
			return o;
		} else {
			return globalScoreboard.getObjective(name);
		}
	}

	public static Team getTeamSafe(String name) {
		if(globalScoreboard.getTeam(name) == null) {
			return globalScoreboard.registerNewTeam(name);
		} else {
			return globalScoreboard.getTeam(name);
		}
	}

	public static double getTPSLastMin() {
		try {
			return ((double[]) recentTps.get(minecraftServer))[0];
		} catch (Exception e) {
			return 0;
		}
	}

	public static void modifyScore(Score score, IntFunction<Integer> handle) {
		score.setScore(handle.apply(score.getScore()));
	}

	public static void setMetadata(Metadatable where, String key, Object what) {
		where.setMetadata(key, new FixedMetadataValue(Main.pluginRef, what));
	}

	public static <T extends Object> T getMetadata(Metadatable from, String key, Class<T> clazz) {
		Optional<MetadataValue> metaVal = from.getMetadata(key).stream().filter(v -> v.getOwningPlugin().equals(Main.pluginRef)).findFirst();
		if (metaVal.isPresent() && clazz.isInstance(metaVal.get().value()))
			return clazz.cast(metaVal.get().value());
		return null;
	}

	private static class ReflectionHelper {
		private static final Map<Class<?>, Class<?>> primitiveWrapperMap =
		Map.of(boolean.class, Boolean.class,
				byte.class, Byte.class,
				char.class, Character.class,
				double.class, Double.class,
				float.class, Float.class,
				int.class, Integer.class,
				long.class, Long.class,
				short.class, Short.class);

		private static boolean isPrimitiveWrapperOf(Class<?> targetClass, Class<?> primitive) {
			if (!primitive.isPrimitive()) {
				throw new IllegalArgumentException("First argument has to be primitive type");
			}
			return primitiveWrapperMap.get(primitive) == targetClass;
		}

		public static boolean isAssignableTo(Class<?> from, Class<?> to) {
			if (to.isAssignableFrom(from)) {
				return true;
			}
			if (from.isPrimitive()) {
				return isPrimitiveWrapperOf(to, from);
			}
			if (to.isPrimitive()) {
				return isPrimitiveWrapperOf(from, to);
			}
			return false;
		}
	}

}
