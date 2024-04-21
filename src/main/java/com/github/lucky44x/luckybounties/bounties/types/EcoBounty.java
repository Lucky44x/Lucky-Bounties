package com.github.lucky44x.luckybounties.bounties.types;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckyutil.config.LangConfig;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class EcoBounty extends Bounty {

    @Getter
    private final double reward;
    @LangConfig.LangData(langKey = "[COUNT]")
    private final String langCount = "";

    public EcoBounty(double reward, Player target, Player setter, LuckyBounties instance) {
        super(target.getUniqueId(), setter.getUniqueId(), instance);
        this.reward = reward;
    }

    public EcoBounty(double reward, UUID target, UUID setter, long setTime, LuckyBounties instance) {
        super(target, setter, setTime, instance);
        this.reward = reward;
    }

    @Override
    public ItemStack toItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(instance.langFile.getText("eco-bounty-title", this));
        meta.setLore(
                instance.langFile.getTextList("bounty-lore", this)
        );

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void returnBounty() {
        if(setter == null)
            return;

        if(!instance.getIntegrationManager().isEconomyActive())
            return;

        instance.getIntegrationManager().getEconomyHandler().add(setter, reward);
        instance.getHandler().removeBounty(this);
    }

    @Override
    public void receiveBounty(Player killer) {
        if(!instance.getIntegrationManager().isEconomyActive())
            return;

        instance.getIntegrationManager().getEconomyHandler().add(killer, reward);
        instance.getHandler().removeBounty(this);
    }

    @Override
    public void giveReward(Player user) {
        if(!instance.getIntegrationManager().isEconomyActive())
            return;

        instance.getIntegrationManager().getEconomyHandler().add(user, reward);
        instance.getHandler().removeBounty(this);
    }

    @LangConfig.LangData(langKey = "[BOUNTY]")
    public String getRewardString(){
        return ChatColor.stripColor(instance.getIntegrationManager().isEconomyActive() ?
                instance.getIntegrationManager()
                        .getEconomyHandler()
                        .format(reward)
                : String.valueOf(reward));
    }

    @LangConfig.LangData(langKey = "[BOUNTY_TRANSLATABLE]")
    public String getRewardStringTranslatable(){
        return ChatColor.stripColor(instance.getIntegrationManager().isEconomyActive() ?
                instance.getIntegrationManager()
                        .getEconomyHandler()
                        .format(reward)
                : String.valueOf(reward));
    }
}
