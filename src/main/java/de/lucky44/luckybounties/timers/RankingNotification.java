package de.lucky44.luckybounties.timers;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.DebugLog;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.util.playerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RankingNotification {
    public static void run(){
        if(CONFIG.rankingMessageEnabled){
            Player highestBounty = null;

            highestBounty = CONFIG.getBool("ranking-message-eco") ? LuckyBounties.I.getHighestEcoBountyOnline() : LuckyBounties.I.getHighestBountyOnline();

            if(highestBounty != null){

                if(CONFIG.getBool("ranking-message-eco")){
                    if(LuckyBounties.I.getEcoBounty(highestBounty.getUniqueId()) != null){
                        float reward = LuckyBounties.I.getEcoBounty(highestBounty.getUniqueId()).moneyPayment;
                        Bukkit.broadcastMessage(LANG.getText("ranking-message-eco")
                                .replace("[PLAYERNAME]", highestBounty.getName())
                                .replace("[AMOUNT]", LuckyBounties.I.Vault.format(reward)));
                    }
                }
                else{
                    int numberOfBounties = LuckyBounties.I.fetchBounties(highestBounty.getUniqueId()).size();
                    Bukkit.broadcastMessage(LANG.getText("ranking-message")
                            .replace("[PLAYERNAME]", highestBounty.getName())
                            .replace("[AMOUNT]", ""+numberOfBounties));
                }
            }

            Bukkit.getScheduler().runTask(LuckyBounties.I, () ->{
                DebugLog.info("[RANKING-MESSAGE] Beginning new wait-cycle: " + CONFIG.rankingMessageDelay);
                Bukkit.getScheduler().runTaskLaterAsynchronously(LuckyBounties.I, RankingNotification::run, CONFIG.rankingMessageDelay);
            });
        }
    }
}
