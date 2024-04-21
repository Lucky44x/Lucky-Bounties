package com.github.lucky44x.luckybounties.guis;

import com.github.lucky44x.gui.FileGUI;
import com.github.lucky44x.gui.components.PagedArray;
import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.integration.extensions.CooldownExtension;
import com.github.lucky44x.luckybounties.integration.plugins.VaultPluginIntegration;
import com.github.lucky44x.luckybounties.integration.plugins.WorldGuardIntegration;
import com.github.lucky44x.luckyutil.config.LangConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class BountiesListGUI extends FileGUI {
    @LangConfig.LangData(langKey="[TARGET]", stringMethodNames="getName")
    private final Player target;
    @GUITag("economyMode")
    protected int ecoMode = 0;
    @GUITag("opMode")
    protected int opMode = 0;

    private Bounty[] targetBounties;

    public BountiesListGUI(LuckyBounties instance, Player user, Player target) throws FileNotFoundException {
        super(instance, user, "BountiesList", instance.langFile, instance.getBridge().getGUIExtensions("BountiesList"));

        this.target = target;

        if(instance.getIntegrationManager().isEconomyActive())
            ecoMode ++;
        if(!instance.configFile.isItemsEnabled())
            ecoMode++;

        if(user.hasPermission("lb.op"))
            opMode ++;

        updateBounties(false);

        finishInit();
    }

    @GUITag("clearBounties")
    public void clearBounties(){

        if(opMode != 1)
            return;

        ((LuckyBounties)instance).getHandler().clearBounties(target);

        updateBounties(true);
        updateView();
    }

    @GUITag("bountyClicked")
    public void onBountyClicked(InventoryClickEvent e, int index){
        if(index >= targetBounties.length)
            return;

        boolean returnToCaller = false;

        if(user.hasPermission("lb.op")){
            returnToCaller = !e.isRightClick();
        }

        if(!((LuckyBounties)instance).removeBounty(targetBounties[index], user, returnToCaller))
            return;

        updateBounties(true);
        updateView();
    }

    @GUITag("setItemBounty")
    public void setItemBounty(InventoryClickEvent event){

        ItemStack stack = inventory.getItem(event.getSlot());
        if(stack == null)
            return;

        if(stack.hasItemMeta() && stack.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)){
            return;
        }

        try {
            new SetBountyGUI((LuckyBounties) instance, user, target);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GUITag("setEcoBounty")
    public void setEcoBounty(InventoryClickEvent event){

        ItemStack stack = inventory.getItem(event.getSlot());
        if(stack == null)
            return;

        if(stack.hasItemMeta() && stack.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)){
            return;
        }
        try{
            new SetEcoBountyGUI(((LuckyBounties)instance), user, target);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //region bountyItems
    @GUITag("bountyItems")
    public ItemStack[] bountyItems(){
        return ((LuckyBounties)instance).configFile.isEcoBountiesMerged() ? getMergedBountyItems() : getSimpleBountyItems();
    }

    public ItemStack[] getMergedBountyItems(){
        List<ItemStack> items = new ArrayList<>();
        int ecoIndex = -1;
        for(int i = 0; i < targetBounties.length; i++){
            if(targetBounties[i] instanceof EcoBounty){
                if(ecoIndex == -1){
                    items.add(targetBounties[i].toItem());
                    ecoIndex = i;
                }
                continue;
            }

            items.add(targetBounties[i].toItem());
        }

        if(ecoIndex == -1)
            return getSimpleBountyItems();

        if(((LuckyBounties)instance).getHandler().getEcoAmount(target) <= 0){
            items.remove(ecoIndex);
        }
        else{
            ItemMeta meta = items.get(ecoIndex).getItemMeta();
            meta.setDisplayName(((LuckyBounties)instance).langFile.getText("eco-bounty-title", this));
            meta.setLore(List.of(((LuckyBounties)instance).langFile.getText("combined-eco-bounty-lore-line", this)));
            items.get(ecoIndex).setItemMeta(meta);

            ItemStack tmp = items.get(0);
            items.set(0, items.get(ecoIndex));
            items.set(ecoIndex, tmp);
        }
        return items.toArray(ItemStack[]::new);
    }

    @LangConfig.LangData(langKey = "[BOUNTY]")
    public String getCombinedEcoAmount(){
        if(!((LuckyBounties)instance).getIntegrationManager().isEconomyActive())
            return String.valueOf(((LuckyBounties)instance).getHandler().getEcoAmount(target));
        else
            return ((LuckyBounties)instance).getIntegrationManager().getEconomyHandler().format(
                    ((LuckyBounties)instance)
                            .getHandler()
                            .getEcoAmount(target)
            );
    }

    public ItemStack[] getSimpleBountyItems(){
        ItemStack[] items = new ItemStack[targetBounties.length];
        for(int i = 0; i < targetBounties.length; i++){
            items[i] = targetBounties[i].toItem();
        }
        return items;
    }

    //endregion

    private void updateBounties(boolean setArray){
        targetBounties = ((LuckyBounties)instance).getHandler().getBountiesByTarget(target);

        if(setArray)
            ((PagedArray)getComponent("bounties-array")).updateItems(bountyItems());
    }

    //region setButtonItem
    @GUITag("itemSetButton")
    public ItemStack setButtonItem(){
        ItemStack specialButton = getSpecialButtons();
        ItemStack button = new ItemStack(Material.PAPER);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(((LuckyBounties)instance).langFile.getText("button-set-bounty", this));
        button.setItemMeta(meta);

        return specialButton == null ? button : specialButton;
    }

    private ItemStack getSpecialButtons(){
        ItemStack button = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = button.getItemMeta();
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS
        );

        boolean[] WGValues = getWGValues();

        if(target.equals(user) && !((LuckyBounties)instance).configFile.isSelfBountyAllowed()){
            meta.setDisplayName(
                    ((LuckyBounties)instance).langFile.getText("self-set-disabled", this)
            );
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            button.setItemMeta(meta);
            return button;
        }

        if(target.hasPermission("lb.exempt") || WGValues[0]){
            meta.setDisplayName(
                    ((LuckyBounties)instance).langFile.getText("target-exempt", this)
            );
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            button.setItemMeta(meta);
            return button;
        }

        if(!user.hasPermission("lb.set") || WGValues[1]){
            meta.setDisplayName(
                    ((LuckyBounties)instance).langFile.getText("missing-set-permission", this)
            );
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            button.setItemMeta(meta);
            return button;
        }

        if(((LuckyBounties)instance).getIntegrationManager().isIntegrationActive("COLex")){
            CooldownExtension cooldownExtension = ((LuckyBounties)instance).getIntegrationManager().getIntegration("COLex", CooldownExtension.class);
            if(!cooldownExtension.isAllowedToSet(null, target, user)){
               meta.setDisplayName(
                       ((LuckyBounties)instance).langFile.getText("cooldown-not-done", this, cooldownExtension)
               );
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                button.setItemMeta(meta);
                return button;
            }
        }

        return null;
    }

    private boolean[] getWGValues(){
        boolean[] ret = new boolean[2];

        if(((LuckyBounties)instance).getIntegrationManager().isIntegrationActive("WGex")){
            ret[0] = ((LuckyBounties)instance).getIntegrationManager().getIntegration("WGex", WorldGuardIntegration.class).isExempt(target);
            ret[1] = !((LuckyBounties)instance).getIntegrationManager().getIntegration("WGex", WorldGuardIntegration.class).isSetAllowed(user);
        }

        return ret;
    }

    @GUITag("ecoSetButton")
    public ItemStack ecoSetButtonItem(){
        ItemStack specialButton = getSpecialButtons();
        ItemStack button = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(((LuckyBounties)instance).langFile.getText("button-set-eco-bounty", this));
        button.setItemMeta(meta);

        return specialButton == null ? button : specialButton;
    }
    //endregion

    @GUITag("clearButton")
    public ItemStack clearBountyButtonItem(){
        ItemStack button = new ItemStack(Material.FEATHER);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(
                ((LuckyBounties)instance)
                        .langFile.getText("button-clear-bounties", this)
        );
        button.setItemMeta(meta);

        return button;
    }

    @GUITag("targetHead")
    public ItemStack targetHeadItem(){
        return getPlayerHead(target);
    }

    private ItemStack getPlayerHead(Player target){
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        assert meta != null;
        meta.setOwningPlayer(target);
        meta.addItemFlags(
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ATTRIBUTES
        );

        meta.setDisplayName(
                ((LuckyBounties)instance)
                        .langFile.getText("button-player-head", this)
        );

        head.setItemMeta(meta);
        return head;
    }
}
