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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReturnBufferGUI extends FileGUI {
    @LangConfig.LangData(langKey="[TARGET]", stringMethodNames="getName")
    private final OfflinePlayer target;

    private Bounty[] targetReturnBuffer;

    public ReturnBufferGUI(LuckyBounties instance, Player user, OfflinePlayer target) throws FileNotFoundException {
        super(instance, user, "ReturnBuffer", instance.langFile, instance.getBridge().getGUIExtensions("ReturnBuffer"));
        this.target = target;

        updateReturnBuffer(false);
        finishInit();
    }

    @GUITag("clearBounties")
    public void clearBuffer(){
        ((LuckyBounties)instance).getHandler().clearReturnBuffer(target.getUniqueId());
        updateReturnBuffer(true);
        updateView();
    }

    @GUITag("bountyClicked")
    public void onBountyClicked(InventoryClickEvent e, int index){
        /* NOOP
        if(index >= targetReturnBuffer.length)
            return;

        boolean returnToCaller = false;

        if(user.hasPermission("lb.op")){
            returnToCaller = !e.isRightClick();
        }

        updateReturnBuffer(true);
        updateView();
        */
    }

    //region bountyItems
    @GUITag("bountyItems")
    public ItemStack[] bountyItems(){
        return getSimpleBountyItems();
    }

    @LangConfig.LangData(langKey = "[BOUNTY]")
    public String getCombinedEcoAmount(){
        if(!((LuckyBounties)instance).getIntegrationManager().isIntegrationActive("VAULT"))
            return String.valueOf(((LuckyBounties)instance).getHandler().getEcoAmount(target.getUniqueId()));
        else
            return ((LuckyBounties)instance).getIntegrationManager().getIntegration("VAULT", VaultPluginIntegration.class).format(
                    ((LuckyBounties)instance)
                            .getHandler()
                            .getEcoAmount(target.getUniqueId())
            );
    }

    public ItemStack[] getSimpleBountyItems(){
        ItemStack[] items = new ItemStack[targetReturnBuffer.length];
        for(int i = 0; i < targetReturnBuffer.length; i++){
            items[i] = targetReturnBuffer[i].toItem();
        }
        return items;
    }

    //endregion

    private void updateReturnBuffer(boolean setArray){
        targetReturnBuffer = ((LuckyBounties)instance).getHandler().getReturnBuffer(target.getUniqueId());

        if(setArray)
            ((PagedArray)getComponent("bounties-array")).updateItems(bountyItems());
    }

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

    private ItemStack getPlayerHead(OfflinePlayer target){
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
