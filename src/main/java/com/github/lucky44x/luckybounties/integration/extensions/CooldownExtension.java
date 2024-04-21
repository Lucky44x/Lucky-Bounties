package com.github.lucky44x.luckybounties.integration.extensions;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.ConditionIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.LBIntegration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Lucky44x
 * a simple cooldown extension for adding this behaviour to the setting system
 */
public class CooldownExtension extends ConditionIntegration {
    private final HashMap<UUID, HashMap<UUID, Long>> cooldownMap = new HashMap<>();

    public CooldownExtension(LuckyBounties instance) {
        super(instance);
    }

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter) {
        final long cooldownTime = instance.configFile.toMillisecTime(instance.configFile.getCooldownTime());
        long lastSetTime;

        if(instance.configFile.getCooldownMode() == 0){
            lastSetTime = cooldownMap.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>()).getOrDefault(target.getUniqueId(), 0L);
        }
        else{
            lastSetTime = cooldownMap.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>()).getOrDefault(setter.getUniqueId(), 0L);
        }

        long elapsedTime = System.currentTimeMillis() - lastSetTime;

        if(elapsedTime >= cooldownTime)
            return true;

        instance.getChatManager().sendLangMessage("cooldown-not-done", setter, this);
        return false;
    }

    @Override
    public boolean isAllowedToRemove(Bounty b, Player caller) {
        return true;
    }

    @Override
    public boolean dropBounties(Player killer, Player killed) {
        return true;
    }

    /**
     * Updates the cooldown to the specified values when setting bounties
     * @param target the target of the bounty - set
     * @param setter the setter of the bounty - set
     */
    public void setCooldown(UUID target, UUID setter){
        if(instance.configFile.getCooldownMode() == 0)
            cooldownMap.computeIfAbsent(target, k -> new HashMap<>()).put(target, System.currentTimeMillis());
        else
            cooldownMap.computeIfAbsent(target, k -> new HashMap<>()).put(setter, System.currentTimeMillis());
    }

    /**
     * Resets the cooldown-values for the given bounty-set action
     * @param target the target of the bounty-set
     * @param setter the setter of the bounty-set
     */
    public void resetCooldown(UUID target, UUID setter){
        if(instance.configFile.getCooldownMode() == 0)
            cooldownMap.computeIfAbsent(target, k -> new HashMap<>()).put(setter, 0L);
        else
            cooldownMap.computeIfAbsent(target, k -> new HashMap<>()).put(setter, 0L);
    }

    /**
     * drops (deletes) all data this object was holding
     */
    public void dropData(){
        cooldownMap.clear();
    }
}
