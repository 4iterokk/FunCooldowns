package me.jetby.cmdTimer.listeners;

import me.jetby.cmdTimer.Main;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

import static me.jetby.cmdTimer.utils.Config.CFG;

public class EntityDamageEvent implements Listener {
    private final Main plugin;
    public EntityDamageEvent(Main plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            List<String> DisabledWorlds = CFG().getStringList("Damage-Disabled-Worlds");

            World world = player.getWorld();
            if (!(DisabledWorlds.contains(world.getName()))) {
                if (CFG().getBoolean("CancelOnPlayerDamage")) {
                    if (event.getDamager().getType() == EntityType.PLAYER) {
                        if (event.getDamage()>0) {
                            plugin.getTimer().cancelTimer(player);
                        }
                    }
                }
                if (CFG().getBoolean("CancelOnMobDamage")) {
                    if (event.getDamager().getType()==EntityType.PLAYER) {
                        if (event.getEntity() instanceof Mob) {
                            Mob mob = (Mob) event.getEntity();
                            if (event.getDamage() > 0 && mob.getNoDamageTicks() <= 0) {
                                plugin.getTimer().cancelTimer(player);
                            }
                        }

                    }
                }
            }

        }
    }
}
