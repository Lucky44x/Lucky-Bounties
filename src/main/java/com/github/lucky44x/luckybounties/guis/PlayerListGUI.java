package com.github.lucky44x.luckybounties.guis;

import com.github.lucky44x.gui.FileGUI;
import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckyutil.config.LangConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.FileNotFoundException;

/**
 * @author Lucky44x
 * FileGUI for the PlayerList
 */
public class PlayerListGUI extends FileGUI {

    private final Player[] players;

    //TEMP
    @LangConfig.LangData(langKey = "[TARGET]", stringMethodNames = "getName")
    private Player currentPlayer;

    public PlayerListGUI(LuckyBounties instance, Player user) throws FileNotFoundException {
        super(instance, user, "PlayerList", instance.langFile, instance.getBridge().getGUIExtensions("PlayerList"));
        currentPlayer = user;
        players = instance.getOnlinePlayers(user);
        finishInit();
    }

    @GUITag("playerHeads")
    public ItemStack[] getPlayerItems(){
        ItemStack[] items = new ItemStack[players.length];
        for(int i = 0; i < players.length; i++){
            items[i] = getPlayerHead(players[i]);
        }

        return items;
    }

    @GUITag("headClicked")
    public void onHeadClicked(InventoryClickEvent e, int index){

        if(index >= players.length)
            return;

        try{
            new BountiesListGUI((LuckyBounties) instance, user, players[index]);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ItemStack getPlayerHead(Player target){
        currentPlayer = target;
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        assert meta != null;
        meta.setOwningPlayer(target);
        meta.addItemFlags(
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ATTRIBUTES
        );

        meta.setDisplayName(
                ((LuckyBounties)instance).langFile
                        .getText("button-player-head",this)
        );

        head.setItemMeta(meta);
        return head;
    }
}
