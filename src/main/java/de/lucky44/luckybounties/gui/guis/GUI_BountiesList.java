package de.lucky44.luckybounties.gui.guis;

import de.lucky44.api.luckybounties.events.BountyRemoveEvent;
import de.lucky44.luckybounties.gui.core.ChestGUI;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GUI_BountiesList extends ChestGUI {

    private final Player target;
    private final bounty[] bounties;

    private int page = 0;
    boolean hasNext = false;
    boolean hasLast = false;

    private int itemBountySlot = 13;
    private int ecoBountySlot = 13;


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

        itemBountySlot = CONFIG.getBool("vault-integration") ? 12 : 13;
        ecoBountySlot = CONFIG.getBool("disable-items") ? 13 : 14;

        setSize(54);
        setName(LANG.getText("player-gui-title").replace("[PLAYERNAME]", target.getName()).replace("[PAGE]", ""+page));
        construct();

        fill(GUIItems.FillerItem());

        set(GUIItems.getPlayerHead(target), 4);

        if(user.hasPermission("lb.op"))
            set(GUIItems.ClearItem(), 8);

        if(!CONFIG.getBool("disable-items")){
            set(GUIItems.SetItem(), itemBountySlot);
            doSetItemCheck(itemBountySlot);
        }

        if(CONFIG.getBool("vault-integration")){
            set(GUIItems.SetEcoItem(), ecoBountySlot);
            doSetItemCheck(ecoBountySlot);
        }


        if(hasNext)
            set(GUIItems.NextItem(), 53);
        if(hasLast)
            set(GUIItems.BackItem(), 52);

        if(CONFIG.getBool("disable-items") && CONFIG.getBool("vault-integration")){

            if(bounties.length == 0)
                return;

            set(GUIItems.BountyItem(bounties[0]), 31);
            return;
        }

        int offset = 27 * page;
        for(int slot = 18; slot < 45; slot++){
            if(slot-18 + offset >= bounties.length)
                return;

            set(GUIItems.BountyItem(bounties[slot-18 + offset]), slot);
        }
    }

    private void doSetItemCheck(int slot){
        if(!user.hasPermission("lb.op")){
            if(!user.hasPermission("lb.set")){
                set(GUIItems.ErrorSlotItem(LANG.getText("missing-set-permission")), slot);
            }
            if(target.hasPermission("lb.exempt")){
                set(GUIItems.ErrorSlotItem(LANG.getText("target-exempt").replace("[PLAYERNAME]", target.getName())), slot);
            }
            if(!CooldownManager.I.isAllowedToSet(target, user)){
                set(GUIItems.ErrorSlotItem(LANG.getText("cooldown-not-done").replace("[TARGET]", target.getName())), slot);
            }
        }

        if(!CONFIG.getBool("allow-self-bounty") && target == user){
            set(GUIItems.ErrorSlotItem(LANG.getText("self-bounty-error")), slot);
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

        //Set Buttons
        if(slot == itemBountySlot && item.getType() == Material.AMETHYST_SHARD){
            GUI_SetBounty setBounty = new GUI_SetBounty(target);
            setBounty.open(user);
        }
        else if(slot == ecoBountySlot && item.getType() == Material.TOTEM_OF_UNDYING){
            GUI_EcoSetBounty setBounty = new GUI_EcoSetBounty(target);
            close();
            setBounty.open(user);
        }

        //Bounty Items Clicked
        if(slot > 17 && slot < 45){

            if(CONFIG.getBool("disable-items") && CONFIG.getBool("vault-integration")){
                return;
            }

            if(!user.hasPermission("lb.remove"))
                return;

            if(item == null)
                return;

            int offset = 27 * page;
            int index = slot-18 + offset;

            if(index >= bounties.length)
                return;

            //Can't take out money bounties (Maybe later)
            if(bounties[index].moneyPayment > 0)
                return;

            ItemMeta meta = item.getItemMeta();

            String setterUUID = "_NULL";
            if(meta.getPersistentDataContainer().has(LuckyBounties.I.dataKey, PersistentDataType.STRING)){
                setterUUID = meta.getPersistentDataContainer().get(LuckyBounties.I.dataKey, PersistentDataType.STRING);
            }

            if(!user.hasPermission("lb.op")){
                if(setterUUID == null || setterUUID.equals("_NULL") || setterUUID.equals("CONSOLE") || !setterUUID.equals(user.getUniqueId().toString()))
                    return;
            }

            BountyRemoveEvent event = new BountyRemoveEvent(user, target, bounties[index]);
            LuckyBounties.I.callEvent(event);
            if(event.isCancelled())
                return;

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
