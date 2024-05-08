package com.github.lucky44x.api.luckybounties.events;

import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.entity.Player;

/**
 * The Event which gets called when a Player sets a bounty on another player
 * @author Nick Balischewski
 */
public class BountySetEvent extends BountyEvent{
    /**
     * The target of the bounty
     */
    public final Player target;
    /**
     * The Setter of the bounty
     */
    public final Player setter;
    private Bounty bounty;

    public BountySetEvent(Player target, Player setter, Bounty bounty){
        this.setter = setter;
        this.target = target;
        this.bounty = bounty;
    }
    /**
     * Set the bounty instance which should be added to the target's bounties
     * @param bounty the Bounty-Object Instance which should be added to the target Player
     */
    public void setBounty(Bounty bounty){
        this.bounty = bounty;
    }

    /**
     * Get the Bounty instance which is about to be added to the target Player
     * @return the Bounty-Instance
     */
    public Bounty getBounty(){
        return bounty;
    }
}
