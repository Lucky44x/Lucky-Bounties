package de.lucky44.luckybounties.files.config;

import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class COMMANDCONFIG {
    private static File commandFile;
    private static FileConfiguration commandFileConfig;

    public static void updateCommands(JavaPlugin instance){
        saveDefaultCommands(instance);
        File configFile = new File(instance.getDataFolder(), "commands.yml");
        try {
            ConfigUpdater.update(instance, "commands.yml", configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadCommandFile(Plugin instance){
        if(commandFile == null){
            commandFile = new File(instance.getDataFolder(), "commands.yml");
        }

        commandFileConfig = YamlConfiguration.loadConfiguration(commandFile);

        InputStream defaultStream = instance.getResource("commands.yml");
        if(defaultStream != null){
            YamlConfiguration defaultCommands = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            commandFileConfig.setDefaults(defaultCommands);
        }
    }

    public static void saveDefaultCommands(Plugin instance){
        if(commandFile == null)
            commandFile = new File(instance.getDataFolder(), "commands.yml");

        if(!commandFile.exists())
            instance.saveResource("commands.yml", false);
    }

    public static String getText(String identifier){
        if(commandFile == null || commandFileConfig == null)
            return ChatColor.RED + "ERROR: NO COMMAND FILE LOADED";

        if(commandFileConfig.getString(identifier) == null)
            return ChatColor.RED + "ERROR: NO PARAMETER FOR " + identifier;
        else
            return ChatColor.translateAlternateColorCodes('&', commandFileConfig.getString(identifier));
    }

    public static List<String> getStringList(String identifier){
        if(commandFile == null || commandFileConfig == null)
            return Stream.of(ChatColor.RED + "ERROR: NO COMMAND FILE LOADED").toList();

        if(commandFileConfig.getStringList(identifier).size() == 0)
            return Stream.of(ChatColor.RED + "ERROR: NO PARAMETER FOR " + identifier).toList();

        List<String> ret = new ArrayList<>();
        for(String s : commandFileConfig.getStringList(identifier)){
            ret.add(ChatColor.translateAlternateColorCodes('&', s));
        }

        return ret;
    }
}