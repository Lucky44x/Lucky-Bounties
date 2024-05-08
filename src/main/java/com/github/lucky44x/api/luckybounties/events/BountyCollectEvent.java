package com.github.lucky44x.api.luckybounties.events;

import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.entity.Player;

/**
 * The event that gets sent when a player is killed and his bounties are collected (dropped)
 * @author Nick Balischewski
 */
public class BountyCollectEvent extends BountyEvent{
    /**
     * The player who killed the player with bounties on them
     */
    public final Player killer;
    /**
     * The player who was killed and had at least 1 bounty on them
     */
    public final Player killed;
    private Bounty[] bounties;

    public BountyCollectEvent(Player killer, Player killed, Bounty[] bounties){
        this.killed = killed;
        this.killer = killer;
        this.bounties = bounties;
    }

    /**
     * Set teh bounties which will be received by the killer
     * @param bounties The bounty array which contains all the bounties which should be dropped
     */
    public final void setBounties(Bounty[] bounties){
       this.bounties = bounties;
    }

    /**
     * Get the bounties which where on the target before they were killed
     * @return The Bounties which will be dropped on the position of the killed player
     */
    public final Bounty[] getBounties(){
        return this.bounties == null ? new Bounty[0] : this.bounties;
    }
}
