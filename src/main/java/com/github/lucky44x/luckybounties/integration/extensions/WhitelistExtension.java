package com.github.lucky44x.luckybounties.integration.extensions;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.extension.FilterExtension;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.bounties.types.ItemBounty;
import com.github.lucky44x.luckyutil.config.LangConfig;
import org.bukkit.entity.Player;

/**
 * @author Lucky44x
 * a simple Whitelist extension for whitelisting items
 */
public class WhitelistExtension extends FilterExtension {
    public WhitelistExtension(LuckyBounties instance) {
        super(instance, "whitelist");
        if(instance.getIntegrationManager().isIntegrationActive("BLLex")){
            instance.getLogger().severe("Can not enable Whitelist and Blacklist at the same time... disabling Blacklist");
            instance.getIntegrationManager().unregisterIntegration("BLLex");
        }
    }

    @LangConfig.LangData(langKey = "[MATERIAL]")
    private String materialNameTMP;

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) {
        if(b instanceof EcoBounty)
            return true;

        if(!isInFilter(((ItemBounty)b).getReward())){
            materialNameTMP = ((ItemBounty)b).getReward().getType().name().toLowerCase();
            setter.sendMessage(instance.langFile.getText("item-not-whitelisted", this));
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
