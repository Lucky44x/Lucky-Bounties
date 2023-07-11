package com.github.lucky44x.luckybounties.integration.plugins;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.ConditionPluginIntegration;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

/**
 * Courtesy of ItsJules
 * TownyIntegration - original code by ItsJules, migrated to LuckyBounties 3.0 by Nick Balischewski
 * @author ItsJules
 * @author Nick Balischewski
 */
public class TownyIntegration extends ConditionPluginIntegration {
    public TownyIntegration(LuckyBounties instance) {
        super(instance, "Towny");
    }

    @Override
    public boolean dropBounties(Player killer, Player killed){
        Resident killerResident = TownyAPI.getInstance().getResident(killer);
        Resident killedResident = TownyAPI.getInstance().getResident(killed);

        if(killerResident == null || killedResident == null)
            return true;

        if(instance.configFile.isTownyFriendsKillIgnored()){
            if(killerResident.hasFriend(killedResident))
                return false;
        }

        if(!killerResident.hasTown() || !killedResident.hasTown())
            return true;

        Town killerTown = killerResident.getTownOrNull();
        Town killedTown = killedResident.getTownOrNull();
        if(killerTown == null || killedTown == null)
            return true;

        if(instance.configFile.isTownySameTownKillIgnored()){
            if(killerTown.equals(killedTown))
                return false;
        }

        if(instance.configFile.isTownyAllyIgnored()){
            return !killerTown.isAlliedWith(killedTown);
        }

        return true;
    }

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter){
        Resident targetResident = TownyAPI.getInstance().getResident(target);
        Resident setterResident = TownyAPI.getInstance().getResident(setter);

        if(setterResident == null || targetResident == null)
            return true;

        if(instance.configFile.isTownyFriendsSetAllowed()){
            if(setterResident.hasFriend(targetResident)){
                instance.getChatManager().sendLangMessage("towny-friends", setter, b);
                return false;
            }
        }

        if(!setterResident.hasTown() || !targetResident.hasTown())
            return true;

        Town setterTown = setterResident.getTownOrNull();
        Town targetTown = targetResident.getTownOrNull();
        if(setterTown == null || targetTown == null)
            return true;

        if(instance.configFile.isTownySameTownSetAllowed()){
            if(targetTown.equals(setterTown)){
                instance.getChatManager().sendLangMessage("towny-same-town", setter, b);
                return false;
            }
        }

        if(instance.configFile.isTownyAllySetAllowed()){
            if(setterTown.isAlliedWith(targetTown)){
                instance.getChatManager().sendLangMessage("towny-ally", setter, b);
                return false;
            }
        }

        return true;
    }
}
