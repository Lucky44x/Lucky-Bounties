package com.github.lucky44x.luckybounties.integration.extensions;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.LBIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.extension.FilterExtension;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.bounties.types.ItemBounty;
import com.github.lucky44x.luckyutil.config.LangConfig;
import org.bukkit.entity.Player;

/**
 * @author Lucky44x
 * A simple BlackList-Extension for blacklisting items
 */
public class BlacklistExtension extends FilterExtension {
    public BlacklistExtension(LuckyBounties instance) {
        super(instance, "blacklist");
        if(instance.getIntegrationManager().isIntegrationActive("WHLex")){
            instance.getLogger().severe("Can not enable Whitelist and Blacklist at the same time... disabling Whitelist");
            instance.getIntegrationManager().unregisterIntegration("WHLex");
        }
    }

    @LangConfig.LangData(langKey = "[MATERIAL]")
    private String materialNameTMP;

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) {
        if(b instanceof EcoBounty)
            return true;

        if(isInFilter(((ItemBounty)b).getReward())){
            materialNameTMP = ((ItemBounty)b).getReward().getType().name().toLowerCase();
            setter.sendMessage(instance.langFile.getText("item-blacklisted", this));
            materialNameTMP = "";
            return false;
        }

        return true;
    }

    @Override
    public boolean isAllowedToRemove(Bounty b, Player caller) {
        return true;
    }

    @Override
    public boolean dropBounties(Player killer, Player killed) {
        return true;
    }
}