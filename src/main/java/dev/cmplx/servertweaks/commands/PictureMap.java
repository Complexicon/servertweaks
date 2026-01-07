package dev.cmplx.servertweaks.commands;

import java.net.URL;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import dev.cmplx.servertweaks.Util;

public class PictureMap implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(Util.fixColor("&cMust be Player!"));
			return true;
		}

		Player p = (Player) sender;

		var item = p.getInventory().getItemInMainHand();

		if (item.getType() != Material.MAP) {
			sender.sendMessage(Util.fixColor("&cMust hold Empty Map!"));
			return true;
		}

		if (args.length != 1) {
			sender.sendMessage("Usage: /picturemap <url>");
			return true;
		}

		try {

			var url = new URL(args[0]);

			if (!url.getHost().equals("i.postimg.cc")) {
				sender.sendMessage(Util.fixColor("&cPicture must be from https://i.postimg.cc !"));
				return true;
			}

			if (!url.getPath().endsWith(".png")) {
				sender.sendMessage(Util.fixColor("&cPicture must be a .png file!"));
				return true;
			}

			var image = ImageIO.read(url);

			var view = Bukkit.createMap(p.getWorld());
			view.setScale(Scale.FARTHEST);
			
			view.addRenderer(new MapRenderer() {
				@Override
				public void render(MapView map, MapCanvas canvas, Player player) {
					canvas.drawImage(0, 0, image);
				}
			});
			
			item.setType(Material.FILLED_MAP);
			var meta = (MapMeta) item.getItemMeta();

			if (meta != null) {
				meta.setMapView(view);
				item.setItemMeta(meta);
			}

		} catch (Exception e) {
			sender.sendMessage(Util.fixColor("&cSomething went wrong!"));
			e.printStackTrace();
		}

		return true;
	}

}
