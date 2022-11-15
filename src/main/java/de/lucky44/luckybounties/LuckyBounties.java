package de.lucky44.luckybounties;

import de.lucky44.gui.GUIManager;
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
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LuckyBounties extends JavaPlugin {
    public static final String CONFIG_VERSION = "1.3";
    public static final String LANG_VERSION = "1.4";

    public static LuckyBounties I;
    public GUIManager guiManager;
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

    @Override
    public void onEnable(){
        //region bstats
        int pluginID = <PLUGINID>;
        Metrics metrics = new Metrics(this, pluginID);
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

                getLogger().info("Saving " + bounties.get(key).size() + " bounties for " + key.toString());

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
            dataContainer.set(dataKey, PersistentDataType.STRING, setter.toString());
            toAdd.payment.setItemMeta(meta);
            bounties.computeIfAbsent(id, k -> new ArrayList<>()).add(toAdd);
        }

        fetchPlayer(id).onGetSetOn();
    }

    public ItemStack cleanBountyItem(bounty b){
        ItemStack toDrop = b.payment;
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
    //endregion

    //region stats
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
}
