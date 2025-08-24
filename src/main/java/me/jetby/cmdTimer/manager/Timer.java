package me.jetby.cmdTimer.manager;

import lombok.Getter;
import me.jetby.cmdTimer.Main;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.jetby.cmdTimer.utils.Config.CFG;

@Getter
public class Timer {

    private final Map<UUID, BukkitTask> activeTimers = new HashMap<>();
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();
    private final Map<UUID, Integer> playerCountdowns = new HashMap<>();

    private final Main plugin;

    public Timer(Main plugin) {
        this.plugin = plugin;
    }

    public void startTimer(Player player, String command, int time) {
        BossBar bossBar = Bukkit.createBossBar(
                CFG().getString("BossBar.countdown").replace('&', '§'),
                BarColor.valueOf(CFG().getString("BossBar.Color")),
                BarStyle.valueOf(CFG().getString("BossBar.Style"))
        );
        bossBar.addPlayer(player);
        playerBossBars.put(player.getUniqueId(), bossBar);
        playerCountdowns.put(player.getUniqueId(), time);

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            UUID playerId = player.getUniqueId();
            int currentCountdown = playerCountdowns.get(playerId);

            if (currentCountdown > 0) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    double progress = currentCountdown / (double) time;
                    bossBar.setProgress(progress);
                    bossBar.setTitle(CFG().getString("BossBar.countdown")
                            .replace('&', '§')
                            .replace("{timer}", String.valueOf(currentCountdown)));
                });

                playerCountdowns.put(playerId, currentCountdown - 1);

            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    bossBar.removeAll();
                    activeTimers.remove(playerId);
                    playerBossBars.remove(playerId);
                    playerCountdowns.remove(playerId);

                    Bukkit.dispatchCommand(player, command);

                    List<String> actions = CFG().getStringList("actions.end");
                    for (String action : actions) {
                        plugin.getActions().execute(player, action.replace("%time%", String.valueOf(time)));
                    }
                });

                Bukkit.getScheduler().cancelTask(activeTimers.get(playerId).getTaskId());
            }
        }, 0, 20);

        activeTimers.put(player.getUniqueId(), task);
    }

    public void cancelTimer(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeTimers.containsKey(playerId)) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                activeTimers.get(playerId).cancel();
                activeTimers.remove(playerId);

                if (playerBossBars.containsKey(playerId)) {
                    playerBossBars.get(playerId).removeAll();
                    playerBossBars.remove(playerId);
                }

                playerCountdowns.remove(playerId);

                List<String> actions = CFG().getStringList("actions.cancel");
                for (String action : actions) {
                    plugin.getActions().execute(player, action);
                }
            });
        }
    }
}