package de.lucky44.luckybounties.files.lang;

import de.lucky44.luckybounties.LuckyBounties;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LANG {

    private static File langFile;
    private static FileConfiguration langFileConfig;

    public static void loadLangFile(Plugin instance){
        if(langFile == null){
            langFile = new File(instance.getDataFolder(), "lang.yml");
        }

        langFileConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream defaultStream = instance.getResource("lang.yml");
        if(defaultStream != null){
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            langFileConfig.setDefaults(defaultLang);
        }

        if(!LuckyBounties.LANG_VERSION.equals(getText("version"))){
            Bukkit.getLogger().warning("[LuckyBounties] There is a new LANG version available. Please update by deleting your old lang file (you can just copy the contents into a temporary file and paste them back into the new file)");
        }
    }

    public static void saveDefaultLang(Plugin instance){
        if(langFile == null)
            langFile = new File(instance.getDataFolder(), "lang.yml");

        if(!langFile.exists())
            instance.saveResource("lang.yml", false);
    }

    public static String getText(String identifier){
        if(langFile == null || langFileConfig == null)
            return ChatColor.RED + "ERROR: NO LANG FILE LOADED";

        if(langFileConfig.getString(identifier) == null)
            return ChatColor.RED + "ERROR: NO PARAMETER FOR " + identifier;
        else
            return ChatColor.translateAlternateColorCodes('&', langFileConfig.getString(identifier));
    }
}
