package me.jetby.cmdTimer.manager;

import me.jetby.cmdTimer.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static me.jetby.cmdTimer.utils.Config.CFG;
import static me.jetby.cmdTimer.utils.Parser.color;
import static me.jetby.cmdTimer.utils.Parser.hex;

public class Actions {

    public void execute(Player player, String command) {
        String[] args = command.split(" ");
        String withoutCMD = command.replace(args[0] + " ", "");
        String cancelTeleport = CFG().getString("cancel-command", "/cancelteleport");

        switch (args[0]) {
            case "[MESSAGE]": {
                player.sendMessage(color(withoutCMD));
                break;
            }
            case "[BUTTON]": {
                // Исправляем обработку кнопки
                String text = hex(color(withoutCMD)); // Сначала применяем цвет, затем hex
                TextComponent msg = new TextComponent(text);
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cancelTeleport));
                player.spigot().sendMessage(msg); // Отправляем просто сообщение, не указывая тип
                break;
            }
            case "[PLAYER]": {
                Bukkit.dispatchCommand(player, color(withoutCMD.replace("%player%", player.getName())));
                break;
            }
            case "[CONSOLE]": {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), color(withoutCMD.replace("%player%", player.getName())));
                break;
            }
            case "[ACTIONBAR]": {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(color(withoutCMD.replace("%player%", player.getName()))));
                break;
            }
            case "[SOUND]": {
                float volume = 1.0f;
                float pitch = 1.0f;
                for (String arg : args) {
                    if (arg.startsWith("-volume:")) {
                        volume = Float.parseFloat(arg.replace("-volume:", ""));
                        continue;
                    }
                    if (arg.startsWith("-pitch:")) {
                        pitch = Float.parseFloat(arg.replace("-pitch:", ""));
                        continue;
                    }
                }
                try {
                    Sound sound = Sound.valueOf(args[1]);
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (IllegalArgumentException e) {
                    // Неправильное название звука
                }
                break;
            }
            case "[EFFECT]": {
                int strength = 0;
                int duration = 1;
                for (String arg : args) {
                    if (arg.startsWith("-strength:")) {
                        strength = Integer.parseInt(arg.replace("-strength:", ""));
                        continue;
                    }
                    if (arg.startsWith("-duration:")) {
                        duration = Integer.parseInt(arg.replace("-duration:", ""));
                        continue;
                    }
                }
                try {
                    PotionEffectType effectType = PotionEffectType.getByName(args[1]);
                    if (effectType != null && !player.hasPotionEffect(effectType)) {
                        player.addPotionEffect(new PotionEffect(effectType, duration * 20, strength));
                    }
                } catch (IllegalArgumentException e) {
                    // Неправильное название эффекта
                }
                break;
            }
            case "[TITLE]": {
                String title = "";
                String subTitle = "";
                int fadeIn = 1;
                int stay = 3;
                int fadeOut = 1;

                // Создаем копию для обработки
                String processedCMD = withoutCMD;

                for (String arg : args) {
                    if (arg.startsWith("-fadeIn:")) {
                        fadeIn = Integer.parseInt(arg.replace("-fadeIn:", ""));
                        processedCMD = processedCMD.replace(arg, "");
                        continue;
                    }
                    if (arg.startsWith("-stay:")) {
                        stay = Integer.parseInt(arg.replace("-stay:", ""));
                        processedCMD = processedCMD.replace(arg, "");
                        continue;
                    }
                    if (arg.startsWith("-fadeOut:")) {
                        fadeOut = Integer.parseInt(arg.replace("-fadeOut:", ""));
                        processedCMD = processedCMD.replace(arg, "");
                        continue;
                    }
                }

                String[] message = color(processedCMD).split(";");
                if (message.length >= 1) {
                    title = message[0];
                    if (message.length >= 2) {
                        subTitle = message[1];
                    }
                }
                player.sendTitle(title, subTitle, fadeIn * 20, stay * 20, fadeOut * 20);
                break;
            }
        }
    }
}