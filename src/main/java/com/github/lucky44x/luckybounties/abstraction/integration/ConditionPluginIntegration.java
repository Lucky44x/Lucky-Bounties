package com.github.lucky44x.luckybounties.abstraction.integration;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.condition.BountyCondition;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import org.bukkit.entity.Player;

public abstract class ConditionPluginIntegration extends PluginIntegration implements BountyCondition {
    public ConditionPluginIntegration(LuckyBounties instance, String pluginName) { super(instance, pluginName); }
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) { return true; }
    public boolean isAllowedToRemove(Bounty b, Player caller) { return true; }
    public boolean isVisible(Player asked, Player target) { return true; }
    public boolean dropBounties(Player killer, Player killed) { return true; }

    @Override
    public void onEnable() throws IntegrationException {
        super.onEnable();

        instance.getConditionManager().registerCondition(this);
    }

    @Override
    public void onDisable() throws IntegrationException {
        super.onDisable();

        instance.getConditionManager().unregisterCondition(this);
    }
}
