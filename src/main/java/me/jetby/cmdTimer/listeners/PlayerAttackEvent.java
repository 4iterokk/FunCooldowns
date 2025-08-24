package me.jetby.cmdTimer.listeners;

import me.jetby.cmdTimer.Main;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

import static me.jetby.cmdTimer.utils.Config.CFG;

public class PlayerAttackEvent implements Listener {

    private final Main plugin;
    public PlayerAttackEvent(Main plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            List<String> DisabledWorlds = CFG().getStringList("Damage-Disabled-Worlds");

            World world = player.getWorld();
            if (!(DisabledWorlds.contains(world.getName()))) {
                if (CFG().getBoolean("CancelOnPlayerDamage")) {
                    if (event.getEntity().getType() == EntityType.PLAYER) {
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
