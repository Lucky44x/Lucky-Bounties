package com.github.lucky44x.api.luckybounties.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The Abstract Super-Class to all bounty-related events
 * @author Nick Balischewski
 */
public abstract class BountyEvent extends Event {
    private boolean cancelled;
    private static HandlerList handlers = new HandlerList();

    /**
     * Should the event be cancelled ?
     * @return The cancelled-State of the event
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Should the event be cancelled
     * @param cancelled true: will prohibit the LuckyBounties plugin from completing this action
     */
    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Get the handler-list of the event
     * @return the HandlerList-Object of the Event
     */
    public static HandlerList getHandlerList(){
        return handlers;
    }
}
