package dev.cmplx.servertweaks;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;

import org.bukkit.Server;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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

	public static void setPersistent(PersistentDataHolder holder, NamespacedKey key, String value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
	}

	public static void setPersistent(PersistentDataHolder holder, NamespacedKey key, List<String> value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, String.join("\n", value));
	}

	public static void setPersistent(PersistentDataHolder holder, NamespacedKey key, int value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
	}

	public static void setPersistent(PersistentDataHolder holder, NamespacedKey key, boolean value) {
		holder.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, value);
	}

	public static Integer getPersistentInt(PersistentDataHolder holder, NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
	}

	public static String getPersistentString(PersistentDataHolder holder, NamespacedKey key) {
		return holder.getPersistentDataContainer().get(key, PersistentDataType.STRING);
	}

	public static boolean getPersistentBool(PersistentDataHolder holder, NamespacedKey key) {
		return holder.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BOOLEAN, false);
	}

	public static List<String> getPersistentStringList(PersistentDataHolder holder, NamespacedKey key) {
		var list = holder.getPersistentDataContainer().get(key, PersistentDataType.STRING);
		if (list == null) return null;
		return Arrays.asList(list.split("\n"));
	}

	public static ItemStack createHead(String url, String name) {
		var skull = new ItemStack(Material.PLAYER_HEAD);

		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		var profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
		var texture = profile.getTextures();
		try {
			texture.setSkin(URI.create("https://textures.minecraft.net/texture/" + url).toURL());
			profile.setTextures(texture);
		} catch (Exception e) {}
		skullMeta.setOwnerProfile(profile);
		skullMeta.setDisplayName(name);
		skull.setItemMeta(skullMeta);

		return skull;
	}

	public final static ItemStack[] numbers = new ItemStack[]{
		createHead("6b87b44111a9f339ed7015d0f2cb646ce6b8c59a0b750b272449aeac25625dfa", "0"),
		createHead("d2d4a69937e0beadc38426c0994b50d950406fd8da9f31c582d46f3b9bfc4c5b", "1"),
		createHead("30a6c7a0d658bb90e27b5934f62a5e15cc9c11c87ae1464a4e79ea66523ba361", "2"),
		createHead("161b31a87b78262c63e94714e5624a2ab5950f75dee32cc3026a5fa7823468de", "3"),
		createHead("3adfd3c99967d3274902ecb6e98658acfdb3918717b2e9037f61c3b4e09e2a1", "4"),
		createHead("8bbaf01909221b9abe945afe7dfdb72f3173311e5620194dd27011a6d554ffc8", "5"),
		createHead("e6ae0fe2256ae356a25f130ae71cf44315157c5fae91d62a4ffb585b16486373", "6"),
		createHead("8f09efe731e73c80b1aee100bb330ab414595ee54a4e2dec439bed3e3649ac96", "7"),
		createHead("d727d4e48f231ce4d871992560f51bf6a3f157c2ffd6f92b860cbb53128426a2", "8"),
		createHead("a473ea351f41e9957f914e3b90f74e9867838b3339d42513ca25ed0f45bc60cb", "9"),
	};

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
