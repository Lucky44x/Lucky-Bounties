package com.github.lucky44x.luckybounties.abstraction.integration;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.condition.BountyCondition;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import org.bukkit.entity.Player;

public abstract class ConditionIntegration extends Integration implements BountyCondition {
    public ConditionIntegration(LuckyBounties instance) {
        super(instance);
    }

    public abstract boolean isAllowedToSet(Bounty b, Player target, Player setter);
    public abstract boolean isAllowedToRemove(Bounty b, Player caller);
    public boolean isVisible(Player asked, Player target) { return true; }
    public boolean dropBounties(Player killer, Player kileld){return true;}

    @Override
    public void onEnable() throws IntegrationException {
        instance.getConditionManager().registerCondition(this);
    }

    @Override
    public void onDisable() throws IntegrationException {
        instance.getConditionManager().unregisterCondition(this);
    }
}
