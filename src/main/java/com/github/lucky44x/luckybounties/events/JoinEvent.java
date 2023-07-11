package com.github.lucky44x.luckybounties.events;

import com.github.lucky44x.luckybounties.LuckyBounties;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    private final LuckyBounties instance;

    public JoinEvent(LuckyBounties instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerJoined(PlayerJoinEvent e){
        if(instance.configFile.isBountiesExpire())
            instance.getHandler().checkForExpiredSetterBounties(e.getPlayer());

        instance.getHandler().returnExpiredBounties(e.getPlayer());
    }
}
