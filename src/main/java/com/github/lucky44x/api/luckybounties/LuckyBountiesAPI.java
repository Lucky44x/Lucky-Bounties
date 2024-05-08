package com.github.lucky44x.api.luckybounties;

import com.github.lucky44x.luckybounties.LuckyBounties;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

/**
 * The LuckyBountiesAPI-Object which acts as a more user-friendly bridge to the plugin
 * @author Nick Balischewski
 */
public final class LuckyBountiesAPI {
    private final Plugin instance;
    private final LuckyBounties pluginInstance;

    /**
     * The constructor to create the bridge between your plugin in LuckyBounties
     * @param caller The Plugin instance which created this API-Instance
     */
    public LuckyBountiesAPI(Plugin caller){
        this.instance = caller;

        if(instance.getServer().getPluginManager().isPluginEnabled("LuckyBounties")){
            this.pluginInstance = (LuckyBounties) instance.getServer().getPluginManager().getPlugin("LuckyBounties");
        }
        else{
            pluginInstance = null;
            instance.getLogger().severe("LuckyBountiesAPI could not initialize because: LuckyBounties is not installed, or is not active on the server. Please make sure LuckyBounties is installed and is loaded before this extensionPlugin");
        }
    }

    /**
     * Registers a new executor which will be executed when /bounties (alias: /b) is called by a player (use to extend functionality, like adding /bounties "yourOwnOption")
     * @param executor The commandExecutor Instance which is responsible for this command-extension
     */
    public void registerBountyCommand(CommandExecutor executor){
        registerBountyCommand(executor, null);
    }

    /**
     * Registers a new executor which will be executed when /luckybounties (alias /lb) is called by an operator (permission: lb.op), use to extend functionality, like adding /lb clear or something like that
     * @param executor The commandExecutor Instance which is responsible for this command-extension
     */
    public void registerBountyOpCommand(CommandExecutor executor){
        registerBountyOpCommand(executor, null);
    }

    /**
     * Registers a new executor which will be executed when /bounties (alias: /b) is called by a player (use to extend functionality, like adding /bounties "yourOwnOption")
     * Also registers a tabcompleter which will get asked for the completion before the main plugin does it stuff
     * @param executor The commandExecutor Instance which is responsible for this command-extension
     * @param completer The tabCompleter Instance which is responsible for this command-extension
     */
    public void registerBountyCommand(CommandExecutor executor, TabCompleter completer){
        pluginInstance.getBridge().registerCommand(executor, completer);
    }

    /**
     * Registers a new executor which will be executed when /luckybounties (alias /lb) is called by an operator (permission: lb.op), use to extend functionality, like adding /lb clear or something like that
     * Also registers a tabcompleter which will get asked for the completion before the main plugin does it stuff
     * @param executor The commandExecutor Instance which is responsible for this command-extension
     * @param completer The tabCompleter Instance which is responsible for this command-extension
     */
    public void registerBountyOpCommand(CommandExecutor executor, TabCompleter completer){
        pluginInstance.getBridge().registerOPCommand(executor, completer);
    }

    /**
     * Registers an object-instance as a GUI-Extension, meaning when used together with the LuckyGUI library, you can create custom buttons, by creating a function a GUITag and designating a slot in the GUI's JSON file, for more info see LuckyGUI
     * @param extensionInstance The Object Instance that should be searched reflectively to get annotated methods
     * @param GUIName The Name of the GUI (You can see valid names here: "server/plugins/targetPlugin/LuckyGUI/*.json"
     */
    public void registerGUIExtension(Object extensionInstance, String GUIName){
        pluginInstance.getBridge().registerGUIExtension(GUIName, extensionInstance);
    }

    /**
     * unregisters a GUI-Extension
     * @param extensionInstance The Object Instance that should be searched reflectively to get annotated methods
     * @param GUIName The Name of the GUI (You can see valid names here: "server/plugins/targetPlugin/LuckyGUI/*.json"
     */
    public void unregisterGUIExtension(Object extensionInstance, String GUIName){
        pluginInstance.getBridge().unRegisterGUIExtension(GUIName, extensionInstance);
    }
}
