package de.lucky44.luckybounties.system;

import de.lucky44.api.luckybounties.events.BountyCollectEvent;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class EventManager implements Listener {

    @EventHandler
    public void onKill(PlayerDeathEvent e){
        Player killed = e.getEntity();
        Player killer = e.getEntity().getKiller();

        if(killer == null || killer == killed)
            return;

        if(!killed.hasPermission("lb.drop"))
            return;

        List<bounty> bounties = LuckyBounties.I.fetchBounties(killed.getUniqueId());

        if(bounties.size() > 0){

            BountyCollectEvent event = new BountyCollectEvent(killer, killed, bounties.toArray(bounty[]::new));
            LuckyBounties.I.callEvent(event);
            if(event.isCancelled())
                return;

            if(CONFIG.getBool("bounty-take-global")){
                e.setDeathMessage(LANG.getText("bounty-take-global")
                        .replace("[PLAYERNAME]", killer.getName())
                        .replace("[TARGET]", killed.getName())
                );
            }
            else{
                killer.sendMessage(LANG.getText("bounty-take").replace("[TARGET]", killed.getName()));
            }

            for(bounty b : bounties){

                if(b.moneyPayment > 0){
                    LuckyBounties.I.Vault.add(killer, b.moneyPayment);
                    continue;
                }

                killer.getWorld().dropItem(killed.getLocation(), LuckyBounties.I.cleanBountyItem(b));
            }

            LuckyBounties.I.callEvent(new BountyCollectEvent(killer, killed, bounties.toArray(bounty[]::new)));
            LuckyBounties.I.clearBounties(killed.getUniqueId());
            LuckyBounties.I.fetchPlayer(killer.getUniqueId()).onCollect();

        }
        else{
            if(CONFIG.getString("kill-without-bounty-penalty").isEmpty())
                return;

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', CONFIG.getString("kill-without-bounty-penalty")
                    .replace("[KILLER]", killer.getName())
                    .replace("[KILLED]", killed.getName())));
        }
    }
}
