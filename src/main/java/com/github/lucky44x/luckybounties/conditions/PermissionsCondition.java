package com.github.lucky44x.luckybounties.conditions;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.condition.BountyCondition;
import org.bukkit.entity.Player;

/**
 * @author Lucky44x
 * simple implementation of a permission based BountyCondition, in order to block certain actions, or allow them
 */
public class PermissionsCondition implements BountyCondition {

    private final LuckyBounties instance;

    public PermissionsCondition(LuckyBounties instance){
        this.instance = instance;
    }

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) {
        if(target.equals(setter) && !instance.configFile.isSelfBountyAllowed()){
            setter.sendMessage(instance.langFile.getText("self-set-disabled", b));
            return false;
        }

        if(!setter.hasPermission("lb.set")){
            setter.sendMessage(instance.langFile.getText("missing-set-permission", b));
            return false;
        }

        if(target.hasPermission("lb.exempt")){
            setter.sendMessage(instance.langFile.getText("target-exempt", b));
            return false;
        }

        return true;
    }

    @Override
    public boolean isAllowedToRemove(Bounty b, Player caller) {
        if(caller.hasPermission("lb.op"))
            return true;

        if(instance.configFile.isSettersAllowedToRemove()){
            if(b.getSetterID().equals(caller.getUniqueId()))
                return true;
        }

        return false;
    }

    @Override
    public boolean isVisible(Player asked, Player target) {
        return true;
    }

    @Override
    public boolean dropBounties(Player killer, Player killed) {
        if(!killer.hasPermission("lb.kill"))
            return false;
        if(!killed.hasPermission("lb.drop"))
            return false;

        return true;
    }
}
