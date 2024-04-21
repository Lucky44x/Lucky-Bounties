package com.github.lucky44x.luckybounties.conditions;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.condition.BountyCondition;
import com.github.lucky44x.luckybounties.abstraction.integration.EconomyHandler;
import com.github.lucky44x.luckybounties.abstraction.integration.Integration;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ConditionManager {
    private final HashMap<String, BountyCondition> conditionMap = new HashMap<>();
    private final LuckyBounties instance;

    public ConditionManager(LuckyBounties instance){
        this.instance = instance;
    }

    public boolean isAllowedToSet(Bounty bounty, Player target, Player setter){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.isAllowedToSet(bounty,target,setter))
                return false;
        }
        return true;
    }

    public boolean isAllowedToRemove(Bounty bounty, Player caller){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.isAllowedToRemove(bounty, caller))
                return false;
        }
        return true;
    }

    public boolean isVisible(Player asked, Player target){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.isVisible(asked, target))
                return false;
        }
        return true;
    }

    public boolean shouldDrop(Player killer, Player killed){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.dropBounties(killer,killed))
                return false;
        }

        return true;
    }

    public void registerCondition(BountyCondition condition){
        conditionMap.put(condition.getClass().getSimpleName(), condition);
    }

    public void unregisterCondition(BountyCondition condition){
        conditionMap.remove(condition.getClass().getSimpleName());
    }

    public String getConditionString() {
        StringBuilder finalOut = new StringBuilder();
        for(Map.Entry<String, BountyCondition> entry : conditionMap.entrySet()){
            finalOut.append("        ").append(entry.getKey()).append(" : ").append(entry.getValue().getClass().getSimpleName()).append("\n");
        }
        return finalOut.toString();
    }
}
