package de.lucky44.api.luckybounties;

import de.lucky44.api.luckybounties.events.BountiesEvent;
import de.lucky44.api.luckybounties.util.BountyData;
import de.lucky44.api.luckybounties.util.EcoBountyData;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class LuckyBountiesAPI {
    private JavaPlugin pluginInstance;
    private LuckyBounties bountiesInstance;
    private boolean connected = false;
    private final HashMap<Class<?>, HashMap<Listener, List<Method>>> eventHandlers = new HashMap<>();
    public LuckyBountiesAPI(@NotNull JavaPlugin pluginInstance){
        if(pluginInstance.isEnabled()){
            this.pluginInstance = pluginInstance;
            PluginManager pM = pluginInstance.getServer().getPluginManager();

            if(pM.isPluginEnabled("LuckyBounties")){
                bountiesInstance = (LuckyBounties) pM.getPlugin("LuckyBounties");
                if(bountiesInstance != null){
                    //bountiesInstance.apiConnections.add(this);
                    connected = true;
                }
            }
        }

        checkNotConnected();
    }

    private boolean checkNotConnected(){
        if(!connected)
            Bukkit.getLogger().warning("""
                    LuckyBountiesAPI was not connected to LuckyBounties. This is usually done in the constructor,
                    meaning either you used witchcraft to call this method,
                    or there simply is no LuckyBounties Instance present/running on your server""");

        return !connected;
    }

    //region commands
    public void registerCommand(CommandExecutor executor){
        if(checkNotConnected())
            return;

        bountiesInstance.executors.add(executor);
    }

    public void registerAutoCompleter(TabCompleter completer){
        if(checkNotConnected())
            return;

        bountiesInstance.completers.add(completer);
    }

    public void deregisterCommand(CommandExecutor executor){
        if(checkNotConnected())
            return;

        bountiesInstance.executors.remove(executor);
    }

    public void deregisterAutoCompleter(TabCompleter completer){
        if(checkNotConnected())
            return;

        bountiesInstance.completers.remove(completer);
    }
    //endregion

    //region Random Stuff
    /**
     * Will return a random player's bounty
     * @param chance the chance a bounty will be returned (0-1)
     * @return A random bounty from a random Player. If no bounty could be returned (limit for tries is 10) will return null
     */
    public bounty getRandomBounty(double chance){
        Map<UUID, List<bounty>> playerBountyMap = bountiesInstance.bounties;
        Random random = new Random();

        UUID chosenPlayer = null;

        int counter = 0;
        while(chosenPlayer == null && counter <= 10){
            for(UUID player : playerBountyMap.keySet()){
                if(playerBountyMap.get(player).size() == 0)
                    continue;

                if(random.nextDouble() > (chance/100))
                    continue;

                chosenPlayer = player;
                break;
            }
            counter++;
        }

        if(chosenPlayer == null)
            return null;

        return getRandomBounty(chosenPlayer, chance);
    }

    /**
     * Chooses a random bounty from a player's list of bounties
     * @param player the player
     * @param chance the chance a bounty from the player's bounty list will be chosen
     * @return A random bounty out of all the player's bounties. If no bounty could be returned (limit for tries is 10) will return null
     */
    public bounty getRandomBounty(Player player, double chance){
        return getRandomBounty(player.getUniqueId(), chance);
    }

    /**
     * Chooses a random bounty from a player's list of bounties
     * @param playerUUID the player's UUID
     * @param chance the chance a bounty from the list will be chosen
     * @return A random bounty out of all the player's bounties. If no bounty could be returned (limit for tries is 10) will return null
     */
    public bounty getRandomBounty(UUID playerUUID, double chance){
        if(!bountiesInstance.bounties.containsKey(playerUUID))
            return null;

        List<bounty> bounties = bountiesInstance.bounties.get(playerUUID);
        if(bounties.size() == 0)
            return null;


        Random random = new Random();
        bounty chosenBounty = null;
        int counter = 0;
        while(chosenBounty == null && counter <= 10){
            for(bounty b : bounties){
                if(random.nextDouble() > (chance/100))
                    continue;

                chosenBounty = b;
                break;
            }
            counter ++;
        }

        return chosenBounty;
    }
    //endregion

    //region Events
    /*
    public void registerEvents(Listener listener){

        //Bukkit.getLogger().info("checking class: " + listener.getClass().getName());

        for(Method m : listener.getClass().getMethods()){

            //Bukkit.getLogger().info("Checking method " + m.getName() + " for annotation");

            if(!m.isAnnotationPresent(BountiesEventHandler.class))
                continue;

            //Bukkit.getLogger().info("Checking method " + m.getName() + " for parameters: " + Arrays.toString(m.getParameters()));

            if(m.getParameterCount() != 1)
                continue;

            //Bukkit.getLogger().info("Found annotated method with 1 parameter: " + listener.getClass().getName() + "." + m.getName());

            if(m.getParameters()[0].getType().isAssignableFrom(BountiesEvent.class))
                continue;

            Bukkit.getLogger().info("Registered method " + m.getName() + " as type " + m.getParameters()[0].getType().getName());

            eventHandlers.computeIfAbsent(m.getParameters()[0].getType(), h -> new HashMap<>()).computeIfAbsent(listener, l -> new ArrayList<>()).add(m);
        }
    }

    public void deregisterEvents(Listener listener){
        for(Method m : listener.getClass().getMethods()){
            if(!m.isAnnotationPresent(BountiesEventHandler.class))
                continue;

            if(m.getParameterCount() != 1)
                continue;

            if(m.getParameters()[0].getType().isAssignableFrom(BountiesEvent.class))
                continue;

            if(!eventHandlers.containsKey(m.getParameters()[0].getType()))
                continue;

            eventHandlers.get(m.getParameters()[0].getType()).get(listener).remove(m);
        }
    }

    public void callEvent(BountiesEvent eventInfo){
        if(!eventHandlers.containsKey(eventInfo.getClass()))
            return;

        for(Map.Entry<Listener,List<Method>> entry : eventHandlers.get(eventInfo.getClass()).entrySet()){
            Listener l = entry.getKey();
            for(Method m : entry.getValue()){
                try{
                    m.invoke(l, eventInfo);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
     */
    //endregion

    //region bountiesManagement
    public void removeBounty(BountyData bounty){
        if(bounty.target() == null)
            return;

        bountiesInstance.removeBounty(bounty.target(), bounty.getOriginalBounty());
    }

    public void removeBounty(Player target, float moneyAmount){
        removeBounty(target.getUniqueId(), moneyAmount);
    }

    public void removeBounty(UUID target, float moneyAmount){
        if(target == null)
            return;

        bountiesInstance.removeBounty(target, moneyAmount);
    }

    public void addBounty(Player target, BountyData bounty){
        addBounty(target.getUniqueId(), bounty);
    }

    public void addBounty(Player target, ItemStack item){
        addBounty(target.getUniqueId(), item);
    }

    public void addBounty(UUID target, ItemStack item){
        addBounty(target, new BountyData(target, new bounty(item)));
    }

    public void addBounty(Player target, float moneyAmount){
        addBounty(target.getUniqueId(), moneyAmount);
    }

    public void addBounty(UUID target, float moneyAmount){
        addBounty(target, new BountyData(target, new bounty(moneyAmount)));
    }

    public void addBounty(UUID target, BountyData bounty){
        if(bounty.target() == null)
            return;

        bountiesInstance.addBounty(target, bounty.getOriginalBounty(), null);
    }

    public EcoBountyData getEcoBounty(Player target){
        return getEcoBounty(target.getUniqueId());
    }

    public EcoBountyData getEcoBounty(UUID target){
        return new EcoBountyData(target, LuckyBounties.I.getEcoBounty(target).moneyPayment);
    }

    public BountyData[] getBounties(Player target){
        return getBounties(target.getUniqueId());
    }

    public BountyData[] getBounties(UUID target){
        if(!LuckyBounties.I.bounties.containsKey(target))
            return null;

        List<BountyData> ret = new ArrayList<>();
        List<bounty> bounties = LuckyBounties.I.bounties.get(target);
        if(bounties.size() == 0)
            return null;

        for(bounty b : bounties){
            if(b.moneyPayment > 0)
                continue;

            ret.add(new BountyData(target, b));
        }
        return ret.toArray(BountyData[]::new);
    }
    //endregion

    //region util
    public boolean isVaultEnabled(){
        return LuckyBounties.I.Vault != null;
    }

    public boolean isPlaceHolderAPIEnabled(){
        return LuckyBounties.I.papiExtension != null;
    }
    //endregion
}
