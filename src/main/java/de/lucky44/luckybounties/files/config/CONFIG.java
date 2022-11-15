package de.lucky44.luckybounties.files.config;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.integrations.papi.LuckyBountiesPAPIExtension;
import de.lucky44.luckybounties.integrations.vault.VaultIntegration;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.timers.RankingNotification;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class CONFIG {

    static FileConfiguration config;
    public static Plugin instance;

    public static long rankingMessageDelay;
    public static boolean rankingMessageEnabled;

    public static void loadConfig(){
        config = instance.getConfig();

        if(!LuckyBounties.CONFIG_VERSION.equals(getString("version"))){
            Bukkit.getLogger().warning("[LuckyBounties] There is a new CONFIG version available. Please update by deleting your old config file (you can just copy the contents into a temporary file and paste them back into the new file)");
        }

        rankingMessageDelay = toTickTime(getString("ranking-message-interval"));

        boolean oldSetting = rankingMessageEnabled;
        rankingMessageEnabled = getBool("ranking-message-enabled");
        if(!oldSetting && rankingMessageEnabled){
            Bukkit.getScheduler().runTaskLaterAsynchronously(LuckyBounties.I, RankingNotification::run, CONFIG.rankingMessageDelay);
        }

        if(getBool("vault-integration")){
            if(Bukkit.getServer().getPluginManager().getPlugin("Vault") == null){
                Bukkit.getLogger().severe(ChatColor.RED + "[LuckyBounties] Could not find Vault, yet it is enabled in the config.\nPlease install Vault");
            }
            else{
                LuckyBounties.I.Vault = new VaultIntegration();
            }
        }
        else{
            LuckyBounties.I.Vault = null;
        }

        if(getBool("papi-integration")){
            if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null){
                Bukkit.getLogger().severe(ChatColor.RED + "[LuckyBounties] Could not find PlaceholderAPI, yet it is enabled in the config.\nPlease install PlaceholderAPI");
            }
            else{

                if(LuckyBounties.I.papiExtension != null)
                    LuckyBounties.I.papiExtension.unregister();

                LuckyBounties.I.papiExtension = new LuckyBountiesPAPIExtension();
                LuckyBounties.I.papiExtension.register();
            }
        }
        else{
            if(LuckyBounties.I.papiExtension != null)
                LuckyBounties.I.papiExtension.unregister();

            LuckyBounties.I.papiExtension = null;
        }

        CooldownManager.I.enabled = getBool("cooldown-enabled");

        long cooldownTime = toMillisecTime(getString("cooldown-time"));
        if(CooldownManager.I.cooldownTime != cooldownTime)
            CooldownManager.I.flushData();
        CooldownManager.I.cooldownTime = cooldownTime;

        if(CooldownManager.I.globalCooldown != getBool("global-cooldown"))
            CooldownManager.I.flushData();
        CooldownManager.I.globalCooldown = getBool("global-cooldown");
    }
    public static void saveDefaultConfig(){
        instance.saveDefaultConfig();
    }
    public static void reloadConfig(){
        instance.reloadConfig();
        loadConfig();
    }
    public static boolean getBool(String path){
        return config.getBoolean(path);
    }
    public static String getString(String path){
        return config.getString(path);
    }

    public static long toTickTime(String input){
        long out = 0;
        String[] parts = input.split(":");
        for(String time : parts){
            String[] units = time.split("_");
            long base = Long.parseLong(units[0]);
            long multiplier = switch (units[1]) {
                case ("h") -> 72000;
                case ("m") -> 1200;
                case ("s") -> 20;
                default -> 0;
            };
            out += base * multiplier;
        }
        return out;
    }

    public static long toMillisecTime(String input){
        long out = 0;
        String[] parts = input.split(":");
        for(String time : parts){
            String[] units = time.split("_");
            long base = Long.parseLong(units[0]);
            long multiplier = switch (units[1]) {
                case ("h") -> 3600000;
                case ("m") -> 60000;
                case ("s") -> 1000;
                default -> 0;
            };
            out += base * multiplier;
        }
        return out;
    }
}
