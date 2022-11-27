package de.lucky44.api.luckybounties.events;

import de.lucky44.api.luckybounties.util.EcoBountyData;
import org.bukkit.entity.Player;

public class EcoBountySetEvent extends BountiesEvent{
    public final Player setter;
    public final Player setOn;

    public final EcoBountyData bounty;

    public EcoBountySetEvent(Player setter, Player setOn, float payment){
        this.setOn = setOn;
        this.setter = setter;
        bounty = new EcoBountyData(setOn.getUniqueId(), payment);
    }
}
