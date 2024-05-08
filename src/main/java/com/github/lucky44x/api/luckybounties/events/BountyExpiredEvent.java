package com.github.lucky44x.api.luckybounties.events;

import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * The event that gets called when a bounty expires
 * @author Nick Balischewski
 */
public class BountyExpiredEvent extends BountyEvent{
    /**
     * The player who is the target of the expired bounty
     */
    public final OfflinePlayer target;
    /**
     * The Bounty which expired
     */
    public final Bounty bounty;

    public BountyExpiredEvent(Bounty bounty){
        this.target = Bukkit.getOfflinePlayer(bounty.getTargetID());
        this.bounty = bounty;
    }
}
