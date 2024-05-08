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

/**
 * @author Lucky44x
 * Condition Manager
 */
public class ConditionManager {
    private final HashMap<String, BountyCondition> conditionMap = new HashMap<>();
    private final LuckyBounties instance;

    public ConditionManager(LuckyBounties instance){
        this.instance = instance;
    }

    /**
     * Checks if the action to set a bounty is valid under the given conditions, by calling all loaded BountyConditions
     * @param bounty the bounty to be set
     * @param target the target of the bounty
     * @param setter the setter of the bounty
     * @return true if it's valid, false if not
     */
    public boolean isAllowedToSet(Bounty bounty, Player target, Player setter){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.isAllowedToSet(bounty,target,setter))
                return false;
        }
        return true;
    }

    /**
     * Checks if the action to remove a bounty is valid under the given conditions, by calling all loaded BountyConditions
     * @param bounty the bounty to be removed
     * @param caller the "caller" of the action (usually gui user)
     * @return true if it's valid, false if not
     */
    public boolean isAllowedToRemove(Bounty bounty, Player caller){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.isAllowedToRemove(bounty, caller))
                return false;
        }
        return true;
    }

    /**
     * Checks if the target player is visible to the "caller"-player
     * @param asked the "caller"-player
     * @param target the target player
     * @return true when visible, false when not
     */
    public boolean isVisible(Player asked, Player target){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.isVisible(asked, target))
                return false;
        }
        return true;
    }

    /**
     * Checks if a bounty should drop under the given conditions
     * @param killer the killer of the event
     * @param killed the killed player of the event
     * @return true when it should drop, false if not
     */
    public boolean shouldDrop(Player killer, Player killed){
        for(BountyCondition condition : conditionMap.values()){
            if(!condition.dropBounties(killer,killed))
                return false;
        }

        return true;
    }

    /**
     * Registers a condition to this manager
     * @param condition the condition to be registered
     */
    public void registerCondition(BountyCondition condition){
        conditionMap.put(condition.getClass().getSimpleName(), condition);
    }

    /**
     * Unregisters the condition from this manager
     * @param condition teh condition
     */
    public void unregisterCondition(BountyCondition condition){
        conditionMap.remove(condition.getClass().getSimpleName());
    }

    /**
     * @return a more readable string format for debug
     */
    public String getConditionString() {
        StringBuilder finalOut = new StringBuilder();
        for(Map.Entry<String, BountyCondition> entry : conditionMap.entrySet()){
            finalOut.append("        ").append(entry.getKey()).append(" : ").append(entry.getValue().getClass().getSimpleName()).append("\n");
        }
        return finalOut.toString();
    }
}
