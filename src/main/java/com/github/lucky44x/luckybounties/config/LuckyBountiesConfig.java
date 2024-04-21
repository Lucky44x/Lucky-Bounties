package com.github.lucky44x.luckybounties.config;

import com.github.lucky44x.luckybounties.abstraction.integration.Integration;
import com.github.lucky44x.luckyutil.config.AutomatedConfig;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.mariadb.jdbc.Driver;

@Getter
public class LuckyBountiesConfig extends AutomatedConfig {

    private final String[] supportedSQLDrivers = new String[]{
            "mysql",
            "mariadb"
    };

    @ConfigData(tag = "system-uuid")
    private String serverUUID;

    @ConfigData(tag = "send-metrics")
    private boolean metricsEnabled = true;

    @ConfigData(tag = "self-bounty-allowed")
    private boolean selfBountyAllowed = false;

    //region message settings
    @ConfigData(tag="bounty-set-global")
    private boolean globalSetMessage = false;

    @ConfigData(tag="bounty-take-global")
    private boolean globalTakeMessage = false;
    //endregion

    //region generalSettings
    @ConfigData(tag="time-format")
    private String timeFormat = "yyyy-MM-dd HH:mm:ss";
    @ConfigData(tag="return-removed-bounties")
    private boolean returnRemovedBounties = false;
    @ConfigData(tag="setters-can-remove-bounties")
    private boolean settersAllowedToRemove = false;
    @ConfigData(tag="expire.enabled")
    private boolean bountiesExpire = false;
    //endregion

    //region GUIs
    @ConfigData(tag="eco-bounties-combined")
    private boolean ecoBountiesMerged = false;
    //endregion

    //region integration settings
    @ConfigData(tag="items-allowed")
    private boolean itemsEnabled = false;

    @ConfigData(tag="vault.enabled")
    private boolean vaultIntegration = false;

    @ConfigData(tag="coins-engine.enabled")
    private boolean coinsEngineIntegration = false;

    @ConfigData(tag="papi.enabled")
    private boolean papiIntegration = false;

    @ConfigData(tag="cooldown.enabled")
    private boolean cooldownEnabled = false;

    //region vanish settings
    @ConfigData(tag="vanish.enabled")
    private boolean vanishEnabled = false;
    @ConfigData(tag="vanish.general-settings.hide-from-everyone")
    private boolean vanishTotalHide = false;
    @ConfigData(tag="vanish.general-settings.do-metadata-check")
    private boolean vanishMetadataCheck = false;
    @ConfigData(tag = "vanish.integrations.SuperVanish") @Getter(AccessLevel.NONE)
    private boolean superVanishEnabled = false;
    @ConfigData(tag = "vanish.integrations.PremiumVanish") @Getter(AccessLevel.NONE)
    private boolean premiumVanishEnabled = false;
    //endregion

    //region Eco settings
    @ConfigData(tag="economy.default-bounty")
    private double defaultEcoBounty = 0;

    @ConfigData(tag="economy.minimum-bounty")
    private double minimumEcoBounty = 0;

    @ConfigData(tag="economy.maximum-bounty")
    private double maximumEcoBounty = 0;

    @ConfigData(tag="economy.eco-item")
    private String ecoItem = "GOLD_NUGGET";
    //endregion

    //region CoinsEngine settings
    @ConfigData(tag="coins-engine.economy-name")
    private String economyName;

    //region WorldGuard
    @ConfigData(tag="worldguard.enabled")
    private boolean worldGuardEnabled;
    @ConfigData(tag="worldguard.invis-region-op-bypass")
    private boolean opOverrideWGInvis;

    //region expiring bounties
    @ConfigData(tag="expire.return-expired-bounties")
    private boolean expiredBountiesReturn;
    @ConfigData(tag = "expire.bounty-lifetime")
    private String bountyLifetime;
    @ConfigData(tag = "expire.periodical-check.enabled")
    private boolean expiredBountiesCheck;
    @ConfigData(tag="expire.periodical-check.period")
    private String expiredCheckPeriod;
    //endregion

    //region lists
    @ConfigData(tag="filters.whitelist")
    private boolean whitelistActive = false;
    @ConfigData(tag="filters.blacklist")
    private boolean blacklistActive = false;
    //endregion

    //region cooldown
    @ConfigData(tag="cooldown.mode")
    private int cooldownMode = 0;

    @ConfigData(tag="cooldown.time")
    private String cooldownTime = "";
    //endregion
    //endregion

    //region towny
    @ConfigData(tag = "towny.enabled")
    private boolean townyEnabled = false;
    @ConfigData(tag = "towny.ally-kill-ignored")
    private boolean townyAllyIgnored = false;
    @ConfigData(tag = "towny.friends-kill-ignored")
    private boolean townyFriendsKillIgnored = false;
    @ConfigData(tag = "towny.same-town-kill-ignored")
    private boolean townySameTownKillIgnored = false;
    @ConfigData(tag = "towny.ally-set-allowed")
    private boolean townyAllySetAllowed = false;
    @ConfigData(tag = "towny.same-town-set-allowed")
    private boolean townySameTownSetAllowed = false;
    @ConfigData(tag = "towny.friends-set-allowed")
    private boolean townyFriendsSetAllowed = false;
    //endregion

    //region SQL
    @ConfigData(tag = "sql.system")
    private String sqlSystemName;
    @ConfigData(tag="sql.host")
    private String sqlHostName;
    @ConfigData(tag="sql.port")
    private int sqlPort;
    @ConfigData(tag="sql.database")
    private String sqlDBName;
    @ConfigData(tag="sql.username")
    private String sqlUserName;
    @ConfigData(tag="sql.password")
    private String sqlPassword;
    @ConfigData(tag="sql.enabled")
    private boolean sqlEnabled;
    //endregion

    //endregion

    public LuckyBountiesConfig(Plugin instance) {
        super(instance, "config");
    }

    public boolean isSuperVanishEnabled(){
        if(!vanishEnabled)
            return false;

        return superVanishEnabled || premiumVanishEnabled;
    }

    public long toMillisecTime(String input){
        long out = 0;
        String[] parts = input.split(":");
        for(String time : parts){
            String[] units = time.split("_");
            long base = Long.parseLong(units[0]);
            long multiplier = switch (units[1]) {
                case ("d") -> 86400000;
                case ("h") -> 3600000;
                case ("m") -> 60000;
                case ("s") -> 1000;
                default -> 0;
            };
            out += base * multiplier;
        }
        return out;
    }

    public long toTickTime(String input){
        long out = 0;
        String[] parts = input.split(":");
        for(String time : parts){
            String[] units = time.split("_");
            long base = Long.parseLong(units[0]);
            long multiplier = switch (units[1]) {
                case ("d") -> 1728000;
                case ("h") -> 72000;
                case ("m") -> 1200;
                case ("s") -> 20;
                default -> 0;
            };
            out += base * multiplier;
        }
        return out;
    }

    public boolean isSQLModeValid() {
        for(String s : supportedSQLDrivers){
            if(sqlSystemName.toLowerCase().equals(s))
                return true;
        }

        instance.getLogger().warning(sqlSystemName + " is not a supported sql driver in this plugin, if you want it to be, you can request it in discord (https://discord.gg/Cc6AfggkMM)");
        return false;
    }

    public String getSQLDriverClassName(){
        if(!isSQLModeValid())
            return null;

        return switch (sqlSystemName.toLowerCase()) {
            case ("mariadb") -> Driver.class.getName();
            default -> null;
        };
    }
}
