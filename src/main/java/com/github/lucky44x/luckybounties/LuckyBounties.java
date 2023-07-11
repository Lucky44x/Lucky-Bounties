package com.github.lucky44x.luckybounties;

import com.github.lucky44x.api.luckybounties.events.BountyRemoveEvent;
import com.github.lucky44x.api.luckybounties.events.BountySetEvent;
import com.github.lucky44x.gui.FileGUI;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.bounties.BountyHandler;
import com.github.lucky44x.luckybounties.abstraction.integration.LBIntegration;
import com.github.lucky44x.luckybounties.api.APIBridge;
import com.github.lucky44x.luckybounties.bounties.handlers.PooledSQLBountyHandler;
import com.github.lucky44x.luckybounties.chat.ChatManager;
import com.github.lucky44x.luckybounties.conditions.ConditionManager;
import com.github.lucky44x.luckybounties.conditions.PermissionsCondition;
import com.github.lucky44x.luckybounties.bounties.handlers.LocalBountyHandler;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.bounties.types.ItemBounty;
import com.github.lucky44x.luckybounties.commands.BountiesCommand;
import com.github.lucky44x.luckybounties.commands.OperatorCommands;
import com.github.lucky44x.luckybounties.config.LuckyBountiesConfig;
import com.github.lucky44x.luckybounties.events.KillEvent;
import com.github.lucky44x.luckybounties.events.JoinEvent;
import com.github.lucky44x.luckybounties.integration.IntegrationManager;
import com.github.lucky44x.luckybounties.integration.extensions.BlacklistExtension;
import com.github.lucky44x.luckybounties.integration.extensions.CooldownExtension;
import com.github.lucky44x.luckybounties.integration.extensions.ExpiredBountiesChecker;
import com.github.lucky44x.luckybounties.integration.extensions.WhitelistExtension;
import com.github.lucky44x.luckybounties.integration.plugins.PapiIntegration;
import com.github.lucky44x.luckybounties.integration.plugins.TownyIntegration;
import com.github.lucky44x.luckybounties.integration.plugins.WorldGuardIntegration;
import com.github.lucky44x.luckybounties.integration.plugins.vanish.SuperPremiumVanishIntegration;
import com.github.lucky44x.luckybounties.integration.plugins.VaultPluginIntegration;
import com.github.lucky44x.luckybounties.integration.plugins.vanish.VanishIntegration;
import com.github.lucky44x.luckybounties.migration.Migrator;
import com.github.lucky44x.luckyutil.config.LangConfig;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LuckyBounties extends JavaPlugin {

    @Getter
    private UUID serverUUID;
    private Metrics metrics = null;

    public static final String[] GUINames = new String[]{
            "BountiesList",
            "PlayerList",
            "SetBounty",
            "ReturnBuffer"
    };

    @Getter
    private final Migrator migrationHelper = new Migrator(this);
    @Getter
    private BountyHandler handler;
    @Getter
    private final ConditionManager conditionManager = new ConditionManager(this);
    @Getter
    private final IntegrationManager integrationManager = new IntegrationManager(this);
    @Getter
    private final ChatManager chatManager = new ChatManager(this);
    @Getter
    private final APIBridge bridge = new APIBridge(this);

    public final LuckyBountiesConfig configFile = new LuckyBountiesConfig(this);
    public final LangConfig langFile = new LangConfig(this, "lang");
    private final OperatorCommands opCommandManager = new OperatorCommands(this);
    private final BountiesCommand bountiesCommandManager = new BountiesCommand(this);

    @Override
    public void onLoad(){
        getLogger().info("Loading Config and Lang");
        //Save config and lang
        configFile.saveDefault();
        langFile.saveDefault();

        //Save GUIs
        saveGUIs();

        //Load config and lang
        configFile.reload();
        langFile.reload();

        serverUUID = UUID.fromString(configFile.getServerUUID());

        if(configFile.isMetricsEnabled()){
            getLogger().info("Metrics are enabled");
            this.metrics = new Metrics(this, 12684);
        }
        else{
            getLogger().info("Metrics, are disabled");
            this.metrics = null;
        }

        getLogger().info("Creating Bounties-Handler");
        //Create handler
        if(configFile.isSqlEnabled()){
            if(!configFile.isSQLModeValid()){
                getLogger().warning("Falling back to a local-handler-instance");
                handler = new LocalBountyHandler(this);
            }
            else{
                handler = new PooledSQLBountyHandler(this,
                        configFile.getSqlHostName(),
                        configFile.getSqlPort(),
                        configFile.getSqlDBName(),
                        configFile.getSqlUserName(),
                        configFile.getSqlPassword());
            }
        }
        else{
            handler = new LocalBountyHandler(this);
        }

        getLogger().info("Initializing Conditions");
        //Register Conditions to conditionManager
        conditionManager.registerCondition(new PermissionsCondition(this));

        getLogger().info("Initializing Load-Integrations");
        //Reload Integrations
        reloadIntegrations(LBIntegration.LoadTime.LOAD, true);
    }

    @Override
    public void onEnable(){
        final long startupTime = System.nanoTime();

        getLogger().info("Initializing Runtime-Integrations");
        reloadIntegrations(LBIntegration.LoadTime.RUNTIME, true);

        getLogger().info("Registering Handlers");
        final long handlerStartTime = System.nanoTime();

        //Register CommandHandlers
        getLogger().info("Register -> /luckybounties CommandHandler");
        PluginCommand opCommand = getCommand("luckybounties");
        opCommand.setExecutor(opCommandManager);
        opCommand.setTabCompleter(opCommandManager);

        getLogger().info("Register -> /bounties CommandHandler");
        PluginCommand bountiesCommand = getCommand("bounties");
        bountiesCommand.setExecutor(bountiesCommandManager);
        bountiesCommand.setTabCompleter(bountiesCommandManager);

        //Register Event-Handlers
        getLogger().info("Register -> killEvent EventHandler");
        KillEvent killEvent = new KillEvent(this);
        getServer().getPluginManager().registerEvents(killEvent, this);
        getLogger().info("Register -> joinEvent EventHandler");
        JoinEvent joinEvent = new JoinEvent(this);
        getServer().getPluginManager().registerEvents(joinEvent, this);

        getLogger().info("Registered 4 Handlers in " + (System.nanoTime() - handlerStartTime) / 1000000 + " ms");
        getLogger().info("Enabled plugin in " + (System.nanoTime() - startupTime) / 1000000 + " ms");
    }

    public void reloadPlugin(CommandSender sender){
        FileGUI.clearGUIData();
        configFile.reload();
        langFile.reload();
        serverUUID = UUID.fromString(configFile.getServerUUID());

        if(metrics != null && !configFile.isMetricsEnabled()){
            this.metrics.shutdown();
            this.metrics = null;
            sender.sendMessage(ChatColor.RED + "Metrics are disabled");
        }
        else if(metrics == null && configFile.isMetricsEnabled()){
            this.metrics = new Metrics(this, 12684);
            sender.sendMessage(ChatColor.GREEN + "Metrics are enabled");
        }

        reloadHandler();
        reloadIntegrations(LBIntegration.LoadTime.RUNTIME, false);

        if(getIntegrationManager().isIntegrationActive("WHLex"))
            integrationManager.getIntegration("WHLex", WhitelistExtension.class).reload();

        if(integrationManager.isIntegrationActive("BLLex"))
            integrationManager.getIntegration("BLLex", BlacklistExtension.class).reload();

        sender.sendMessage(langFile.getText("reload-complete", this));
    }

    private void reloadIntegrations(LBIntegration.LoadTime time, boolean supressMessage){
        //Check VAULT integration
        integrationManager.registerOrUnregisterIntegration(
                "VAULT", configFile.isVaultIntegration(), VaultPluginIntegration.class, time, supressMessage
        );

        //Check PAPI integration
        integrationManager.registerOrUnregisterIntegration(
                "PAPI", configFile.isPapiIntegration(), PapiIntegration.class, time, supressMessage
        );

        //Check expired-bounties-checker extension
        integrationManager.registerOrUnregisterIntegration(
                "EBCex", configFile.isExpiredBountiesCheck(), ExpiredBountiesChecker.class, time, supressMessage
        );

        //Check Whitelist
        integrationManager.registerOrUnregisterIntegration(
                "WHLex", configFile.isWhitelistActive(), WhitelistExtension.class, time, supressMessage
        );

        //Check Blacklist
        integrationManager.registerOrUnregisterIntegration(
                "BLLex", configFile.isBlacklistActive(), BlacklistExtension.class, time, supressMessage
        );

        //Check Cooldown
        integrationManager.registerOrUnregisterIntegration(
                "COLex", configFile.isCooldownEnabled(), CooldownExtension.class, time, supressMessage
        );

        //Check Vanish Integration
        integrationManager.registerOrUnregisterIntegration(
                "VANex", configFile.isVanishEnabled(), VanishIntegration.class, time, supressMessage
        );

        //Check Super/Premium Vanish Integration
        integrationManager.registerOrUnregisterIntegration(
                "SUVANex", configFile.isSuperVanishEnabled(), SuperPremiumVanishIntegration.class, time, supressMessage
        );

        //Check WorldGuard Integration
        integrationManager.registerOrUnregisterIntegration(
                "WGex", configFile.isWorldGuardEnabled(), WorldGuardIntegration.class, time, supressMessage
        );

        //Check Towny Integration
        integrationManager.registerOrUnregisterIntegration(
                "TWAex", configFile.isTownyEnabled(), TownyIntegration.class, time, supressMessage
        );
    }

    private void reloadHandler(){
        if(configFile.isSqlEnabled()){
            if(!configFile.isSQLModeValid())
                return;

            //SQL Connection would be identical, just skip handler reload
            if(handler instanceof PooledSQLBountyHandler && ((PooledSQLBountyHandler)handler).getUrl().equals(
                    "jdbc:" + configFile.getSqlSystemName().toLowerCase() + "://"
                            + configFile.getSqlHostName()
                            + ":" + configFile.getSqlPort()
                            + "/" + configFile.getSqlDBName()) && ((PooledSQLBountyHandler)handler).credentialsEqual(configFile.getSqlPassword(), configFile.getSqlUserName()
                    )){
                return;
            }

            handler.disableHandler();
            handler = new PooledSQLBountyHandler(this,
                    configFile.getSqlHostName(),
                    configFile.getSqlPort(),
                    configFile.getSqlDBName(),
                    configFile.getSqlUserName(),
                    configFile.getSqlPassword());
        }
        else{
            if(handler instanceof LocalBountyHandler)
                return;

            handler.disableHandler();
            handler = new LocalBountyHandler(this);
        }
    }

    private void saveGUIs(){
        for(String guiName : GUINames){
            FileGUI.saveDefaultGUI(guiName, this);
        }
    }

    public void updateGUIs() {
        for(String guiName : GUINames){
            File f = new File(getDataFolder() + "/LuckyGUI/" + guiName + ".json");
            if(!f.exists())
                continue;

            f.delete();
        }

        saveGUIs();
    }

    @Override
    public void onDisable(){
        if(handler == null){
            getLogger().severe("Something went massively wrong: Plugin crashed before handler could be initialized");
            return;
        }
        handler.disableHandler();
    }

    public boolean setBounty(ItemStack reward, Player target, Player setter){
        return setBounty(new ItemBounty(reward, target, setter, this), target, setter);
    }

    public boolean setBounty(double reward, Player target, Player setter){
        return setBounty(new EcoBounty(reward, target, setter, this), target, setter);
    }

    private boolean setBounty(Bounty b, Player target, Player setter){
        if(!conditionManager.isAllowedToSet(b, target, setter))
            return false;

        BountySetEvent event = new BountySetEvent(target, setter, b);
        bridge.callEvent(event);
        if(event.isCancelled())
            return false;

        if(b instanceof EcoBounty && integrationManager.isIntegrationActive("VAULT")){
            integrationManager.getIntegration("VAULT", VaultPluginIntegration.class).withdraw(setter, ((EcoBounty) b).getReward());
        }
        else if(b instanceof EcoBounty && !integrationManager.isIntegrationActive("VAULT")){
           getLogger().warning("Could not withdraw " + ((EcoBounty)b).getReward() + " eco from " + setter.getName() + " -> Vault not enabled");
        }

        handler.addBounty(b);
        handler.addStatSet(b.getSetterID());
        handler.addStatReceived(b.getTargetID());
        chatManager.sendSetMessage(b);
        if(integrationManager.isIntegrationActive("COLex"))
            integrationManager.getIntegration("COLex", CooldownExtension.class).setCooldown(target.getUniqueId(), setter.getUniqueId());
        return true;
    }

    public boolean removeBounty(Bounty bounty, Player caller, boolean returnToCaller){
        if(!conditionManager.isAllowedToRemove(bounty, caller))
            return false;

        BountyRemoveEvent event = new BountyRemoveEvent(caller, bounty);
        bridge.callEvent(event);
        if(event.isCancelled())
            return false;

        if(returnToCaller){
            bounty.giveReward(caller);
        }
        else{
            //Check if return is on
            if(configFile.isReturnRemovedBounties()){
                //Check if setter is online
                if(bounty.getSetterID() != null && Bukkit.getOfflinePlayer(bounty.getSetterID()).isOnline()){
                    //Return bounty to setter
                    bounty.returnBounty();
                }
                else{
                    //Move bounty to setter's returnBuffer
                    handler.moveBountyToReturn(bounty);
                }
            }

            handler.removeBounty(bounty);
        }
        return true;
    }

    public Player[] getOnlinePlayers(Player requester){
        List<Player> players = new ArrayList<>();

        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(conditionManager.isVisible(requester, p))
                players.add(p);
        }

        return players.toArray(Player[]::new);
    }
}
