package com.github.lucky44x.luckybounties.integration.plugins;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.integration.ConditionPluginIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.LBIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

/**
 * @author Lucky44x
 * a WorldGuard integration
 */
@LBIntegration(value = LBIntegration.LoadTime.LOAD, errorMessage = "Worldguard-Integration has to be loaded at startup")
public class WorldGuardIntegration extends ConditionPluginIntegration {
    private StateFlag lbEnabled, setAllowed, removeAllowed, drop, exempt, invisible;
    private final WorldGuard guardInstance;
    private final FlagRegistry registry;

    public WorldGuardIntegration(LuckyBounties instance) {
        super(instance, "WorldGuard");
        instance.getLogger().info("Creating WorldGuard-Link");
        this.guardInstance = com.sk89q.worldguard.WorldGuard.getInstance();
        registry = guardInstance.getFlagRegistry();
    }

    @Override
    public void onEnable() throws IntegrationException {
        super.onEnable();
        lbEnabled = registerFlag("lb-enabled", true);
        setAllowed = registerFlag("lb-allow-set", true);
        removeAllowed = registerFlag("lb-allow-remove", true);
        drop = registerFlag("lb-allow-drop", true);
        exempt = registerFlag("lb-exempt", false);
        invisible = registerFlag("lb-invisible", false);
    }

    @Override
    public void onDisable() throws IntegrationException {
        super.onDisable();
    }

    @Override
    public boolean isAllowedToSet(Bounty b, Player target, Player setter){
        boolean[] baseTarget = checkStates(target, new StateFlag[]{exempt, lbEnabled});
        boolean[] baseSetter = checkStates(setter, new StateFlag[]{setAllowed, lbEnabled});

        return !baseTarget[0] && baseTarget[1] && baseSetter[0] && baseSetter[1];
    }

    @Override
    public boolean isAllowedToRemove(Bounty b, Player caller){
        boolean[] removerBase = checkStates(caller, new StateFlag[]{removeAllowed, lbEnabled});

        return removerBase[0] && removerBase[1];
    }

    @Override
    public boolean isVisible(Player asked, Player target) {
        if(instance.configFile.isOpOverrideWGInvis() && asked.hasPermission("lb.op"))
            return true;

        boolean[] baseTarget = checkStates(target, new StateFlag[]{invisible, lbEnabled});
        return !baseTarget[0] && baseTarget[1];
    }

    @Override
    public boolean dropBounties(Player killer, Player killed) {
        return checkStateFlag(killed, drop) && checkStateFlag(killer, drop);
    }

    /**
     * Checks if the StateFlag LB is enabled
     * @param p the player
     * @return true when enabled, false when not
     */
    public boolean isLBEnabled(Player p){
        return checkStateFlag(p, lbEnabled);
    }

    public boolean isExempt(Player p){return checkStateFlag(p, exempt);}
    public boolean isSetAllowed(Player p){return checkStateFlag(p, setAllowed);}

    /**
     * registers a flag
     * @param name the name of the flag
     * @param defaultVal the default value of the flag
     * @return the generated StateFlag
     * @throws IntegrationException when the flag registering fails
     */
    private StateFlag registerFlag(String name, boolean defaultVal) throws IntegrationException {
        try{
            StateFlag flag = new StateFlag(name, defaultVal);
            registry.register(flag);
            return flag;
        }
        catch(FlagConflictException e){
            Flag<?> existing = registry.get(name);
            if(existing instanceof StateFlag){
                return (StateFlag) existing;
            }
            else{
                throw new IntegrationException("Could not register flag: \"" + name + "\" with default value: \"" + defaultVal + "\"\n Existing flag not instanceof StateFlag");
            }
        }
    }

    /**
     * Checks weather or not a StateFlag is enabled
     * @param p the player
     * @param flag the StateFlag
     * @return true, when active, false if not
     */
    private boolean checkStateFlag(Player p, StateFlag flag){
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
        ApplicableRegionSet set = getSet(localPlayer);

        return set.testState(localPlayer, flag);
    }

    /**
     * checks if multiple states are active
     * @param p the player
     * @param flags the flags
     * @return an array of boolean corresponding to their active state
     */
    private boolean[] checkStates(Player p, StateFlag[] flags){
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
        ApplicableRegionSet set = getSet(localPlayer);
        boolean[] ret = new boolean[flags.length];

        for(int i = 0; i < flags.length; i++){
            ret[i] = set.testState(localPlayer, flags[i]);
        }

        return ret;
    }

    /**
     * gets a regionset for the player p
     * @param p the player
     * @return a ApplicableRegionSet for the player p
     */
    private ApplicableRegionSet getSet(LocalPlayer p){
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return query.getApplicableRegions(p.getLocation());
    }
}
