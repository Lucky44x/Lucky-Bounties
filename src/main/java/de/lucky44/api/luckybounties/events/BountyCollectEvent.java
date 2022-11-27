package de.lucky44.api.luckybounties.events;

import de.lucky44.api.luckybounties.util.BountyData;
import de.lucky44.api.luckybounties.util.EcoBountyData;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BountyCollectEvent extends BountiesEvent{
    public final Player collector;
    public final Player killed;

    public final EcoBountyData ecoBounty;
    public final BountyData[] droppedBounties;

    public BountyCollectEvent(Player killer, Player killed, bounty[] bounties){
        this.collector = killer;
        this.killed = killed;

        List<BountyData> data = new ArrayList<>();
        bounty eco = null;
        for(bounty b : bounties){
            if(b.payment == null){
                eco = b;
            }
            else
                data.add(new BountyData(killed.getUniqueId(), b));
        }
        if(eco != null)
            ecoBounty = new EcoBountyData(killed.getUniqueId(), eco.moneyPayment);
        else
            ecoBounty = null;

        droppedBounties = data.toArray(BountyData[]::new);
    }
}
