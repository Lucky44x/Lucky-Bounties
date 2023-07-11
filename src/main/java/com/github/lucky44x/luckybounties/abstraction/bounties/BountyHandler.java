package com.github.lucky44x.luckybounties.abstraction.bounties;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.condition.BountyCondition;
import com.github.lucky44x.luckybounties.user.UserStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public abstract class BountyHandler {
    protected final LuckyBounties instance;
    protected final HashMap<String, BountyCondition> conditionMap = new HashMap<>();

    public BountyHandler(LuckyBounties instance){
        this.instance = instance;
    }

    protected final void finishInit(){
        this.onLoad();
    }

    public abstract Bounty[] getBountiesByTarget(UUID target);
    public abstract Bounty[] getBountiesBySetter(UUID setter);
    public final Bounty[] getBountiesByTarget(Player player){
        return getBountiesByTarget(player.getUniqueId());
    }
    public final Bounty[] getBountiesBySetter(Player setter){
        return getBountiesBySetter(setter.getUniqueId());
    }
    public abstract double getEcoAmount(UUID target);
    public abstract UUID[] getAllTargets();
    public abstract UUID[] getAllUsers();
    public final double getEcoAmount(Player target){
        return getEcoAmount(target.getUniqueId());
    }
    public abstract int getGlobalBountyNum();
    public abstract int getMaxBountyNum();
    public abstract String getMaxBountyName();
    public abstract String getMaxEcoBountyName();
    public abstract double getMaxEcoBountyAmount();
    public abstract double getGlobalEcoAmount();
    public abstract void checkForExpiredTargetBounties(Player target);
    public abstract void checkForExpiredBounties();
    public abstract void checkForExpiredSetterBounties(Player setter);
    public abstract Bounty[] getReturnBuffer(UUID user);
    public abstract void addBounty(Bounty bounty);
    public abstract boolean removeBounty(Bounty bounty);
    public abstract void moveBountyToReturn(Bounty bounty);
    public abstract void addBounty(ItemStack payment, Player target, Player setter);
    public abstract void addBounty(double payment, Player target, Player setter);
    public abstract void clearBounties(Player target);
    public abstract void clearReturnBuffer(UUID target);
    protected abstract UserStats getUserStats(UUID id);
    public abstract UUID getUserMaxBountiesTaken();
    public abstract UUID getUserMaxBountiesReceived();
    public abstract UUID getUserMaxBountiesSet();
    public abstract void resetStats(UUID user);
    public abstract void addStatTaken(UUID target);
    public abstract void subtractStatTaken(UUID target);
    public abstract void removeStatTaken(UUID target);
    public abstract void addStatSet(UUID target);
    public abstract void subtractStatSet(UUID target);
    public abstract void removeStatSet(UUID target);
    public abstract void addStatReceived(UUID target);
    public abstract void subtractStatReceived(UUID target);
    public abstract void removeStatReceived(UUID target);
    public abstract void insertUser(UUID target, UserStats stats);
    protected abstract void onLoad();
    protected abstract void onSave();
    public abstract void dropData();
    public final int getStatBountiesSet(Player user){
        return getUserStats(user.getUniqueId()).getBountiesSet();
    }
    public final int getStatBountiesReceived(Player user){
        return getUserStats(user.getUniqueId()).getBountiesReceived();
    }
    public final int getStatBountiesTaken(Player user){
        return getUserStats(user.getUniqueId()).getBountiesTaken();
    }
    public final void resetStats(Player user){
        this.resetStats(user.getUniqueId());
    }
    public final void disableHandler(){
        onSave();
    }
    public final void expireBounty(Bounty b){
        if(!instance.configFile.isBountiesExpire())
            return;

        if(instance.configFile.isExpiredBountiesReturn()){
            b.returnBounty();
        }

        removeBounty(b);
    }
    public final void returnExpiredBounties(){
        for(Player p : Bukkit.getOnlinePlayers()){
            returnExpiredBounties(p);
        }
    }
    public final void returnExpiredBounties(Player setter){
        //Dumb condition because this will only get called by returnExpiredBounties() which only uses online players, but better be safe than sorry
        if(!setter.isOnline())
            return;

        for(Bounty b : getReturnBuffer(setter.getUniqueId())){
            b.returnBounty();
        }

        clearReturnBuffer(setter.getUniqueId());
    }
}
