package de.lucky44.luckybounties.chat.Versions;

import de.lucky44.luckybounties.chat.ChatManager;
import de.lucky44.luckybounties.files.lang.LANG;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChatManager_Normal extends ChatManager {

    @Override
    public String getChatItem(ItemStack originalItem) {
        return null;
    }

    @Override
    public void bountySet(Player target, Player setter, ItemStack item){
        String fullMessage = LANG.getText("bounty-set-global")
                .replace("[PLAYERNAME]", setter == null ? LANG.getText("console-setter-name") : setter.getName())
                .replace("[TARGET]", target.getName())
                .replace("[AMOUNT]", ""+item.getAmount())
                .replace("[ITEM]", ""+item.getType().name());

        Bukkit.getServer().broadcastMessage(fullMessage);
    }
}
