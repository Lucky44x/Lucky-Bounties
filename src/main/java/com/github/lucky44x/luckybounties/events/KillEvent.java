package com.github.lucky44x.luckybounties.events;

import com.github.lucky44x.api.luckybounties.events.BountyCollectEvent;
import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KillEvent implements Listener {

    private final LuckyBounties instance;

    public KillEvent(LuckyBounties instance){
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e){
        if(e.getEntity().getKiller() == null)
            return;

        if(e.getEntity().getKiller() == e.getEntity())
            return;

        if(!instance.getConditionManager().shouldDrop(e.getEntity().getKiller(), e.getEntity()))
            return;

        Bounty[] bounties = instance.getHandler().getBountiesByTarget(e.getEntity());

        BountyCollectEvent event = new BountyCollectEvent(e.getEntity().getKiller(), e.getEntity(), bounties);
        instance.getBridge().callEvent(event);
        if(event.isCancelled())
            return;

        for(Bounty b : event.getBounties()){
            b.receiveBounty(e.getEntity().getKiller());
        }

        instance.getHandler().addStatTaken(e.getEntity().getKiller().getUniqueId());
        instance.getChatManager().sendTakeMessage(e.getEntity().getKiller(), e.getEntity(), bounties);
    }
}
