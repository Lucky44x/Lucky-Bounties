package com.github.lucky44x.luckybounties.abstraction.chat;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import org.bukkit.entity.Player;

/**
 * @author Lucky44x
 * BountyMessage-handler
 */
public abstract class BountyMessages {
    protected final LuckyBounties instance;

    public BountyMessages(LuckyBounties instance){
        this.instance = instance;
    }

    protected abstract void sendGlobalTakeMessage(Bounty bounty, Player killer);
    protected abstract void sendGlobalSetMessage(Bounty bounty, Player setter);

    protected abstract void sendLocalTakeMessage(Bounty bounty, Player killer);
    protected abstract void sendLocalSetMessage(Bounty bounty, Player setter);

    public final void sendTakeMessage(Bounty bounty, Player killer){
        if(instance.configFile.isGlobalTakeMessage()){
            sendGlobalTakeMessage(bounty, killer);
        }
        else{
            sendLocalTakeMessage(bounty, killer);
        }
    }

    public final void sendSetMessage(Bounty bounty, Player setter){
        if(instance.configFile.isGlobalSetMessage()){
            sendGlobalSetMessage(bounty, setter);
        }
        else{
            sendGlobalSetMessage(bounty, setter);
        }
    }
}
