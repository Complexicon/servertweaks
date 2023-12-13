package dev.cmplx.servertweaks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class PartyCommand implements CommandExecutor/*, TabCompleter */ {

	// List<String> subcommands = Arrays.asList("create", "setname", "leave", "invite");

	NamespacedKey partyKey = new NamespacedKey(Main.pluginRef, "party");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can run this command !");
			return false;
		}
		Player p = (Player)sender;


		if(args.length < 1) {
			sender.sendMessage(Util.fixColor("&cMissing Subcommand!"));
			return false;
		}

		switch(args[0]) {
			case "invite":
				if(args.length != 2) {
					sender.sendMessage(Util.fixColor("&cMissing Playername!"));
					return false;
				}
				invite(p, args[1]);
				break;
			case "accept":
				if(args.length != 2) {
					sender.sendMessage(Util.fixColor("&cMissing Partyname!"));
					return false;
				}
				accept(p, args[1]);
				break;
			case "setname":
				if(args.length != 2) {
					sender.sendMessage(Util.fixColor("&cMissing Displayname!"));
					return false;
				}
				setName(p, args[1]);
				break;
			case "leave":
				leave(p);
				break;
			case "create":
				create(p);
				break;
			default:
				sender.sendMessage(Util.fixColor("&cInvalid Command!"));
				return false;
		}

		return true;
	}

	void accept(Player sender, String teamName) {
		var party = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);

		if(party == null) {
			sender.sendMessage(Util.fixColor("&cInvalid Party Name!"));
			return;
		}

		party.addEntry(sender.getName());
		Util.setPersistent(sender, partyKey, teamName);
		sender.sendMessage(Util.fixColor("&aDu bist " + party.getDisplayName() + " &aBeigetreten!"));
	}

	void invite(Player sender, String toInvite) {
		
		String party = Util.getPersistentString(sender, partyKey);
		if(party == null) {
			sender.sendMessage(Util.fixColor("&cNot in a Party!"));
			return;
		}

		var targetPlayer = Bukkit.getPlayer(toInvite);

		if(targetPlayer == null) {
			sender.sendMessage(Util.fixColor("&cPlayer not found!"));
			return;
		}
		
		var accept = new TextComponent("Hier klicken");
		accept.setColor(ChatColor.GREEN);
		accept.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/party accept " + party));
		targetPlayer.spigot().sendMessage(
			new TextComponent(Util.fixColor("Du wurdest von " + sender.getName() + " zu einer Party eingeladen. Zum Akzeptieren ")),
			accept
		);
		sender.sendMessage(Util.fixColor("&aEinladung verschickt!"));
	}

	void create(Player sender) {
		var partyName = UUID.randomUUID().toString();
		var party = Util.getTeamSafe(partyName);
		party.addEntry(sender.getName());
		Util.setPersistent(sender, partyKey, partyName);
		sender.sendMessage(Util.fixColor("&aParty erstellt!"));
	}

	void leave(Player sender) {

		String party = Util.getPersistentString(sender, partyKey);
		if(party == null) {
			sender.sendMessage(Util.fixColor("&cNot in a Party!"));
			return;
		}

		Util.getTeamSafe(party).removeEntry(sender.getName());
		sender.getPersistentDataContainer().remove(partyKey);
		sender.sendMessage("Party wurde verlassen.");
	}

	final String colors = "0123456789abcdef";

	void setName(Player sender, String name) {
		String party = Util.getPersistentString(sender, partyKey);
		if(party == null) {
			sender.sendMessage(Util.fixColor("&cNot in a Party!"));
			return;
		}

		var coloredName = Util.fixColor(name);

		if(ChatColor.stripColor(coloredName).length() > 5) {
			sender.sendMessage(Util.fixColor("&cName too long! Max. 5"));
			return;
		}

		var partyTeam = Util.getTeamSafe(party);
		partyTeam.setDisplayName(coloredName);
		var lastColorString = org.bukkit.ChatColor.getLastColors(coloredName);
		if(lastColorString.isEmpty()) lastColorString = "ff";
		partyTeam.setColor(org.bukkit.ChatColor.getByChar(lastColorString.charAt(1)));
		partyTeam.setPrefix(coloredName + " ");
		sender.sendMessage(Util.fixColor("&aName geändert zu: " + coloredName));
	}

	// @Override
	// public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

	// 	if (!(sender instanceof Player)) {
	// 		sender.sendMessage("Only players can run this command !");
	// 		return Arrays.asList("");
	// 	}

	// 	return subcommands.stream().filter(v -> v.startsWith(args[0])).toList();
	// }

}
