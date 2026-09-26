package tw.sac.serverbridge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class ServerBridgePlugin extends JavaPlugin implements Listener {
    private static final String USE_PERMISSION = "serverbridge.use";
    private static final String ADMIN_PERMISSION = "serverbridge.admin";

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private NamespacedKey arrivalCookie;
    private final Map<UUID, String> occupiedBridge = new HashMap<>();
    private final Map<UUID, Long> cooldownUntil = new HashMap<>();
    private Map<String, Bridge> bridges = Map.of();
    private long cooldownMillis;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        arrivalCookie = new NamespacedKey(this, "arrival");
        loadBridgeConfig();
        registerCommands();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, VelocityConnector.CHANNEL);

        long interval = Math.max(1L, getConfig().getLong("check-interval-ticks", 5L));
        getServer().getScheduler().runTaskTimer(this, this::scanPlayers, interval, interval);
        getLogger().info("Loaded " + bridges.size() + " enabled bridge(s).");
    }

    private void registerCommands() {
        PluginCommand command = getCommand("serverbridge");
        if (command == null) {
            throw new IllegalStateException("serverbridge command is missing from plugin.yml");
        }
        BridgeCommand handler = new BridgeCommand();
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }

    private void loadBridgeConfig() {
        reloadConfig();
        bridges = Map.copyOf(BridgeConfig.load(getConfig().getConfigurationSection("bridges"), getLogger()));
        cooldownMillis = Math.max(0L, getConfig().getLong("transfer-cooldown-seconds", 5L)) * 1000L;
        occupiedBridge.clear();
    }

    private void scanPlayers() {
        long now = System.currentTimeMillis();
        cooldownUntil.entrySet().removeIf(entry -> entry.getValue() <= now);

        for (Player player : getServer().getOnlinePlayers()) {
            Bridge current = findBridge(player);
            String previous = occupiedBridge.get(player.getUniqueId());

            if (current == null) {
                occupiedBridge.remove(player.getUniqueId());
                continue;
            }
            if (current.id().equals(previous)) {
                continue;
            }
            occupiedBridge.put(player.getUniqueId(), current.id());
            if (player.hasPermission(USE_PERMISSION) && !cooldownUntil.containsKey(player.getUniqueId())) {
                transfer(player, current);
            }
        }
    }

    private Bridge findBridge(Player player) {
        for (Bridge bridge : bridges.values()) {
            if (bridge.contains(player.getLocation())) {
                return bridge;
            }
        }
        return null;
    }

    private void transfer(Player player, Bridge bridge) {
        cooldownUntil.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMillis);
        if (bridge.arrival() != null) {
            player.storeCookie(arrivalCookie, TransferTicket.encode(bridge.arrival()));
        }
        player.sendMessage(message("transferring", "<aqua>正在前往 <white><server></white>…</aqua>", "server", bridge.displayName()));
        if (bridge.connectionMode() == Bridge.ConnectionMode.VELOCITY) {
            VelocityConnector.connect(this, player, bridge.proxyServer());
        } else {
            player.transfer(bridge.host(), bridge.port());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.retrieveCookie(arrivalCookie).thenAccept(data -> {
            if (data == null || data.length == 0) {
                return;
            }
            player.storeCookie(arrivalCookie, new byte[0]);
            TransferTicket.decode(data).ifPresent(arrival -> getServer().getScheduler().runTask(this, () -> {
                if (!player.isOnline()) {
                    return;
                }
                World world = getServer().getWorld(arrival.world());
                if (world == null) {
                    getLogger().warning("Arrival world '" + arrival.world() + "' is not loaded for " + player.getName());
                    return;
                }
                player.teleportAsync(new Location(world, arrival.x(), arrival.y(), arrival.z(), arrival.yaw(), arrival.pitch()));
            }));
        }).exceptionally(exception -> {
            getLogger().warning("Could not read transfer destination for " + player.getName() + ": " + exception.getMessage());
            return null;
        });
    }

    private Component message(String key, String fallback) {
        String prefix = getConfig().getString("messages.prefix", "");
        return miniMessage.deserialize(prefix + getConfig().getString("messages." + key, fallback));
    }

    private Component message(String key, String fallback, String placeholder, String value) {
        String prefix = getConfig().getString("messages.prefix", "");
        String raw = prefix + getConfig().getString("messages." + key, fallback);
        return miniMessage.deserialize(raw.replace("<" + placeholder + ">", MiniMessage.miniMessage().escapeTags(value)));
    }

    private final class BridgeCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
                String names = bridges.isEmpty() ? "(none)" : String.join(", ", bridges.keySet());
                sender.sendMessage(miniMessage.deserialize("<aqua>Bridges:</aqua> <white>" + MiniMessage.miniMessage().escapeTags(names) + "</white>"));
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission(ADMIN_PERMISSION)) {
                    sender.sendMessage(message("no-permission", "<red>你沒有權限。</red>"));
                    return true;
                }
                loadBridgeConfig();
                sender.sendMessage(message("reloaded", "<green>設定已重新載入。</green>"));
                return true;
            }

            if (args[0].equalsIgnoreCase("go") && args.length >= 2 && sender instanceof Player player) {
                if (!player.hasPermission(USE_PERMISSION)) {
                    player.sendMessage(message("no-permission", "<red>你沒有權限。</red>"));
                    return true;
                }
                Bridge bridge = bridges.get(args[1].toLowerCase(Locale.ROOT));
                if (bridge == null) {
                    player.sendMessage(message("unknown-bridge", "<red>找不到連接點 <white><bridge></white>。</red>", "bridge", args[1]));
                    return true;
                }
                transfer(player, bridge);
                return true;
            }

            sender.sendMessage(miniMessage.deserialize("<yellow>/serverbridge &lt;list|go|reload&gt;</yellow>"));
            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            if (args.length <= 1) {
                List<String> options = new ArrayList<>(List.of("list", "go"));
                if (sender.hasPermission(ADMIN_PERMISSION)) {
                    options.add("reload");
                }
                return options;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("go")) {
                return List.copyOf(bridges.keySet());
            }
            return List.of();
        }
    }
}
