package me.jetby.cmdTimer.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Config {

    private static FileConfiguration config;
    private static File file;

    public void loadYamlFile(Plugin plugin) {
        file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("config.yml", true);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration CFG() {
        return config;
    }

    public void reloadCfg(Plugin plugin) {
        if(!file.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("config.yml", true);
        }
        try {
            config.load(file);
            Bukkit.getConsoleSender().sendMessage("Конфигурация перезагружена!");
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage("Не удалось перезагрузить конфигурацию!");
        }
    }
}
