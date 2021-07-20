package de.lucky44.luckybounties.system;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class eventManager implements Listener {

    @EventHandler
    public static void OnSlotClick(InventoryClickEvent e){

        Inventory I = e.getClickedInventory();
        Player p = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        int clickedSlot = e.getSlot();

        if(clickedItem == null || I == null || (I.getType() != InventoryType.CHEST && I.getType() != InventoryType.DISPENSER)){
            return;
        }

        String invName = ChatColor.stripColor(e.getView().getTitle()).toLowerCase();

        if(!(invName.equals("bounties") || invName.contains("'"))){
            return;
        }
        else if(invName.contains("'")){
            if(!invName.split("'")[1].equals("s bounties") && !invName.split("'")[1].equals("s head")){
                return;
            }
        }

        if(invName.equals("bounties")){

            //Check if head was clicked
            if(clickedItem.getType().equals(Material.PLAYER_HEAD)){
                SkullMeta sKM = (SkullMeta) clickedItem.getItemMeta();

                assert sKM != null;
                String UUID = sKM.getOwningPlayer().getUniqueId().toString();

                guiManager.showSpecificMenu(p, Bukkit.getPlayer(java.util.UUID.fromString(UUID)));
            }

            e.setResult(Event.Result.DENY);
        }
        else if(invName.split("'")[1].equals("s bounties")){

            //Check if set bounty was clicked
            if(clickedItem.getType() == Material.AMETHYST_SHARD && clickedSlot == 13){
                SkullMeta sKM = (SkullMeta) I.getItem(4).getItemMeta();

                assert sKM != null;
                String UUID = sKM.getOwningPlayer().getUniqueId().toString();

                guiManager.showBountySetMenu(p,Bukkit.getPlayer(java.util.UUID.fromString(UUID)));
            }
            else if(clickedItem.getType() == Material.FEATHER && clickedSlot == 8 && p.isOp()){

                //Clear bounties
                SkullMeta sKM = (SkullMeta) I.getItem(4).getItemMeta();

                assert sKM != null;
                String UUID = sKM.getOwningPlayer().getUniqueId().toString();

                LuckyBounties.clearBounties(UUID);

                e.setResult(Event.Result.DENY);

                guiManager.showSpecificMenu(p,Bukkit.getPlayer(java.util.UUID.fromString(UUID)));
                return;
            }

            boolean allow = false;

            if(clickedSlot > 17 && p.isOp()){
                allow = true;

                bounty r = null;

                for(bounty b : LuckyBounties.bounties){
                    if(b.payment.converted == clickedItem){
                        r = b;
                    }
                }

                if(r != null){
                    LuckyBounties.bounties.remove(r);
                }
            }

            if(!allow){
                e.setResult(Event.Result.DENY);
            }
        }
        else if(invName.split("'")[1].equals("s head")){

            if(clickedSlot != 4){

                SkullMeta sKM = (SkullMeta) I.getItem(1).getItemMeta();

                assert sKM != null;
                String UUID = sKM.getOwningPlayer().getUniqueId().toString();

                //Cancel bounty set
                if(clickedSlot == 6){
                    guiManager.cancelBounty(p, Bukkit.getPlayer(java.util.UUID.fromString(UUID)));
                }
                else if(clickedSlot == 8){ //Confirm bounty set
                    guiManager.confirmBounty(p, Bukkit.getPlayer(java.util.UUID.fromString(UUID)));
                }

                e.setResult(Event.Result.DENY);
            }
        }
    }

    @EventHandler
    public static void onKill(PlayerDeathEvent e){
        Player killed = e.getEntity();
        Entity killer = e.getEntity().getKiller();

        //Check if Player was killed
        if(killer instanceof Player){
            Player killerP = (Player)killer;

            List<bounty> bounties = LuckyBounties.getBounties(killed.getUniqueId().toString());

            if(bounties.size() == 1){
                e.setDeathMessage(killerP.getDisplayName() + " has taken " + killed.getDisplayName() + "'s bounty");
            }
            else if(bounties.size() > 1){
                e.setDeathMessage(killerP.getDisplayName() + " has taken " + killed.getDisplayName() + "'s bounties");
            }

            //Drop the bounties of killed player
            for(bounty b : bounties){
                Bukkit.getWorld("world").dropItem(killed.getLocation(),b.payment.converted);
            }

            //Clear the bounties of killed player
            LuckyBounties.clearBounties(killed.getUniqueId().toString());
        }
    }
}
