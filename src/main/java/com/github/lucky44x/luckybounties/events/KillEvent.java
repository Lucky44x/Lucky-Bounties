package com.github.lucky44x.luckybounties.events;

import com.github.lucky44x.api.luckybounties.events.BountyCollectEvent;
import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * @author Lucky44x
 * Kill Event Handler
 */
public class KillEvent implements Listener {

    private final LuckyBounties instance;

    public KillEvent(LuckyBounties instance){
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e){
        //When there is no killer, don't do shit
        if(e.getEntity().getKiller() == null)
            return;

        //Doesn't make sense to give someone his own bounty does it?
        if(e.getEntity().getKiller() == e.getEntity())
            return;

        //when the conditions say don't drop, don't do it
        if(!instance.getConditionManager().shouldDrop(e.getEntity().getKiller(), e.getEntity()))
            return;

        Bounty[] bounties = instance.getHandler().getBountiesByTarget(e.getEntity());

        //Call server-event for boutny collection (api 'n stuff)
        BountyCollectEvent event = new BountyCollectEvent(e.getEntity().getKiller(), e.getEntity(), bounties);
        instance.getBridge().callEvent(event);
        //Cancel all of this if the event is cancelled
        if(event.isCancelled())
            return;

        //Give all bounties to killer
        for(Bounty b : event.getBounties()){
            b.receiveBounty(e.getEntity().getKiller());
        }

        //Stat updates
        instance.getHandler().addStatTaken(e.getEntity().getKiller().getUniqueId());
        //Do the message stuff
        instance.getChatManager().sendTakeMessage(e.getEntity().getKiller(), e.getEntity(), bounties);
    }
}
