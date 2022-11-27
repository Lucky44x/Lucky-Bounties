package de.lucky44.api.luckybounties.events;

import de.lucky44.api.luckybounties.util.BountyData;
import org.bukkit.entity.Player;

public class BountySetEvent extends BountiesEvent{
    public final Player setter;
    public final Player setOn;

    public final BountyData bounty;

    public BountySetEvent(Player setter, Player setOn, de.lucky44.luckybounties.util.bounty bounty){
        this.setOn = setOn;
        this.setter = setter;
        this.bounty = new BountyData(setOn.getUniqueId(), bounty);
    }
}
