package com.github.lucky44x.luckybounties.integration.plugins.vanish;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.ConditionIntegration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class VanishIntegration extends ConditionIntegration {

    public VanishIntegration(LuckyBounties instance) { super(instance); }

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) { return true; }
    @Override
    public boolean isAllowedToRemove(Bounty b, Player caller) { return true; }

    @Override
    public boolean isVisible(Player asked, Player target){
        if(instance.getIntegrationManager().isIntegrationActive("SUVANex"))
            return instance.getIntegrationManager().getIntegration("SUVANex", SuperPremiumVanishIntegration.class).isVisible(asked, target);

        if(instance.configFile.isVanishTotalHide()){
            if(instance.configFile.isVanishMetadataCheck()){
                for (MetadataValue meta : target.getMetadata("vanished")) {
                    if (meta.asBoolean()) return false;
                }
                return true;
            }
        }

        return asked.canSee(target);
    }

    @Override
    public boolean dropBounties(Player killer, Player killed) {
        return true;
    }
}
