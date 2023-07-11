package com.github.lucky44x.luckybounties.integration.plugins.vanish;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class SuperPremiumVanishIntegration extends VanishIntegration {
    public SuperPremiumVanishIntegration(LuckyBounties instance) {
        super(instance);
    }

    @Override
    public void onEnable() throws IntegrationException {
        if(!Bukkit.getPluginManager().isPluginEnabled("SuperVanish") && !Bukkit.getPluginManager().isPluginEnabled("PremiumVanish"))
            throw new IntegrationException("Neither PremiumVanish nor SuperVanish are installed");
    }

    @Override
    public void onDisable() throws IntegrationException {
        if(!Bukkit.getPluginManager().isPluginEnabled("SuperVanish") && !Bukkit.getPluginManager().isPluginEnabled("PremiumVanish"))
            throw new IntegrationException("Neither PremiumVanish nor SuperVanish are installed");
    }

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) {
        return true;
    }
    @Override
    public boolean isAllowedToRemove(Bounty b, Player caller) {
        return true;
    }

    @Override
    public boolean isVisible(Player asked, Player target){
        if(instance.configFile.isVanishTotalHide()){
            for (MetadataValue meta : target.getMetadata("vanished")) {
                if (meta.asBoolean()) return false;
            }

            return !VanishAPI.isInvisible(target);
        }
        else{
            return VanishAPI.canSee(asked, target);
        }
    }
}
