package me.jetby.cmdTimer;

import lombok.Getter;
import me.jetby.cmdTimer.commands.Reload;
import me.jetby.cmdTimer.listeners.EntityDamageEvent;
import me.jetby.cmdTimer.listeners.PlayerAttackEvent;
import me.jetby.cmdTimer.listeners.PlayerCommandPreprocess;
import me.jetby.cmdTimer.manager.Actions;
import me.jetby.cmdTimer.manager.Timer;
import me.jetby.cmdTimer.utils.Config;
import me.jetby.cmdTimer.utils.Metrics;
import me.jetby.cmdTimer.utils.Region;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {
    private Timer timer;
    private Actions actions;
    private Region region;

    @Override
    public void onEnable() {

        Config cfg = new Config();
        cfg.loadYamlFile(this);

        actions = new Actions();
        timer = new Timer(this);
        region = new Region();

        new Metrics(this, 23883);

        register();
    }

    private void register() {
        registerCommands();
        registerEvents();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerCommandPreprocess(this), this);
        getServer().getPluginManager().registerEvents(new PlayerAttackEvent(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageEvent(this), this);
    }

    private void registerCommands() {
        getCommand("cmdtimer-reload").setExecutor(new Reload(this));
    }
}
