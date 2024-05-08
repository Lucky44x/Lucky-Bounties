package com.github.lucky44x.luckybounties.commands;

import com.github.lucky44x.gui.FileGUI;
import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.bounties.handlers.LocalBountyHandler;
import com.github.lucky44x.luckybounties.bounties.handlers.PooledSQLBountyHandler;
import com.github.lucky44x.luckybounties.guis.ReturnBufferGUI;
import com.github.lucky44x.luckybounties.integration.extensions.CooldownExtension;
import com.github.lucky44x.luckyutil.config.LangConfig;
import com.github.lucky44x.luckyutil.plugin.LuckyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lucky44x
 * Command-Handler for the LuckyBounties command (operator stuff)
 */
public class OperatorCommands implements CommandExecutor, TabCompleter {
    private final LuckyBounties instance;

    public OperatorCommands(LuckyBounties instance){
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if(!cmd.getName().equalsIgnoreCase("luckybounties"))
            return true;

        for(CommandExecutor executor : instance.getBridge().getOpCommandExtensions().keySet()){
            executor.onCommand(sender, cmd, command, args);
        }

        if(args.length == 0)
            return true;

        switch(args[0]){
            case("reload") -> {
                if(!sender.hasPermission("lb.reload")){
                    sender.sendMessage(instance.langFile.getText("missing-permission",this));
                    return true;
                }

                instance.reloadPlugin(sender);
            }
            case("returnbuffer") -> {
                if(!(sender instanceof Player)){
                    sender.sendMessage(ChatColor.RED + "This command can only be used by Players");
                    return true;
                }

                if(args.length < 2)
                    break;

                OfflinePlayer target = null;
                for(OfflinePlayer player : Bukkit.getOfflinePlayers()){
                    if(player == null || args[1] == null)
                        continue;

                    if(player.getName().equals(args[1])){
                        target = player;
                        break;
                    }
                }

                if(target == null){
                    sender.sendMessage(instance.langFile.getText("player-not-found", new nameCarrier(args[1])));
                    return true;
                }

                try{
                    new ReturnBufferGUI(instance, (Player) sender, target);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }
            case("update") -> {
                if(args.length < 2)
                    break;

                switch(args[1]){
                    case("guis") -> {
                        instance.updateGUIs();
                    }
                    case("bounties") -> {
                        if(!instance.getMigrationHelper().isHasOldData()){
                            sender.sendMessage(ChatColor.RED + "No old data was detected during plugin startup...");
                            break;
                        }
                        instance.getMigrationHelper().migrate(sender);
                    }
                }
            }
            case("purgedata") -> {
                if(args.length < 2)
                    break;

                if(!sender.hasPermission("lb.data")){
                    sender.sendMessage(ChatColor.DARK_RED + "You are missing the lb.data permission. This command deletes ALL save-data, HANDLE WITH CARE!");
                    return true;
                }

                switch (args[1]){
                    case("handler") -> instance.getHandler().dropData();
                    case("cooldown") -> {
                        if(instance.getIntegrationManager().isIntegrationActive("COLex"))
                            instance.getIntegrationManager().getIntegration("COLex", CooldownExtension.class).dropData();
                        else
                            sender.sendMessage(ChatColor.RED + "Cooldown is not enabled");
                    }
                }


            }
            case("time") -> {
                if(args.length < 3)
                    return true;

                switch (args[1]){
                    case("tick") -> sender.sendMessage(String.valueOf(instance.configFile.toTickTime(args[2])));
                    case("milli") -> sender.sendMessage(String.valueOf(instance.configFile.toMillisecTime(args[2])));
                }
            }
            case("dump") -> {
                sender.sendMessage(
                        "Server Version: " + Bukkit.getBukkitVersion(),
                        "Plugin-API Version: " + instance.getDescription().getAPIVersion(),
                        "Plugin Version: " + instance.getDescription().getVersion(),
                        "Lucky-GUI Version: " + FileGUI.class.getPackage().getImplementationVersion(),
                        "Lucky-Util Version: " + LuckyPlugin.class.getPackage().getImplementationVersion(),
                        "Bounty-Handler: " + instance.getHandler().getClass().getSimpleName(),
                        "Economy-Handler: " + (instance.getIntegrationManager().isEconomyActive() ? instance.getIntegrationManager().getEconomyHandler() : "Disabled"),
                        "Integrations: \n " + instance.getIntegrationManager().getIntegrationString(),
                        "Conditions: \n " + instance.getConditionManager().getConditionString()
                );
            }
            case("transfer") -> {

                if(args.length < 3){
                    sender.sendMessage(ChatColor.RED + "\"/lb transfer <old> <new>\" Example: \"/lb transfer sql local\"");
                    return true;
                }

                String previousHandler = args[1];
                String newHandler = args[2];

                if(previousHandler.equalsIgnoreCase(newHandler)){
                    sender.sendMessage(ChatColor.RED + "Doesn't really make sense, does it?");
                    return true;
                }

                if(!(instance.getHandler() instanceof PooledSQLBountyHandler)){
                    sender.sendMessage(ChatColor.RED + "For this operation, you have to enable SQL in the config");
                    return true;
                }

                switch(previousHandler.toLowerCase()){
                    case("sql") -> {
                        ((PooledSQLBountyHandler) instance.getHandler()).saveToDisk();
                        sender.sendMessage(ChatColor.GREEN + "Transferred sql data to local (saved to disk)...");
                    }

                    case("local") -> {
                        LocalBountyHandler tmpHandler = new LocalBountyHandler(instance);
                        instance.getHandler().dropData();
                        ((PooledSQLBountyHandler)instance.getHandler()).transferDataFromLocal(tmpHandler);
                        sender.sendMessage(ChatColor.GREEN + "Transferred local data to sql...");
                    }
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String command, String[] args) {
        if(!cmd.getName().equalsIgnoreCase("luckybounties"))
            return null;

        List<String> ret = new ArrayList<>();
        List<String> playerNames = new ArrayList<>();

        for(Player p : Bukkit.getOnlinePlayers()){
            playerNames.add(p.getName());
        }

        for(Map.Entry<CommandExecutor, TabCompleter> entry : instance.getBridge().getOpCommandExtensions().entrySet()){
            if(entry.getValue() == null)
                continue;

            List<String> toAdd = entry.getValue().onTabComplete(sender, cmd, command, args);
            if(toAdd == null)
                continue;

            ret.addAll(toAdd);
        }

        switch(args.length){
            case(1) -> {
                ret.addAll(List.of("reload", "purgedata", "update", "transfer", "returnbuffer"));
            }
            case(2) -> {
                switch (args[0]){
                    case("update") -> {
                        ret.add("guis");
                        if(instance.getMigrationHelper().isHasOldData())
                            ret.add("bounties");
                    }
                    case("transfer") -> {
                        ret.addAll(List.of("local", "sql"));
                    }
                    case("purgedata") -> {
                        ret.add("handler");
                        if(instance.configFile.isCooldownEnabled())
                            ret.add("cooldown");
                    }
                    case("returnbuffer") -> {
                        ret.addAll(playerNames);
                    }
                }
            }
            case(3) -> {
                switch (args[0]){
                    case("transfer") -> {
                        switch(args[1]){
                            case("local") -> {ret.add("sql");}
                            case("sql") -> {ret.add("local");}
                        }
                    }
                }
            }
        }

        return ret.stream().filter(f -> f.startsWith(args[args.length-1])).toList();
    }

    private record nameCarrier(String name){
        @LangConfig.LangData(langKey = "[NAME]")
        private String getName(){
            return name;
        }
    }
}
