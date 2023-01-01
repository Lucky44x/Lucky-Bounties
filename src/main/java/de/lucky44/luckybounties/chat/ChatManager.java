package de.lucky44.luckybounties.chat;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.util.bounty;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.*;

public abstract class ChatManager {
    public abstract String getChatItem(ItemStack originalItem);
    public void bountySet(Player target, Player setter, ItemStack item){
        BaseComponent[] hoverEventComponents = new BaseComponent[]{
                new TextComponent(getChatItem(item))
        };
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);

        String fullMessage = LANG.getText("bounty-set-global")
                .replace("[PLAYERNAME]", setter.getName())
                .replace("[TARGET]", target.getName())
                .replace("[AMOUNT]", ""+item.getAmount());

        String[] parts = fullMessage.split("\\[ITEM]");

        TextComponent eventComponent = new TextComponent(item.getItemMeta().getDisplayName().isBlank() ? ChatColor.getLastColors(parts[0]) + item.getType().name().replace("_"," ") : ChatColor.getLastColors(parts[0]) + item.getItemMeta().getDisplayName());
        eventComponent.setHoverEvent(event);

        BaseComponent[] message = new BaseComponent[]{
                new TextComponent(parts[0]),
                eventComponent,
                new TextComponent(parts[1])
        };

        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            p.spigot().sendMessage(message);
        }
    }

    public void bountyCollect(Player killer, Player killed, bounty[] bounties){
        BaseComponent[] hoverEventComponents = new BaseComponent[bounties.length];
        for(int i = 0; i < hoverEventComponents.length; i++){
            if(bounties[i].payment == null){
                hoverEventComponents[i] = new TextComponent(LuckyBounties.I.Vault.format(bounties[i].moneyPayment));
                continue;
            }

            hoverEventComponents[i] = new TextComponent(bounties[i].payment.getItemMeta().getDisplayName());
        }

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventComponents);

        String fullMessage = LANG.getText("bounty-take-global")
                .replace("[PLAYERNAME]", killer.getName())
                .replace("[TARGET]", killed.getName());

        String[] parts = fullMessage.split("\\[BOUNTIES]");
        String part1 = parts[0];
        String bountiesText = parts[1].split("}")[0];
        String rest = parts[1].split("}")[1];

        TextComponent eventComponent = new TextComponent(bountiesText);
        eventComponent.setHoverEvent(hoverEvent);

        BaseComponent[] message = new BaseComponent[]{
                new TextComponent(part1),
                eventComponent,
                new TextComponent(rest)
        };

        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            p.spigot().sendMessage(message);
        }
    }
}
