package com.github.lucky44x.luckybounties.integration;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.integration.EconomyHandler;
import com.github.lucky44x.luckybounties.abstraction.integration.Integration;
import com.github.lucky44x.luckybounties.abstraction.integration.LBIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lucky44x
 * Manager to manage all integrations and extensions
 *
 * NOTICE: This whole thing stinks of bad code and could probably spontaniously combust at any time, but yeah....
 * too lazy
 */
public class IntegrationManager {
    private final LuckyBounties instance;
    private final HashMap<String, Integration> integrationMap = new HashMap<>();

    @Getter
    private EconomyHandler economyHandler;

    public final boolean isEconomyActive(){return economyHandler != null;}

    public IntegrationManager(LuckyBounties instance){
        this.instance = instance;
    }

    /**
     * @param name the (internal-)name of the integration
     * @return the active-state of the integration with the given name
     */
    public boolean isIntegrationActive(String name){
        return integrationMap.containsKey(name);
    }

    /**
     * When isConfigActive is true, the function will try to register an integration, by creating a new instance of the give Class at runtime.
     * When isConfigActive is false, the function will try to unregister an integration
     * @param name the (internal-)name of the integration
     * @param isConfigActive should the integration nbe registered or unregistered
     * @param clazz the integration's class
     * @param time the load-time parameter (RUNTIME | LOAD)
     * @param suppressMessage should errors and warnings be suppressed?
     */
    public void registerOrUnregisterIntegration(String name, boolean isConfigActive, Class<?> clazz, LBIntegration.LoadTime time, boolean suppressMessage){
        LBIntegration.LoadTime preferredTime = LBIntegration.LoadTime.RUNTIME;

        if(clazz.isAnnotationPresent(LBIntegration.class)){
            preferredTime = clazz.getAnnotation(LBIntegration.class).value();
        }

        if(preferredTime != time){
            if(clazz.isAnnotationPresent(LBIntegration.class)){
                if(!clazz.getAnnotation(LBIntegration.class).errorMessage().isEmpty() && !suppressMessage){
                    instance.getLogger().warning("[INTEGRATION-INIT] " + clazz.getAnnotation(LBIntegration.class).errorMessage());
                }
            }
            return;
        }

        if(isConfigActive){
            if(!isIntegrationActive(name)) {
                try {
                    registerIntegration(name, (Integration) clazz.getConstructor(LuckyBounties.class).newInstance(instance));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            if(isIntegrationActive(name))
                unregisterIntegration(name);
        }
    }

    /**
     * Gets an integration-instance by name
     * @param name the (internal-)name of the integration
     * @param clazz the class which the instance should be cast to
     * @return the integration instance, cast to clazz
     * @param <T> the type of the clazz parameter
     */
    public <T extends Integration> T getIntegration(String name, Class<T> clazz){
        try{
            return clazz.cast(integrationMap.get(name));
        }
        catch(ClassCastException e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Registers an integration under the given (internal-)name
     * @param name the name under which to register the integration
     * @param integration the integration to be registered under the specified name
     */
    public void registerIntegration(String name, Integration integration){
        if(integration == null)
            return;

        if(isIntegrationActive(name))
            throw new RuntimeException("Integration " + name + " is already active");

        try{
            integration.onEnable();
            integrationMap.put(name, integration);
        }
        catch (IntegrationException exception){
            throw new RuntimeException("Could not enable " + integration.getClass().getSimpleName() + ": " + exception.getMessage());
        }

        if(integration instanceof EconomyHandler){
            if(economyHandler != null && economyHandler.getClass() != integration.getClass()){
                Bukkit.getLogger().warning("Trying to register two different economy-handlers \"" + integration.getClass().getSimpleName() + "\" and \"" + economyHandler.getClass().getSimpleName() + "\" which is not supported...");
            }

            this.economyHandler = (EconomyHandler) integration;
        }
    }

    /**
     * unregisters the integration of the given name
     * @param name the name under which to register the integration
     */
    public void unregisterIntegration(String name){
        if(!isIntegrationActive(name))
            throw new RuntimeException("Integration " + name + " is not active");

        try {
            integrationMap.get(name).onDisable();
            if(integrationMap.get(name) instanceof EconomyHandler){
                if(integrationMap.get(name) == economyHandler){
                    economyHandler = null;
                }
            }
            integrationMap.remove(name);
        }
        catch (IntegrationException exception){
            throw new RuntimeException("Could not disable Integration: " + name + ": " + exception.getMessage());

        }
    }

    /**
     * returns a more readable format of the data of this object (only used for debugging)
     * @return a readable format of this object's data for chat/logging purposes
     */
    public String getIntegrationString() {
        StringBuilder finalOut = new StringBuilder();
        for(Map.Entry<String, Integration> entry : integrationMap.entrySet()){
            finalOut.append("        ").append(entry.getKey()).append(" : ").append(entry.getValue().getClass().getSimpleName()).append("\n");
        }
        return finalOut.toString();
    }
}
