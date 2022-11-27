package de.lucky44.luckybounties.timers;

import de.lucky44.luckybounties.files.config.CONFIG;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {

    public static CooldownManager I;


    public boolean enabled;
    private final HashMap<UUID, HashMap<UUID, Long>> cooldownMap;
    public boolean globalCooldown = false;
    public long cooldownTime = 0;

    public CooldownManager(){
        cooldownMap = new HashMap<>();
        I = this;
    }

    public void flushData(){
        cooldownMap.clear();
    }

    public void setBounty(Player target, Player setter){

        cooldownMap.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>());

        if(globalCooldown)
            cooldownMap.get(target.getUniqueId()).put(target.getUniqueId(), System.currentTimeMillis());
        else
            cooldownMap.get(target.getUniqueId()).put(setter.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isAllowedToSet(Player target, Player setter){

        if(!enabled)
            return true;

        if(setter.hasPermission("lb.op") && CONFIG.getBool("op-ignore-cooldown"))
            return true;

        long lastTime = 0;

        HashMap<UUID, Long> timeMap = cooldownMap.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>());

        if(globalCooldown){
            if(timeMap.containsKey(target.getUniqueId())){
                lastTime = timeMap.getOrDefault(target.getUniqueId(), System.currentTimeMillis());
            }
            else{
                return true;
            }
        }
        else {
            if(timeMap.containsKey(setter.getUniqueId())){
                lastTime = timeMap.getOrDefault(setter.getUniqueId(), System.currentTimeMillis());
            }
            else{
                return true;
            }
        }

        long diff = System.currentTimeMillis() - lastTime;

        return diff >= cooldownTime;
    }
}
