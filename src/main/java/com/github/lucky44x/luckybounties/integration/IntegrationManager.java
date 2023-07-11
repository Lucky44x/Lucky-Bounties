package com.github.lucky44x.luckybounties.integration;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.integration.Integration;
import com.github.lucky44x.luckybounties.abstraction.integration.LBIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class IntegrationManager {
    private final LuckyBounties instance;
    private final HashMap<String, Integration> integrationMap = new HashMap<>();

    public IntegrationManager(LuckyBounties instance){
        this.instance = instance;
    }

    public boolean isIntegrationActive(String name){
        return integrationMap.containsKey(name);
    }

    public void registerOrUnregisterIntegration(String name, boolean isConfigActive, Class<?> clazz, LBIntegration.LoadTime time, boolean supressMessage){
        LBIntegration.LoadTime preferredTime = LBIntegration.LoadTime.RUNTIME;

        if(clazz.isAnnotationPresent(LBIntegration.class)){
            preferredTime = clazz.getAnnotation(LBIntegration.class).value();
        }

        if(preferredTime != time){
            if(clazz.isAnnotationPresent(LBIntegration.class)){
                if(!clazz.getAnnotation(LBIntegration.class).errorMessage().isEmpty() && !supressMessage){
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

    public <T extends Integration> T getIntegration(String name, Class<T> clazz){
        try{
            return clazz.cast(integrationMap.get(name));
        }
        catch(ClassCastException e){
            e.printStackTrace();
        }

        return null;
    }

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
    }

    public void unregisterIntegration(String name){
        if(!isIntegrationActive(name))
            throw new RuntimeException("Integration " + name + " is not active");

        try{
            integrationMap.get(name).onDisable();
            integrationMap.remove(name);
        }
        catch (IntegrationException exception){
            throw new RuntimeException("Could not disable Integration: " + name + ": " + exception.getMessage());

        }
    }
}
