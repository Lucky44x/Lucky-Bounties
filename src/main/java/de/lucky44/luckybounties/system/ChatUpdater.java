package de.lucky44.luckybounties.system;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.playerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ChatUpdater {
    public static void run() {
        playerData maxBounties = LuckyBounties.mostWorth;

        if(maxBounties != null){
            String message = LuckyBounties.instance.chatUpdateMessage;
            message = message.replace("{player}", maxBounties.playerName);
            message = message.replace("{amount}", ""+maxBounties.worth);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        Bukkit.getScheduler().runTask(LuckyBounties.instance, () ->{
            Bukkit.getScheduler().runTaskLaterAsynchronously(LuckyBounties.instance, ChatUpdater::run, LuckyBounties.instance.chatUpdateDelay);
        });
    }
}
