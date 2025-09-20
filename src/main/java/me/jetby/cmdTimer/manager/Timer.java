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
    private final Map<UUID, Integer> playerInitialTimes = new HashMap<>();

    private final Main plugin;

    public Timer(Main plugin) {
        this.plugin = plugin;
    }

    public void startTimer(Player player, String command, int time) {
        UUID playerId = player.getUniqueId();

        // Отменяем существующий таймер, если он есть
        if (activeTimers.containsKey(playerId)) {
            cancelTimer(player);
        }

        // Удаляем старый BossBar, если он существует
        if (playerBossBars.containsKey(playerId)) {
            playerBossBars.get(playerId).removeAll();
            playerBossBars.remove(playerId);
        }

        BossBar bossBar = Bukkit.createBossBar(
                CFG().getString("BossBar.countdown")
                        .replace("{timer}", String.valueOf(time))
                        .replace('&', '§'),
                BarColor.valueOf(CFG().getString("BossBar.Color")),
                BarStyle.valueOf(CFG().getString("BossBar.Style"))
        );
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        playerBossBars.put(playerId, bossBar);
        playerCountdowns.put(playerId, time);
        playerInitialTimes.put(playerId, time);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                UUID playerId = player.getUniqueId();

                // Проверяем, существует ли еще таймер для этого игрока
                if (!activeTimers.containsKey(playerId)) {
                    this.cancel();
                    return;
                }

                Integer currentCountdown = playerCountdowns.get(playerId);
                Integer initialTime = playerInitialTimes.get(playerId);

                // Проверяем, не null ли значение
                if (currentCountdown == null || initialTime == null) {
                    this.cancel();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        activeTimers.remove(playerId);
                        if (playerBossBars.containsKey(playerId)) {
                            playerBossBars.get(playerId).removeAll();
                            playerBossBars.remove(playerId);
                        }
                        playerCountdowns.remove(playerId);
                        playerInitialTimes.remove(playerId);
                    });
                    return;
                }

                if (currentCountdown > 0) {
                    // Обновляем BossBar
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!activeTimers.containsKey(playerId)) {
                            return;
                        }

                        double progress = (double) currentCountdown / initialTime;
                        bossBar.setProgress(progress);
                        bossBar.setTitle(CFG().getString("BossBar.countdown")
                                .replace('&', '§')
                                .replace("{timer}", String.valueOf(currentCountdown)));
                    });

                    playerCountdowns.put(playerId, currentCountdown - 1);

                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!activeTimers.containsKey(playerId)) {
                            return;
                        }

                        bossBar.removeAll();
                        activeTimers.remove(playerId);
                        playerBossBars.remove(playerId);
                        playerCountdowns.remove(playerId);
                        playerInitialTimes.remove(playerId);

                        Bukkit.dispatchCommand(player, command);

                        List<String> actions = CFG().getStringList("actions.end");
                        for (String action : actions) {
                            plugin.getActions().execute(player, action.replace("%time%", String.valueOf(time)));
                        }
                    });

                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);

        activeTimers.put(playerId, task);
    }

    public void cancelTimer(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeTimers.containsKey(playerId)) {
            // Отменяем задачу сразу
            activeTimers.get(playerId).cancel();

            Bukkit.getScheduler().runTask(plugin, () -> {
                // Удаляем из мап
                activeTimers.remove(playerId);

                if (playerBossBars.containsKey(playerId)) {
                    playerBossBars.get(playerId).removeAll();
                    playerBossBars.remove(playerId);
                }

                playerCountdowns.remove(playerId);
                playerInitialTimes.remove(playerId);

                List<String> actions = CFG().getStringList("actions.cancel");
                for (String action : actions) {
                    plugin.getActions().execute(player, action);
                }
            });
        }
    }
}