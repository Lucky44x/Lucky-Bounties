package com.github.lucky44x.api.luckybounties.events;

import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * The Event which gets called when a bounty is removed from a player
 */
public class BountyRemoveEvent extends BountyEvent{
    /**
     * The Player who removed the bounty
     */
    public final Player remover;
    /**
     * The offlinePlayer from whom the bounty was removed
     */
    public final OfflinePlayer target;
    /**
     * The bounty which is getting removed
     */
    public final Bounty bounty;

    public BountyRemoveEvent(Player remover, Bounty bounty){
        this.bounty = bounty;
        this.target = Bukkit.getOfflinePlayer(bounty.getTargetID());
        this.remover = remover;
    }
}
