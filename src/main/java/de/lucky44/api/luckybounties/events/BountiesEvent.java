package de.lucky44.api.luckybounties.events;

import org.bukkit.entity.Player;

public class BountiesEvent {
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;
    }
}
