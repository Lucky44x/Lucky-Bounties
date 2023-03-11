package de.lucky44.luckybounties.gui.guis;

import de.lucky44.api.luckybounties.events.BountySetEvent;
import de.lucky44.luckybounties.files.DebugLog;
import de.lucky44.luckybounties.gui.core.ChestGUI;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.util.bounty;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUI_SetBounty extends ChestGUI {

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

        if(target == null || Bukkit.getServer().getPlayer(target.getUniqueId()) == null){
            close();
            DebugLog.info("[GUI] => ERROR Forcing Close Chest-GUI of type GUI_SetBounty for " + user.getName() + " because target is no longer online");
            user.sendMessage(LANG.getText("player-not-found").replace("[PLAYERNAME]", "Target"));
            return;
        }

        if(slot == 8){

            if(inv.getItem(4) == null){
                user.sendMessage(LANG.getText("no-bounty-item"));
                close();
            }
            else{
                ItemStack bountyPayment = v.getItem(4);
                bounty b = new bounty(bountyPayment);

                switch (LuckyBounties.I.isBountyValid(b)) {
                    case (1) -> {
                        user.sendMessage(LANG.getText("blacklist-error").replace("[ITEM]", b.payment.getType().name()));
                        user.getInventory().addItem(bountyPayment);
                        close();
                        return;
                    }
                    case (2) -> {
                        user.sendMessage(LANG.getText("whitelist-error").replace("[ITEM]", b.payment.getType().name()));
                        user.getInventory().addItem(bountyPayment);
                        close();
                        return;
                    }
                }

                BountySetEvent event = new BountySetEvent(user, target, b);
                LuckyBounties.I.callEvent(event);
                if(event.isCancelled()){
                    user.getInventory().addItem(bountyPayment);
                }
                else{
                    LuckyBounties.I.addBounty(target.getUniqueId(), b, user.getUniqueId());
                    CooldownManager.I.setBounty(target, user);
                }

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
