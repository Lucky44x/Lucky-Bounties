package de.lucky44.luckybounties;

import de.lucky44.api.luckybounties.LuckyBountiesAPI;
import de.lucky44.api.luckybounties.events.BountiesEvent;
import de.lucky44.luckybounties.gui.core.GUIManager;
import de.lucky44.luckybounties.chat.ChatManager;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.data.loadManager;
import de.lucky44.luckybounties.files.data.saveManager;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.integrations.papi.LuckyBountiesPAPIExtension;
import de.lucky44.luckybounties.integrations.vault.VaultIntegration;
import de.lucky44.luckybounties.system.CommandManager;
import de.lucky44.luckybounties.system.EventManager;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.util.bounty;
import de.lucky44.luckybounties.util.playerData;
import de.lucky44.luckybounties.integrations.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LuckyBounties extends JavaPlugin {
    public static final String CONFIG_VERSION = "1.4";
    public static final String LANG_VERSION = "1.5";

    public static LuckyBounties I;
    public GUIManager guiManager;
    public ChatManager chatManager;
    public CooldownManager cooldownManager;
    public Map<UUID, List<bounty>> bounties = new HashMap<>();
    public Map<UUID, playerData> players = new HashMap<>();

    //For saving the setter UUID in the bounty Items (gets removed from items when dropped)
    public NamespacedKey dataKey;

    //region stats
    public static playerData mostWorth;
    public static playerData mostCollected;

    public static playerData ecoMostWorth;
    //endregion

    //region integrations
    public LuckyBountiesPAPIExtension papiExtension;
    public VaultIntegration Vault;
    //endregion

    //region API
    public List<TabCompleter> completers = new ArrayList<>();
    public List<CommandExecutor> executors = new ArrayList<>();

    public List<LuckyBountiesAPI> apiConnections = new ArrayList<>();
    //endregion

    @Override
    public void onEnable(){
        //region bstats
        Metrics metrics = new Metrics(this, 12684);
        //endregion

        //region Instancing
        if(I != null && I != this)
            getPluginLoader().disablePlugin(this);
        else
            I = this;
        //endregion

        getLogger().info("Loading saved bounties");

        try{
            File oldB = new File("plugins/LuckyBounties/data.json");
            if(oldB.exists()){
                loadManager.LoadOldBounties();
                getLogger().warning("Loaded old-bounties, will not load new bounties to make sure there are no duplicates");
                getLogger().warning("Once you shutdown the server all the old bounties will be saved in the new format, so you can delete the old file 'data.json'");
            }
            else{
                loadManager.loadBounties();
                getLogger().info("Loaded bounties");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("Loading saved player-stats");

        try{
            loadManager.LoadPlayers();
            getLogger().info("Loaded player-stats");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        cooldownManager = new CooldownManager();

        loadConfigData();

        guiManager = new GUIManager(this);
        Objects.requireNonNull(getCommand("bounties")).setExecutor(new CommandManager());
        Bukkit.getServer().getPluginManager().registerEvents(new EventManager(),this);

        getHighestBountyCount();
        getHighestEcoBounty();
        getMostCollected();

        dataKey = new NamespacedKey(this, "lbData");

        chatManager = new ChatManager();

        getLogger().info("Plugin enabled");
    }

    @Override
    public void onDisable(){
        saveData();
    }

    private void saveData(){

        getLogger().info("Saving bounties...");
        try{
            for(UUID key : bounties.keySet()){

                //getLogger().info("Saving " + bounties.get(key).size() + " bounties for " + key.toString());

                saveManager.SaveBounties(key, bounties.get(key));
            }
            getLogger().info("Saved bounties");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        getLogger().info("Saving player-stats...");
        try{
            saveManager.SavePlayers(players.values().toArray(new playerData[0]));
            getLogger().info("Saved player-stats");
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void loadConfigData(){
        CONFIG.instance = this;
        CONFIG.saveDefaultConfig();
        CONFIG.loadConfig();
        LANG.saveDefaultLang(this);
        LANG.loadLangFile(this);
    }

    public void reloadConfigData(){
        CONFIG.saveDefaultConfig();
        CONFIG.reloadConfig();
        LANG.saveDefaultLang(this);
        LANG.loadLangFile(this);
    }

    //region bounty management
    public List<bounty> fetchBounties(UUID id){
        return bounties.computeIfAbsent(id, k -> new ArrayList<>());
    }

    public void addBounty(UUID id, bounty toAdd, UUID setter){

        if(toAdd.moneyPayment > 0){
            bounty ecoBounty = getEcoBounty(id);

            if(ecoBounty == null)
                bounties.computeIfAbsent(id, k -> new ArrayList<>()).add(toAdd);
            else
                ecoBounty.moneyPayment += toAdd.moneyPayment;

            if(CONFIG.getBool("bounty-set-global")){
                Bukkit.broadcastMessage(LANG.getText("eco-bounty-set-global")
                        .replace("[PLAYERNAME]", setter == null ? LANG.getText("console-setter-name") : Bukkit.getPlayer(setter).getName())
                        .replace("[AMOUNT]", ""+toAdd.moneyPayment)
                        .replace("[SYMBOL]", CONFIG.getString("currency-symbol"))
                        .replace("[TARGET]", Bukkit.getPlayer(id).getName()));
            }
            else{
                if(setter != null)
                    Bukkit.getPlayer(setter).sendMessage(LANG.getText("eco-bounty-set")
                        .replace("[AMOUNT]", ""+ toAdd.moneyPayment)
                        .replace("[SYMBOL]", CONFIG.getString("currency-symbol"))
                        .replace("[TARGET]", Bukkit.getPlayer(id).getName()));
            }

            ecoBounty = getEcoBounty(id);

            if(ecoBounty == null)
                return;

            fetchPlayer(id).ecoWorth = ecoBounty.moneyPayment;
            if(ecoMostWorth == null || fetchPlayer(id).ecoWorth >= ecoMostWorth.ecoWorth){
                ecoMostWorth = fetchPlayer(id);
            }
        }
        else{
            ItemMeta meta = toAdd.payment.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(dataKey, PersistentDataType.STRING, setter != null ? setter.toString() : "CONSOLE");
            toAdd.payment.setItemMeta(meta);
            bounties.computeIfAbsent(id, k -> new ArrayList<>()).add(toAdd);
            if(CONFIG.getBool("bounty-set-global")){
                Bukkit.broadcastMessage(LANG.getText("bounty-set-global")
                        .replace("[PLAYERNAME]", setter == null ? LANG.getText("console-setter-name") : Bukkit.getPlayer(setter).getName())
                        .replace("[AMOUNT]", ""+toAdd.payment.getAmount())
                        .replace("[ITEM]", toAdd.payment.getType().name())
                        .replace("[TARGET]", Bukkit.getPlayer(id).getName()));
            }
            else{
                if(setter != null)
                    Bukkit.getPlayer(setter).sendMessage(LANG.getText("bounty-set")
                            .replace("[AMOUNT]", ""+ toAdd.payment.getAmount())
                            .replace("[ITEM]", toAdd.payment.getType().name())
                            .replace("[TARGET]", Bukkit.getPlayer(id).getName()));
            }
        }

        fetchPlayer(id).onGetSetOn();
    }

    public ItemStack cleanBountyItem(bounty b){
        ItemStack toDrop = b.payment.clone();
        ItemMeta meta = toDrop.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if(container.has(LuckyBounties.I.dataKey, PersistentDataType.STRING)){
            container.remove(LuckyBounties.I.dataKey);
            toDrop.setItemMeta(meta);
        }
        return toDrop;
    }

    public void clearBounties(UUID id){
        bounties.computeIfAbsent(id, k -> new ArrayList<>()).clear();
        fetchPlayer(id).onDeath();

        getHighestEcoBounty();
    }

    public bounty getEcoBounty(UUID id){

        if(Vault == null)
            return null;

        for(bounty b : fetchBounties(id)){
            if(b.moneyPayment > 0)
                return b;
        }

        return null;
    }

    public void removeBounty(UUID id, int index){
        bounties.computeIfAbsent(id, k -> new ArrayList<>()).remove(index);
    }

    public  void removeBounty(UUID id, bounty b){
        bounties.computeIfAbsent(id, k -> new ArrayList<>()).remove(b);
    }

    public void removeBounty(UUID target, float amount){
        bounty ecoBounty = getEcoBounty(target);
        if(ecoBounty == null)
            return;
        ecoBounty.moneyPayment -= amount;
        ecoBounty.moneyPayment = ecoBounty.moneyPayment < 0 ? 0 : ecoBounty.moneyPayment;
    }
    //endregion

    //region stats

    public String getAllEcoBountyWorth() {
        double completeAmount = 0;

        for(UUID player : bounties.keySet()){
            bounty ecoBounty = getEcoBounty(player);
            if(ecoBounty == null)
                continue;
            completeAmount += ecoBounty.moneyPayment;
        }

        return ""+completeAmount;
    }

    public void getHighestEcoBounty(){

        ecoMostWorth = null;

        for(playerData pD : players.values()){

            if(pD.ecoWorth == 0)
                continue;

            if(ecoMostWorth == null || ecoMostWorth.ecoWorth < pD.ecoWorth)
                ecoMostWorth = pD;
        }
    }

    public void getHighestBountyCount(){

        mostWorth = null;

        for(playerData pD : players.values()){

            if(pD.worth == 0)
                continue;

            if(mostWorth == null || mostWorth.worth < pD.worth)
                mostWorth = pD;
        }
    }

    public void getMostCollected(){

        mostCollected = null;

        for(playerData pD : players.values()){

            if(pD.collected == 0)
                continue;

            if(mostCollected == null || mostCollected.collected < pD.collected)
                mostCollected = pD;
        }
    }
    //endregion

    //region player management
    public playerData fetchPlayer(UUID uuid){
        return players.computeIfAbsent(uuid, k -> new playerData(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName(), uuid));
    }
    //endregion

    //region API-connection
    public void callEvent(BountiesEvent e){
        for(LuckyBountiesAPI api : apiConnections){
            api.callEvent(e);
        }
    }
    //endregion
}
