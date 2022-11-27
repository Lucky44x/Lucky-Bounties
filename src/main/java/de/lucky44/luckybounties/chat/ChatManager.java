package de.lucky44.luckybounties.chat;

import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.*;

public class ChatManager {
    public static ChatManager I;

    public ChatManager(){
        I = this;
    }

    /*
    public void bountySet(Player target, Player setter, ItemStack item){

        Item chatItem = new Item();
        TextComponent itemText = new TextComponent(item.getType().name()).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, item));

        if(CONFIG.getBool("bounty-set-global")){
            Bukkit.getServer().broadcastMessage(LANG.getText("bounty-set-global")
                    .replace("[PLAYERNAME]", setter.getName())
                    .replace("[TARGET]", target.getName())
                    .replace("[AMOUNT]", ""+ item.getAmount())
                    .replace("[ITEM]", ""+ item.getType().name()));
        }
        else{
            setter.sendMessage(LANG.getText("bounty-set")
                    .replace("[TARGET]", target.getName())
                    .replace("[AMOUNT]", ""+item.getAmount())
                    .replace("[ITEM]", ""+item.getType().name()));
        }
    }
    */
}
