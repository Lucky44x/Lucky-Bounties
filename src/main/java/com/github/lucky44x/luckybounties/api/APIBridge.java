package com.github.lucky44x.luckybounties.api;

import com.github.lucky44x.luckybounties.LuckyBounties;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class APIBridge {
    private final LuckyBounties instance;
    @Getter
    private final Map<CommandExecutor, TabCompleter> commandExtensions = new HashMap<>();
    @Getter
    private final Map<CommandExecutor, TabCompleter> opCommandExtensions = new HashMap<>();
    @Getter
    private final Map<String, ArrayList<Object>> GUIExtensions = new HashMap<>();

    public APIBridge(LuckyBounties instance){
        this.instance = instance;
        for(String GUIName : LuckyBounties.GUINames){
            GUIExtensions.put(GUIName, new ArrayList<>());
        }
    }

    public void registerCommand(CommandExecutor executor, TabCompleter completer){
        commandExtensions.put(executor, completer);
    }

    public void registerOPCommand(CommandExecutor executor, TabCompleter completer){
        opCommandExtensions.put(executor, completer);
    }

    public void unregisterCommand(CommandExecutor executor){
        commandExtensions.remove(executor);
    }

    public void unregisterOPCommand(CommandExecutor executor){
        opCommandExtensions.remove(executor);
    }

    public final void registerGUIExtension(String GUI, Object objectInstance){
        if(!GUIExtensions.containsKey(GUI)){
            instance.getLogger().warning("Could not register GUIExtension of type " + objectInstance.getClass().getSimpleName() + " because targeted GUI " + GUI + " does not exist");
            return;
        }

        GUIExtensions.get(GUI).add(objectInstance);
    }

    public final void unRegisterGUIExtension(String GUI, Object objectInstance){
        if(!GUIExtensions.containsKey(GUI)){
            instance.getLogger().warning("Could not unregister GUIExtension of type " + objectInstance.getClass().getSimpleName() + " because targeted GUI " + GUI + " does not exist");
            return;
        }

        GUIExtensions.get(GUI).remove(objectInstance);
    }

    public final Object[] getGUIExtensions(String GUIName){
        return GUIExtensions.get(GUIName).toArray();
    }

    public void callEvent(Event event){
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
