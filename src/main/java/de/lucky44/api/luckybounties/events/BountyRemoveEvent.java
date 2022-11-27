package de.lucky44.api.luckybounties.events;

import de.lucky44.api.luckybounties.util.BountyData;
import org.bukkit.entity.Player;

public class BountyRemoveEvent extends BountiesEvent{
    public final Player remover;
    public final Player target;

    public final BountyData bounty;

    public BountyRemoveEvent(Player remover, Player target, de.lucky44.luckybounties.util.bounty bounty){
        this.remover = remover;
        this.target = target;
        this.bounty = new BountyData(target.getUniqueId(), bounty);
    }
}
