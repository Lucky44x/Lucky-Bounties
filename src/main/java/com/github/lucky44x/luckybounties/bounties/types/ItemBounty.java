package com.github.lucky44x.luckybounties.bounties.types;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckyutil.config.LangConfig;
import com.github.lucky44x.luckyutil.plugin.PluginUtilities;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemBounty extends Bounty {

    @Getter
    private final ItemStack reward;

    public ItemBounty(ItemStack reward, Player target, Player setter, LuckyBounties instance) {
        super(target.getUniqueId(), setter.getUniqueId(), instance);
        this.reward = reward;
    }

    public ItemBounty(ItemStack reward, UUID target, UUID setter, long setTime, LuckyBounties instance){
        super(target, setter, setTime, instance);
        this.reward = reward;
    }

    @Override
    public ItemStack toItem() {
        ItemStack GUIItem = reward.clone();
        ItemMeta meta = GUIItem.getItemMeta();
        if(meta == null)
            return GUIItem;

        List<String> lore = new ArrayList<>();
        if(meta.hasLore())
            lore = meta.getLore();

        lore.addAll(instance.langFile.getTextList("bounty-lore", this));
        meta.setLore(lore);

        GUIItem.setItemMeta(meta);
        return GUIItem;
    }

    @Override
    public void returnBounty() {
        Player setter = Bukkit.getServer().getPlayer(setterID);
        if(setter != null){
            if(setter.getInventory().firstEmpty() == -1)
                setter.getWorld().dropItemNaturally(setter.getLocation(), reward);
            else
                setter.getInventory().addItem(reward);

            instance.getHandler().removeBounty(this);
        }
        else
            instance.getHandler().moveBountyToReturn(this);
    }

    @Override
    public void receiveBounty(Player killer) {
        //Target has to be online to be killed
        Player target = Bukkit.getPlayer(targetID);
        if(target == null){
            instance.getLogger().severe("We're in deep shit now... Target Player is not online, but was killed ?");
            return;
        }

        target.getWorld().dropItemNaturally(target.getLocation(), reward);
        instance.getHandler().removeBounty(this);
    }

    @Override
    public void giveReward(Player user) {
        if(user.getInventory().firstEmpty() == -1)
            user.getWorld().dropItemNaturally(user.getLocation(), reward);
        else
            user.getInventory().addItem(reward);

        instance.getHandler().removeBounty(this);
    }

    @LangConfig.LangData(langKey = "[BOUNTY]")
    public String getRewardString(){
        if(reward.getItemMeta().getDisplayName().isEmpty())
            return reward.getType().name().toLowerCase().replaceAll("_", " ");

        return reward.getItemMeta().getDisplayName();
    }

    @LangConfig.LangData(langKey = "[BOUNTY_TRANSLATABLE]")
    public String getRewardStringTranslatable(){
        if(reward.getItemMeta().getDisplayName().isEmpty()){
            return PluginUtilities.getTranslationKey(reward.getType());
        }

        return reward.getItemMeta().getDisplayName();
    }

    @LangConfig.LangData(langKey = "[COUNT]")
    public String getRewardCountString(){
        return String.valueOf(reward.getAmount());
    }
}
