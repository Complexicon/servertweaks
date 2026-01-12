package dev.cmplx.servertweaks.tweaks.items;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;

public class PictureMap implements Listener {

	static final Gson gson = new Gson();
	static final NamespacedKey picture_maps = new NamespacedKey(Main.pluginRef, "picture_maps");
	static final Type pictureMapTableType = new TypeToken<Map<Integer, String>>() {
	}.getType();
	static final Path cachePath = Main.pluginRef.getDataFolder().toPath().resolve("picturemaps_cache");
	static MessageDigest sha256;

	public PictureMap() {
		try {
			sha256 = MessageDigest.getInstance("SHA-256");
		} catch (Exception e) {
			/* impossible (i really hope so) */
		}
	}

	static {
		cachePath.toFile().mkdir();
	}

	static class PictureMapRenderer extends MapRenderer {
		final String id;

		public PictureMapRenderer(String id) {
			this.id = id;
		}

		public void render(MapView map, MapCanvas canvas, Player player) {
			try {
				var img = ImageIO.read(cachePath.resolve(id).toFile());
				canvas.drawImage(0, 0, img);
			} catch (Exception e) {
			}
		}
	}

	static Map<Integer, String> getPictureMapTable(World w) {
		var pictureMapsString = Util.getPersistentString(w, picture_maps);
		if (pictureMapsString == null)
			pictureMapsString = "{}";
		return gson.fromJson(pictureMapsString, pictureMapTableType);
	}

	public static ItemStack createNewMap(World associatedWorld, BufferedImage image) {

		String uid = "";

		try {
			var baos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", baos);
			var imageBytes = baos.toByteArray();

			sha256.reset();
			var hashBytes = sha256.digest(imageBytes);

			StringBuilder hash = new StringBuilder();

			for (byte b : hashBytes)
				hash.append(String.format("%02x", b));

			uid = hash.toString();
			
			var cacheFile = cachePath.resolve(uid).toFile();

			if (!cacheFile.exists()) { // cheap deduplication
				var cacheFileStream = new FileOutputStream(cacheFile);
				cacheFileStream.write(imageBytes);
				cacheFileStream.close();
			}


		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		var item = new ItemStack(Material.FILLED_MAP);
		var meta = (MapMeta) item.getItemMeta();

		var view = Bukkit.createMap(associatedWorld);
		view.addRenderer(new PictureMapRenderer(uid));
		view.setScale(Scale.FARTHEST);

		var pictureMapTable = getPictureMapTable(associatedWorld);

		pictureMapTable.put(view.getId(), uid);

		Util.setPersistent(associatedWorld, picture_maps, gson.toJson(pictureMapTable));

		meta.setMapView(view);
		item.setItemMeta(meta);

		return item;
	}

	@EventHandler
	void onMapInit(MapInitializeEvent e) {
		var mapView = e.getMap();

		var pictureMapTable = getPictureMapTable(mapView.getWorld());

		if (pictureMapTable.containsKey(mapView.getId())) {
			Log.info("Restoring Map #" + mapView.getId() + " to  Picture with ID "
					+ pictureMapTable.get(mapView.getId()));
			mapView.getRenderers().stream().forEach(entry -> mapView.removeRenderer(entry));
			mapView.addRenderer(new PictureMapRenderer(pictureMapTable.get(mapView.getId())));
		}
	}

}