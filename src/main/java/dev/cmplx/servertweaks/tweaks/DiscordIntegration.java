package dev.cmplx.servertweaks.tweaks;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import dev.cmplx.servertweaks.Config;
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;

public class DiscordIntegration implements Listener, WebSocket.Listener, Runnable {

	HttpClient client;
	WebSocket gateway;
	Integer lastSeq;
	BukkitTask heartbeatTask;
	boolean ready = false;
	Gson gson;
	URI webhookURI;

	public DiscordIntegration() {
		client = HttpClient.newHttpClient();
		Util.scheduler.runTaskAsynchronously(Main.pluginRef, this); // dispatch everything http & websockety
		gson = new Gson();
	}

	@Override
	public void run() {

		Log.info("connecting to discord");
		var request = HttpRequest
				.newBuilder(URI.create("https://discord.com/api/channels/" + Config.discordChannelId + "/webhooks"))
				.header("Authorization", "Bot " + Config.discordBotToken)
				.GET()
				.build();

		try {
			var resp = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
			if (resp.statusCode() != 200)
				throw new Exception("bad status code " + resp.statusCode());
			JsonArray hooks = (JsonArray) JsonParser.parseString(resp.body());

			Optional<JsonElement> syncWebhook = hooks.asList().stream()
					.filter(v -> v.isJsonObject() && v.getAsJsonObject().get("name").getAsString().equals("chat-sync"))
					.findFirst();

			if (!syncWebhook.isPresent())
				throw new Exception("no webhook with name 'chat-sync' in channel");

			webhookURI = URI.create(syncWebhook.get().getAsJsonObject().get("url").getAsString());

		} catch (Exception e) {
			Log.error("failed to get chat-sync-channel webhook. Discord Integration disabled.");
			e.printStackTrace();
			return;
		}

		gateway = client.newWebSocketBuilder()
				.buildAsync(URI.create("wss://gateway.discord.gg/?v=10&encoding=json"), this).join();

		Log.info("Discord Integration Online.");
	}

	void destroyWebsocket() {
		destroyWebsocket(true);
	}

	void destroyWebsocket(boolean doReconnect) {
		if (heartbeatTask != null)
			heartbeatTask.cancel();
		if (gateway == null)
			return;
		gateway.abort();
		gateway = null;
		System.gc();

		if(!doReconnect) return;

		Util.scheduler.runTaskLater( // reconnect after 10 seconds
			Main.pluginRef,
			() -> Util.scheduler.runTaskAsynchronously(Main.pluginRef, this), // dispatch everything http & websockety
			20 * 10
		);
	}

	@EventHandler
	public void onUnload(PluginDisableEvent e) {
		destroyWebsocket(false);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		destroyWebsocket();
		Log.error("discord died");
		error.printStackTrace();
	}

	void doHeartbeat() {
		gateway.sendText("{\"op\":1,\"d\":" + lastSeq + "}", true);
	}

	void handleGatewayMessage(JsonObject req, WebSocket gateway) {
		lastSeq = req.get("s").isJsonNull() ? null : req.get("s").getAsInt();

		if (req.get("d").isJsonNull())
			return;

		var payload = req.get("d").getAsJsonObject();

		if (req.get("op").getAsInt() == 10) { // Discord Setup OpCode
			long heartbeatMs = payload.get("heartbeat_interval").getAsInt();
			long heartbeatTicks = heartbeatMs / (1000 / 20) / 2;
			// start heartbeat service
			heartbeatTask = Util.scheduler.runTaskTimerAsynchronously(
					Main.pluginRef,
					() -> doHeartbeat(),
					heartbeatTicks,
					heartbeatTicks);

			JsonObject authReq = new JsonObject();
			JsonObject auth = new JsonObject();
			JsonObject props = new JsonObject();

			props.addProperty("os", "linux");
			props.addProperty("browser", "ServerTweaks");
			props.addProperty("device", "Minecraft-Server");

			auth.addProperty("token", Config.discordBotToken);
			auth.addProperty("intents", ((1 << 15) | (1 << 9)));
			auth.add("properties", props);

			authReq.addProperty("op", 2);
			authReq.add("d", auth);

			gateway.sendText(new Gson().toJson(authReq), true);
		}

		if (!req.get("t").isJsonNull() && req.get("t").getAsString().equals("MESSAGE_CREATE")) {

			var channel = payload.get("channel_id").getAsString();
			if (!channel.equals(Config.discordChannelId))
				return;

			var author = payload.get("author").getAsJsonObject();
			if (author.has("bot") && author.get("bot").getAsBoolean())
				return; // ignore bot messages

			var sender = author.get("username").getAsString();
			var content = payload.get("content").getAsString();

			if (content.equals(""))
				content = "<empty>";

			Bukkit.broadcastMessage(Util.fixColor("&9Discord>>&6" + sender + ": &f" + content));

		}
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// Log.info("isLast " + last);
		handleGatewayMessage(JsonParser.parseString(data.toString()).getAsJsonObject(), webSocket);
		return WebSocket.Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		destroyWebsocket();
		Log.warning("ended" + reason + statusCode);
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}

	private void responseHandler(HttpResponse<String> response) {
		if (response.statusCode() != 204) {
			Log.warning("Call to Discord Webhook failed!");
			Log.debug(response.body());
		}
	}

	private String stripColor(String in) {
		return ChatColor.stripColor(in.replace('&', '§'));
	}

	private void postMessageAsync(Player p, String message) {
		String avatar = p != null ? ("https://mc-heads.net/avatar/" + p.getUniqueId().toString())
				: "https://static.wikia.nocookie.net/minecraft_gamepedia/images/c/c7/Grass_Block.png/revision/latest/scale-to-width-down/250";
		String user = p != null ? p.getName() : "Server Message";

		var webhookPayload = new JsonObject();

		webhookPayload.add("content", new JsonPrimitive(stripColor(message)));
		webhookPayload.add("username", new JsonPrimitive(user));
		webhookPayload.add("avatar_url", new JsonPrimitive(avatar));

		var req = HttpRequest.newBuilder(webhookURI)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(webhookPayload)))
				.build();

		client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> responseHandler(resp));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent e) {
		postMessageAsync(null, e.getJoinMessage());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onLeave(PlayerQuitEvent e) {
		postMessageAsync(null, e.getQuitMessage());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerDeath(PlayerDeathEvent e) {
		postMessageAsync(null, e.getDeathMessage());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {
		postMessageAsync(e.getPlayer(), e.getMessage());
	}

}
