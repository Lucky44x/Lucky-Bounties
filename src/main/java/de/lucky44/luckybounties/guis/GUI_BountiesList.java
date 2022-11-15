package de.lucky44.luckybounties.guis;

import de.lucky44.gui.GUI;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.deprecated.bountyOld;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GUI_BountiesList extends GUI {

    private final Player target;
    private final bounty[] bounties;

    private int page = 0;
    boolean hasNext = false;
    boolean hasLast = false;


    public GUI_BountiesList(Player target, int page){
        this.target = target;
        bounties = LuckyBounties.I.fetchBounties(target.getUniqueId()).toArray(new bounty[0]);
        this.page = page;

        bounty ecoBounty = null;
        int ecoBountyIndex = 0;
        for(int i = 0; i < bounties.length; i++){
            if(bounties[i].moneyPayment > 0){
                ecoBountyIndex = i;
                ecoBounty = bounties[i];
                break;
            }
        }

        if(ecoBounty != null){
            bounty tmp = bounties[0];
            bounties[0] = ecoBounty;
            bounties[ecoBountyIndex] = tmp;
        }

        int wholeLength = bounties.length;
        int newLength = wholeLength - (27 * page);
        if(newLength > 27)
            hasNext = true;
        if(page > 0)
            hasLast = true;
    }

    @Override
    public void onOpen(Player user) {
        setSize(54);
        setName(LANG.getText("player-gui-title").replace("[PLAYERNAME]", target.getName()).replace("[PAGE]", ""+page));
        construct();

        fill(GUIItems.FillerItem());

        set(GUIItems.getPlayerHead(target), 4);

        set(GUIItems.SetItem(), 13);

        if(!user.hasPermission("lb.op")){
            if(!user.hasPermission("lb.set")){
                set(GUIItems.ErrorSlotItem(LANG.getText("missing-set-permission")), 13);
            }
            if(target.hasPermission("lb.exempt")){
                set(GUIItems.ErrorSlotItem(LANG.getText("target-exempt").replace("[PLAYERNAME]", target.getName())), 13);
            }
            if(!CooldownManager.I.isAllowedToSet(target, user)){
                set(GUIItems.ErrorSlotItem(LANG.getText("cooldown-not-done").replace("[TARGET]", target.getName())), 13);
            }
        }
        else{
            set(GUIItems.ClearItem(), 8);
        }

        if(CONFIG.getBool("disable-items")){
            set(GUIItems.ErrorSlotItem(LANG.getText("items-disabled")), 13);
        }

        if(!CONFIG.getBool("allow-self-bounty") && target == user){
            set(GUIItems.ErrorSlotItem(LANG.getText("self-bounty-error")), 13);
        }

        if(hasNext)
            set(GUIItems.NextItem(), 53);
        if(hasLast)
            set(GUIItems.BackItem(), 52);

        int offset = 27 * page;
        for(int slot = 18; slot < 45; slot++){
            if(slot-18 + offset >= bounties.length)
                return;

            set(GUIItems.BountyItem(bounties[slot-18 + offset]), slot);
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(int slot, ItemStack item) {

        //Clear Feather
        if(slot == 8 && user.hasPermission("lb.op")){
            LuckyBounties.I.clearBounties(target.getUniqueId());
            GUI_BountiesList updated = new GUI_BountiesList(target, page);
            updated.open(user);
        }

        //Set Button
        if(slot == 13 && item.getType() == Material.AMETHYST_SHARD){
            GUI_SetBounty setBountyGUI = new GUI_SetBounty(target);
            setBountyGUI.open(user);
        }

        //Bounty Items Clicked
        if(slot > 17 && slot < 45){

            if(!user.hasPermission("lb.remove"))
                return;

            if(item == null)
                return;

            int offset = 27 * page;
            int index = slot-18 + offset;

            if(index >= bounties.length)
                return;

            ItemMeta meta = item.getItemMeta();

            String setterUUID = "_NULL";
            if(meta.getPersistentDataContainer().has(LuckyBounties.I.dataKey, PersistentDataType.STRING)){
                setterUUID = meta.getPersistentDataContainer().get(LuckyBounties.I.dataKey, PersistentDataType.STRING);
            }

            if(!user.hasPermission("lb.op")){
                if(setterUUID == null || setterUUID.equals("_NULL") || !setterUUID.equals(user.getUniqueId().toString()))
                    return;
            }

            if(user.getInventory().firstEmpty() == -1){
                user.sendMessage(LANG.getText("inventory-full"));
                user.getWorld().dropItemNaturally(user.getLocation(), LuckyBounties.I.cleanBountyItem(bounties[index]));
            }
            else{
                user.getInventory().addItem(LuckyBounties.I.cleanBountyItem(bounties[index]));
            }

            LuckyBounties.I.removeBounty(target.getUniqueId(), bounties[index]);
            GUI_BountiesList newPage = new GUI_BountiesList(target, page);
            newPage.open(user);
        }

        //Page Buttons
        if(slot == 53 && hasNext){
            GUI_BountiesList newPage = new GUI_BountiesList(target, page + 1);
            newPage.open(user);
        }
        if(slot == 52 && hasLast){
            GUI_BountiesList newPage = new GUI_BountiesList(target, page - 1);
            newPage.open(user);
        }
    }
}
