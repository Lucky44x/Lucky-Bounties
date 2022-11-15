package de.lucky44.luckybounties.timers;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.util.playerData;
import org.bukkit.Bukkit;

public class RankingNotification {
    public static void run(){
        playerData maxBounties = LuckyBounties.mostWorth;

        if(maxBounties != null){
            Bukkit.broadcastMessage(LANG.getText("ranking-message")
                    .replace("[PLAYERNAME]", maxBounties.playerName)
                    .replace("[AMOUNT]", ""+maxBounties.worth));
        }

        if(CONFIG.rankingMessageEnabled){
            Bukkit.getScheduler().runTask(LuckyBounties.I, () ->{
                Bukkit.getScheduler().runTaskLaterAsynchronously(LuckyBounties.I, RankingNotification::run, CONFIG.rankingMessageDelay);
            });
        }
    }
}
