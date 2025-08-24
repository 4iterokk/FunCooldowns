package me.jetby.cmdTimer.listeners;

import me.jetby.cmdTimer.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

import static me.jetby.cmdTimer.utils.Config.CFG;

public class PlayerCommandPreprocess implements Listener {
    private final Main plugin;
    public PlayerCommandPreprocess(Main plugin) {
        this.plugin = plugin;
    }
    private final List<String> commands = CFG().getStringList("commands");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().substring(1).toLowerCase();
        Player p = e.getPlayer();

        // Получаем команду отмены из конфига
        String cancelCommand = CFG().getString("cancel-command", "/cancelteleport");
        String cancelCommandWithoutSlash = cancelCommand.substring(1).toLowerCase();

        // Проверяем команду отмены
        if (command.equalsIgnoreCase(cancelCommandWithoutSlash)) {
            if (plugin.getTimer().getActiveTimers().containsKey(p.getUniqueId())) {
                plugin.getTimer().cancelTimer(p);
            } else {
                p.sendMessage(CFG().getString("messages.nothing").replace('&', '§'));
            }
            e.setCancelled(true);
            return;
        }

        boolean shouldDelay = commands.stream().anyMatch((configCommand) ->
                configCommand.endsWith("!") ? command.startsWith(configCommand
                        .replace("!", "")) : command.equalsIgnoreCase(configCommand));

        if (shouldDelay) {
            List<String> DisabledWorlds = CFG().getStringList("disabled-worlds");
            if (!DisabledWorlds.isEmpty()) {
                for (String world : DisabledWorlds) {
                    if (p.getWorld().getName().equalsIgnoreCase(world)) {
                        return;
                    }
                }
            }
            List<String> DisabledRegions = CFG().getStringList("disabled-regions");
            if (!DisabledRegions.isEmpty()) {
                if (plugin.getRegion().regionCheck(p.getLocation(), DisabledRegions)) {
                    return;
                }
            }
            if (plugin.getTimer().getActiveTimers().containsKey(e.getPlayer().getUniqueId())) {
                plugin.getTimer().cancelTimer(p);
            }
            int time = CFG().getInt("Timer.default");
            for (String perm : CFG().getConfigurationSection("Timer").getKeys(false)) {
                if (p.hasPermission("funcooldowns." + perm)) {
                    time = CFG().getInt("Timer." + perm);
                    break;
                }
            }
            if (time != 0) {
                e.setCancelled(true);
                List<String> actions = CFG().getStringList("actions.start");
                for (String action : actions) {
                    plugin.getActions().execute(p, action.replace("%time%", String.valueOf(time)));
                }
                plugin.getTimer().startTimer(e.getPlayer(), command, time);
            }
        }
    }
}