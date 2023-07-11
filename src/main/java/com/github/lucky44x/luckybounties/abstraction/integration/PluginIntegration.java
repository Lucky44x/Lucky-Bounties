package com.github.lucky44x.luckybounties.abstraction.integration;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import org.bukkit.Bukkit;

public class PluginIntegration extends Integration{

    private final String pluginName;

    public PluginIntegration(LuckyBounties instance, String pluginName){
        super(instance);
        this.pluginName = pluginName;
    }

    public void onEnable() throws IntegrationException {
        if(Bukkit.getPluginManager().getPlugin(pluginName) == null)
            throw new IntegrationException(pluginName + " is not installed");
    }

    public void onDisable() throws IntegrationException{
        if(Bukkit.getPluginManager().getPlugin(pluginName) == null)
            throw new IntegrationException(pluginName + " is not installed");
    }
}
