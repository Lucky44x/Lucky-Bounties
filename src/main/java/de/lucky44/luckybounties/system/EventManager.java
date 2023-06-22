package de.lucky44.luckybounties.system;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import de.lucky44.api.luckybounties.events.BountyCollectEvent;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.chat.ChatManager;
import de.lucky44.luckybounties.files.config.COMMANDCONFIG;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EventManager implements Listener {

    @EventHandler
    public void onKill(PlayerDeathEvent e){
        Player killed = e.getEntity();
        Player killer = e.getEntity().getKiller();
        Resident killedResident = TownyAPI.getInstance().getResident(killed);
        Resident killerResident = TownyAPI.getInstance().getResident(killer);


        if(killerResident.hasTown() && killedResident.hasTown()){
            Town killedTown = killedResident.getTownOrNull();
            Town killerTown = killerResident.getTownOrNull();
            if(killerTown.isAlliedWith(killedTown) || killerTown.equals(killedTown)){
                return;
            }
        }

        if(killerResident.hasFriend(killedResident)){
            return;
        }


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

                bounty ecoBounty = LuckyBounties.I.getEcoBounty(killed.getUniqueId());

                if(CONFIG.getBool("bounty-take-eco") && ecoBounty != null){

                    if(CONFIG.getBool("take-message-overrides-death-message")){
                        e.setDeathMessage(LANG.getText("eco-bounty-take-global")
                                .replace("[PLAYERNAME]", killer.getName())
                                .replace("[TARGET]", killed.getName())
                                .replace("[AMOUNT]", LuckyBounties.I.Vault.format(ecoBounty.moneyPayment)));
                    }
                    else{
                        Bukkit.broadcastMessage(LANG.getText("eco-bounty-take-global")
                                .replace("[PLAYERNAME]", killer.getName())
                                .replace("[TARGET]", killed.getName())
                                .replace("[AMOUNT]", LuckyBounties.I.Vault.format(ecoBounty.moneyPayment))
                        );
                    }
                }
                else{

                    if(CONFIG.getBool("take-message-overrides-death-message")){
                        e.setDeathMessage(LANG.getText("bounty-take-global")
                                .replace("[PLAYERNAME]", killer.getName())
                                .replace("[TARGET]", killed.getName()));
                    }
                    else{
                        LuckyBounties.I.chatManager.bountyCollect(killer, killed, bounties.toArray(bounty[]::new));
                    }
                }
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

            LuckyBounties.I.clearBounties(killed.getUniqueId());
            LuckyBounties.I.fetchPlayer(killer.getUniqueId()).onCollect();
        }
        else{
            for(String s : COMMANDCONFIG.getStringList("kill-without-bounty-penalty")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', s
                        .replace("[KILLER]", killer.getName())
                        .replace("[KILLED]", killed.getName())));
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        List<bounty> returns = LuckyBounties.I.fetchBuffer(e.getPlayer().getUniqueId());
        if(!(returns.size() > 0))
            return;

        for(bounty b : returns){
            ItemMeta meta = b.payment.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            String target = dataContainer.get(LuckyBounties.I.dataKey, PersistentDataType.STRING);
            UUID targetID = UUID.fromString(target);
            e.getPlayer().sendMessage(LANG.getText("bounty-expired")
                    .replace("[AMOUNT]", ""+b.payment.getAmount())
                    .replace("[ITEM]", ""+b.payment.getType())
                    .replace("[TARGET]", ""+Bukkit.getOfflinePlayer(targetID).getName()));

            if(e.getPlayer().getInventory().firstEmpty() == -1){
                e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), LuckyBounties.I.cleanBountyItem(b));
            }
            else{
                e.getPlayer().getInventory().addItem(LuckyBounties.I.cleanBountyItem(b));
            }
        }

        LuckyBounties.I.removeBuffer(e.getPlayer().getUniqueId());
    }
}
