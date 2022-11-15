package de.lucky44.luckybounties.guis;

import de.lucky44.gui.GUI;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.deprecated.bountyOld;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUI_SetBounty extends GUI {

    private final Player target;

    public GUI_SetBounty(Player target){
        this.target = target;
    }

    @Override
    public void onOpen(Player user) {
        //Bukkit.getLogger().info("Opening set menu");
        setName(LANG.getText("set-gui-title").replace("[PLAYERNAME]", target.getName()));
        setSize(9);
        construct(InventoryType.DISPENSER);
        fill(GUIItems.FillerItem());

        set(null, 4);
        movable(4);
        set(GUIItems.ConfirmItem(), 8);
        set(GUIItems.CancelItem(), 6);
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(int slot, ItemStack item) {

        if(slot == 8){
            if(inv.getItem(4) == null){
                user.sendMessage(LANG.getText("no-bounty-item"));
                close();
            }
            else{
                ItemStack bountyPayment = v.getItem(4);
                LuckyBounties.I.addBounty(target.getUniqueId(), new bounty(bountyPayment), user.getUniqueId());

                if(CONFIG.getBool("bounty-set-global")){
                    Bukkit.getServer().broadcastMessage(LANG.getText("bounty-set-global")
                            .replace("[PLAYERNAME]", user.getName())
                            .replace("[TARGET]", target.getName())
                            .replace("[AMOUNT]", ""+v.getItem(4).getAmount())
                            .replace("[ITEM]", ""+v.getItem(4).getType().name()));
                }
                else{
                    user.sendMessage(LANG.getText("bounty-set")
                            .replace("[TARGET]", target.getName())
                            .replace("[AMOUNT]", ""+v.getItem(4).getAmount())
                            .replace("[ITEM]", ""+v.getItem(4).getType().name()));
                }

                CooldownManager.I.setBounty(target, user);

                GUI_BountiesList bountiesList = new GUI_BountiesList(target, 0);
                bountiesList.open(user);
            }
        }
        else if(slot  == 6){
            if(inv.getItem(4) != null){
                user.getInventory().addItem(inv.getItem(4));
            }

            GUI_BountiesList bountiesList = new GUI_BountiesList(target, 0);
            bountiesList.open(user);
        }

    }
}
