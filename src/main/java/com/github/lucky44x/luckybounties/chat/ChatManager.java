package com.github.lucky44x.luckybounties.chat;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.bounties.types.ItemBounty;
import com.github.lucky44x.luckyutil.color.ColorUtilities;
import com.github.lucky44x.luckyutil.config.LangConfig;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import java.util.List;

public class ChatManager {
    private final LuckyBounties instance;
    private final com.github.lucky44x.luckyutil.chat.ChatManager chatUtils;

    public ChatManager(LuckyBounties instance){
        this.instance = instance;
        this.chatUtils = new com.github.lucky44x.luckyutil.chat.ChatManager(instance);
    }

    //Had to partly ditch the auto-lang here, because auto-lang cannot handle special tags like {[<tag>]<text>}

    @LangConfig.LangData(langKey = "TRANSLATE", isPostProcessor = true)
    private BaseComponent[] translate(String textIn){
        if(!textIn.contains("item.minecraft.") && !textIn.contains("block.minecraft."))
            return TextComponent.fromLegacyText(textIn);

        return new BaseComponent[]{new TranslatableComponent(textIn)};
    }

    //region setMessage
    public void sendSetMessage(Bounty b){
        boolean isGlobal = instance.configFile.isGlobalSetMessage();
        recentSetBounty = b;
        if(isGlobal)
            sendGlobalSetMessage(b);
        else
            sendLocalSetMessage(b);
    }

    private void sendLocalSetMessage(Bounty b){
        if(instance.getServer().getPlayer(b.getSetterID()) == null)
            return;

        BaseComponent[] message = instance.langFile.getPostProcessedText("bounty-set-local", b, this);
        instance.getServer().getPlayer(b.getSetterID()).spigot().sendMessage(message);
    }

    private void sendGlobalSetMessage(Bounty b){
        BaseComponent[] message = instance.langFile.getPostProcessedText("bounty-set-global", b, this);

        for(Player p : instance.getServer().getOnlinePlayers()){
            p.spigot().sendMessage(message);
        }
    }

    Bounty recentSetBounty;
    @LangConfig.LangData(langKey = "HOVER_SET", isPostProcessor = true)
    private BaseComponent[] setHoverProcessor(BaseComponent[] compIn){
        if(recentSetBounty == null)
            return compIn;

        if(recentSetBounty instanceof EcoBounty)
            return compIn;

        HoverEvent hoverEvent = chatUtils.generateItemHoverEvent(((ItemBounty)recentSetBounty).getReward());
        for(BaseComponent c : compIn){
            c.setHoverEvent(hoverEvent);
        }

        return compIn;
    }
    //endregion setMessage

    //region takeMessage
    public void sendTakeMessage(Player killer, Player target, Bounty[] bounties){
        boolean isGlobal = instance.configFile.isGlobalTakeMessage();
        recentTakeBounties = bounties;
        if(isGlobal)
            sendGlobalTakeMessage(killer, target, bounties);
        else
            sendLocalTakeMessage(killer, target, bounties);
    }

    private  void sendLocalTakeMessage(Player killer, Player target, Bounty[] bounties){
        BaseComponent[] components = instance.langFile.getPostProcessedText("bounty-take-local", new takeLangCarrier(target.getName(), killer.getName()), this);
        killer.spigot().sendMessage(components);
    }

    private  void sendGlobalTakeMessage(Player killer, Player target, Bounty[] bounties){
        BaseComponent[] components = instance.langFile.getPostProcessedText("bounty-take-global", new takeLangCarrier(target.getName(), killer.getName()), this);

        for(Player p : instance.getServer().getOnlinePlayers()){
            p.spigot().sendMessage(components);
        }
    }

    Bounty[] recentTakeBounties;
    @LangConfig.LangData(langKey = "HOVER_TAKE", isPostProcessor = true)
    private BaseComponent[] takeHoverProcessor(BaseComponent[] compIn){
        StringBuilder builder = new StringBuilder();
        if(recentTakeBounties == null)
            return compIn;

        List<String> hoverFormat = instance.langFile.getRawTextList("bounty-take-hover-format");
        for(int i = 0; i < hoverFormat.size(); i++){
            if(i > recentTakeBounties.length - 1)
                break;

            builder.append(ColorUtilities.translateColors(instance.langFile.translateKeys(hoverFormat.get(i), recentTakeBounties[i]))).append("\n");
        }
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(builder.toString()));

        for(BaseComponent c : compIn){
            c.setHoverEvent(hoverEvent);
        }

        return compIn;
    }

    private record takeLangCarrier(
            @LangConfig.LangData(langKey = "[TARGET]")
            String targetName,
            @LangConfig.LangData(langKey = "[KILLER]")
            String killerName){}
    //endregion

    public void sendLangMessage(String key, Player target, Object... callers){
        target.spigot().sendMessage(instance.langFile.getPostProcessedText(key, callers));
    }
}
