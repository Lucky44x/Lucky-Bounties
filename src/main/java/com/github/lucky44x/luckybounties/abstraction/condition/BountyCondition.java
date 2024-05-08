package com.github.lucky44x.luckybounties.abstraction.condition;

import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.entity.Player;

/**
 * @author Lucky44x
 * BountyCondition interface for handling conditions
 */
public interface BountyCondition {
    boolean isAllowedToSet(Bounty b, Player target, Player setter);
    boolean isAllowedToRemove(Bounty b, Player caller);
    boolean isVisible(Player asked, Player target);
    boolean dropBounties(Player killer, Player killed);
}
