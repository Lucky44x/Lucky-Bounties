package de.lucky44.api.luckybounties.events;

import de.lucky44.api.luckybounties.util.BountyData;
import de.lucky44.api.luckybounties.util.EcoBountyData;
import org.bukkit.entity.Player;

public class EcoBountyRemoveEvent extends BountiesEvent{
    public final Player remover;
    public final Player target;

    public final EcoBountyData bounty;

    public EcoBountyRemoveEvent(Player remover, Player target, float payment){
        this.remover = remover;
        this.target = target;
        this.bounty = new EcoBountyData(target.getUniqueId(), payment);
    }
}
