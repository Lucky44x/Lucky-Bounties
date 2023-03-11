package de.lucky44.luckybounties;

import de.lucky44.api.luckybounties.events.BountiesEvent;
import de.lucky44.luckybounties.chat.Versions.*;
import de.lucky44.luckybounties.files.DebugLog;
import de.lucky44.luckybounties.files.FilterList;
import de.lucky44.luckybounties.files.config.COMMANDCONFIG;
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
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

//TODO: Complete black / whitelist => actually load white and blacklist from config

public class LuckyBounties extends JavaPlugin {

    public static LuckyBounties I;
    public GUIManager guiManager;
    public ChatManager chatManager;
    public CooldownManager cooldownManager;
    public Map<UUID, List<bounty>> bounties = new HashMap<>();
    public Map<UUID, List<bounty>> returnBuffer = new HashMap<>();
    public Map<UUID, playerData> players = new HashMap<>();
    public FilterList WhiteList = new FilterList();
    public FilterList BlackList = new FilterList();

    //For saving the setter UUID in the bounty Items (gets removed from items when dropped)
    public NamespacedKey dataKey;
    public NamespacedKey timeKey;

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

        DebugLog.init();
        DebugLog.info("Initialized Debug-Logger with plugin version " + getDescription().getVersion());
        getLogger().info("Loading saved bounties");
        DebugLog.info("Loading saved bounties");

        try{
            File oldB = new File("plugins/LuckyBounties/data.json");
            if(oldB.exists()){
                loadManager.LoadOldBounties();
                DebugLog.warn("Loading deprecated save file");
                getLogger().warning("Loaded old-bounties, will not load new bounties to make sure there are no duplicates");
                getLogger().warning("Once you shutdown the server all the old bounties will be saved in the new format, so you can delete the old file 'data.json'");
            }
            else{
                loadManager.loadBounties();
                DebugLog.info("Bounties loaded");
                getLogger().info("Loaded bounties");
            }
        }
        catch(IOException e){
            DebugLog.error("Loading saved bounties failed: IOException");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            DebugLog.error("Loading saved bounties failed: ClassNotFoundException");
            throw new RuntimeException(e);
        }

        getLogger().info("Loading saved player-stats");
        DebugLog.info("Loading saved player-stats");

        try{
            loadManager.LoadPlayers();
            getLogger().info("Loaded player-stats");
            DebugLog.info("Player-Stats loaded");
        }
        catch(IOException e){
            DebugLog.info("Loading player-stats failed: IOException");
            e.printStackTrace();
        }

        getLogger().info("Loading saved return-buffer");
        DebugLog.info("Loading saved return-buffer");

        try{
            loadManager.loadReturnBuffer();
            getLogger().info("Loaded return-buffer");
            DebugLog.info("Return-Buffer loaded");
        }
        catch(IOException e){
            DebugLog.info("Loading return-buffer failed: IOException");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            DebugLog.error("Loading return-buffer failed: ClassNotFoundException");
            throw new RuntimeException(e);
        }

        cooldownManager = new CooldownManager();
        DebugLog.info("Initialized CooldownManager");

        loadConfigData();

        guiManager = new GUIManager(this);
        DebugLog.info("Initialized GUIManager");

        Objects.requireNonNull(getCommand("bounties")).setExecutor(new CommandManager());
        DebugLog.info("Registered 'bounties' command");
        Bukkit.getServer().getPluginManager().registerEvents(new EventManager(),this);
        DebugLog.info("Registered EventManager");

        getHighestBountyCount();
        getHighestEcoBounty();
        getMostCollected();
        DebugLog.info("Reset internal stat-trackers (Get the correct data from the loaded stats)");

        dataKey = new NamespacedKey(this, "lbData");
        DebugLog.info("Registered data-key");

        timeKey = new NamespacedKey(this, "lbTime");
        DebugLog.info("Registered time-key");

        chatManager = new ChatManager_Normal();
        DebugLog.info("Initializing ChatManager");

        try{
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            if("v1_19_R1".equalsIgnoreCase(version))
                chatManager = new ChatManager1_19();
            else if("v1_18_R2".equalsIgnoreCase(version))
                chatManager = new ChatManager1_18_2();
            else if("v1_19_R2".equalsIgnoreCase(version))
                chatManager = new ChatManager1_19_3();
            else if("v1_16_R3".equalsIgnoreCase(version))
                chatManager = new ChatManager1_16_5();

            DebugLog.info("Server using version: " + getServer().getVersion() + " API: " + version + " ChatManager: " + chatManager.getClass().getName());
        }
        catch(ArrayIndexOutOfBoundsException e){
            DebugLog.warn("Something went wrong during ChatManager init");
            e.printStackTrace();
        }

        getLogger().info("Plugin enabled");
        DebugLog.info("Startup finished");
    }

    @Override
    public void onDisable(){
        checkAllExpiredBounties();
        saveData();
    }

    private void saveData(){
        getLogger().info("Saving bounties...");
        DebugLog.info("Saving bounties");
        try{
            for(UUID key : bounties.keySet()){

                DebugLog.info("Saving " + Bukkit.getOfflinePlayer(key).getName() + " bounties");
                //getLogger().info("Saving " + bounties.get(key).size() + " bounties for " + key.toString());

                saveManager.SaveBounties(key, bounties.get(key));
            }
            DebugLog.info("Saved bounties");
            getLogger().info("Saved bounties");
        }
        catch(IOException e){
            DebugLog.error("Saving bounties failed: IOException");
            e.printStackTrace();
        }

        DebugLog.info("Saving playerStats");
        getLogger().info("Saving player-stats...");
        try{
            saveManager.SavePlayers(players.values().toArray(new playerData[0]));
            getLogger().info("Saved player-stats");
            DebugLog.info("Saved playerStats");
        }
        catch(IOException e){
            DebugLog.error("Saving player-stats failed: IOException");
            e.printStackTrace();
        }

        getLogger().info("Saving return-buffer...");
        DebugLog.info("Saving return-buffer");
        try{
            for(UUID key : returnBuffer.keySet()){

                DebugLog.info("Saving " + Bukkit.getOfflinePlayer(key).getName() + " return-buffer");
                //getLogger().info("Saving " + bounties.get(key).size() + " bounties for " + key.toString());

                saveManager.SaveReturnBuffer(key, returnBuffer.get(key));
            }
            DebugLog.info("Saved return-buffer");
            getLogger().info("Saved return-buffer");
        }
        catch(IOException e){
            DebugLog.error("Saving return-buffer failed: IOException");
            e.printStackTrace();
        }
    }

    private void loadConfigData(){
        CONFIG.instance = this;
        CONFIG.updateConfig();
        CONFIG.saveDefaultConfig();
        CONFIG.loadConfig();
        DebugLog.info("Loaded Config");
        LANG.saveDefaultLang(this);
        LANG.updateLang(this);
        LANG.loadLangFile(this);
        DebugLog.info("Loaded Lang");
        COMMANDCONFIG.saveDefaultCommands(this);
        COMMANDCONFIG.updateCommands(this);
        COMMANDCONFIG.loadCommandFile(this);
        DebugLog.info("Loaded Commands");
    }

    public void reloadConfigData(){
        DebugLog.info("Reload Config");
        CONFIG.saveDefaultConfig();
        CONFIG.reloadConfig();
        DebugLog.info("Reloaded Config");
        LANG.saveDefaultLang(this);
        LANG.loadLangFile(this);
        DebugLog.info("Reloaded Lang");
        COMMANDCONFIG.saveDefaultCommands(this);
        COMMANDCONFIG.loadCommandFile(this);
        DebugLog.info("Reloaded Commands");
    }

    //region bounty management
    public List<bounty> fetchBounties(UUID id){
        DebugLog.info("fetching all unexpired bounties of " + id.toString());
        List<bounty> playerBounties = new ArrayList<>(bounties.computeIfAbsent(id, k -> new ArrayList<>()));
        List<bounty> ret = new ArrayList<>();

        for (bounty b : playerBounties) {
            if(b == null)
                continue;

            if(isBountyExpired(b, id)) {
                transferExpiredBounty(b, id);
                continue;
            }
            ret.add(b);
        }
        return ret;
    }

    public List<bounty> fetchBountiesSilent(UUID id){
        return bounties.computeIfAbsent(id, k -> new ArrayList<>());
    }

    public byte isBountyValid(bounty b){
        DebugLog.info("Checking black/whitelist for " + b.payment.getType().name());
        if(b.payment == null)
            return 0;

        byte ret = 0;

        if(CONFIG.getBool("enable-blacklist")){
            if(BlackList.isFiltered(b.payment))
                ret = 1;
        }
        if(CONFIG.getBool("enable-whitelist")){
            if(!WhiteList.isFiltered(b.payment))
                ret = 2;
        }

        return ret;
    }

    public void checkAllExpiredBounties(){
        for(UUID key : bounties.keySet()){
            fetchBounties(key);
        }
    }

    public boolean isBountyExpired(bounty b, UUID id){
        DebugLog.info("Checking if " + b + " of Player " + id + " is expired");

        if(!CONFIG.getBool("bounties-expire")){
            DebugLog.info("Bounty expiring disabled, defaulting to false");
            return false;
        }

        long setAt;
        ItemMeta meta = b.payment.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        if(!dataContainer.has(timeKey, PersistentDataType.LONG)){
            DebugLog.warn("No set time found, setting to current Time");
            dataContainer.set(timeKey, PersistentDataType.LONG, System.currentTimeMillis());
        }
        if(dataContainer.has(timeKey, PersistentDataType.LONG)){
            try{
                setAt = dataContainer.get(timeKey, PersistentDataType.LONG);
                long diff = System.currentTimeMillis() - setAt;
                if(diff >= CONFIG.toMillisecTime(CONFIG.getString("bounty-expire-time"))){
                    DebugLog.info("Bounty " + b + " from " + id.toString() + " was expired, returning true");
                    return true;
                }
                DebugLog.info("Bounty " + b + " from " + id.toString() + " was not expired, returning false");
                return false;
            }
            catch (Exception e){
                DebugLog.error("Exception (NullPointer) while trying to get setAtTime from PersistentDataContainer of bounty " + b + " of player " + id.toString());
                DebugLog.error(" ==> " + e.getMessage());
                e.printStackTrace();
            }
        }
        DebugLog.error("No set time information found for " + b + " from " + id.toString() + ", defaulting to false");
        return false;
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
                        .replace("[AMOUNT]", Vault.format(toAdd.moneyPayment))
                        .replace("[TARGET]", Bukkit.getPlayer(id).getName()));
            }
            else{
                if(setter != null)
                    Bukkit.getPlayer(setter).sendMessage(LANG.getText("eco-bounty-set")
                        .replace("[AMOUNT]", Vault.format(toAdd.moneyPayment))
                        .replace("[TARGET]", Bukkit.getPlayer(id).getName()));
            }

            ecoBounty = getEcoBounty(id);

            if(ecoBounty == null)
                return;

            for(String s : COMMANDCONFIG.getStringList("on-set-eco-bounty")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', s
                        .replace("[TARGET]", Bukkit.getOfflinePlayer(id).getName())
                        .replace("[SETTER]", Bukkit.getPlayer(setter).getName())
                        .replace("[AMOUNT]", ""+toAdd.moneyPayment)));
            }

            fetchPlayer(id).ecoWorth = ecoBounty.moneyPayment;
            if(ecoMostWorth == null || fetchPlayer(id).ecoWorth >= ecoMostWorth.ecoWorth){
                ecoMostWorth = fetchPlayer(id);
            }
        }
        else{
            ItemMeta meta = toAdd.payment.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            dataContainer.set(dataKey, PersistentDataType.STRING, setter != null ? setter.toString() : "CONSOLE");
            dataContainer.set(timeKey, PersistentDataType.LONG, System.currentTimeMillis());
            toAdd.payment.setItemMeta(meta);
            bounties.computeIfAbsent(id, k -> new ArrayList<>()).add(toAdd);

            for(String s : COMMANDCONFIG.getStringList("on-set-bounty")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', s
                        .replace("[TARGET]", Bukkit.getOfflinePlayer(id).getName())
                        .replace("[SETTER]", Bukkit.getPlayer(setter).getName())
                        .replace("[AMOUNT]", ""+toAdd.payment.getAmount())
                        .replace("[ITEM]", toAdd.payment.getType().name())));
            }

            if(CONFIG.getBool("bounty-set-global")){
                chatManager.bountySet(Bukkit.getPlayer(id), Bukkit.getPlayer(setter), toAdd.payment);
            }
            else{
                if(setter != null)
                    Bukkit.getPlayer(setter).sendMessage(LANG.getText("bounty-set")
                            .replace("[AMOUNT]", ""+ toAdd.payment.getAmount())
                            .replace("[ITEM]", toAdd.payment.getType().name())
                            .replace("[TARGET]", Bukkit.getPlayer(id).getName()));
            }
        }

        DebugLog.info((setter != null ? Bukkit.getPlayer(setter).getName() : "CONSOLE") + " set a bounty on " + Bukkit.getPlayer(id).getName());

        fetchPlayer(id).onGetSetOn();
    }

    public ItemStack cleanBountyItem(bounty b){
        ItemStack toDrop = b.payment.clone();
        ItemMeta meta = toDrop.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if(container.has(LuckyBounties.I.dataKey, PersistentDataType.STRING)){
            if(container.has(LuckyBounties.I.timeKey, PersistentDataType.LONG)){
                container.remove(LuckyBounties.I.timeKey);
            }
            container.remove(LuckyBounties.I.dataKey);
            toDrop.setItemMeta(meta);
        }
        return toDrop;
    }

    public void clearBounties(UUID id){

        if(CONFIG.getBool("return-bounties-when-cleared")){
            for(bounty b : fetchBounties(id)){
                transferExpiredBounty(b, id, (byte)1);
            }
        }

        bounties.computeIfAbsent(id, k -> new ArrayList<>()).clear();
        fetchPlayer(id).onDeath();

        getHighestEcoBounty();
    }

    public bounty getEcoBounty(UUID id){

        if(Vault == null)
            return null;

        for(bounty b : fetchBountiesSilent(id)){
            if(b.moneyPayment > 0)
                return b;
        }

        return null;
    }

    public void removeBounty(UUID id, int index){
        fetchPlayer(id).onRemoved();
        bounties.computeIfAbsent(id, k -> new ArrayList<>()).remove(index);
    }

    public  void removeBounty(UUID id, bounty b){
        bounties.computeIfAbsent(id, k -> new ArrayList<>()).remove(b);
        fetchPlayer(id).onRemoved();
    }

    public  void removeBountySilent(UUID id, bounty b){
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

    //region returnBuffer
    private void transferExpiredBounty(bounty b, UUID targetID){
        ItemMeta meta = b.payment.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        String setter = dataContainer.get(dataKey, PersistentDataType.STRING);
        if(Objects.equals(setter, "CONSOLE") || Objects.equals(setter, "_NULL") || !CONFIG.getBool("return-after-expire")){
            removeBountySilent(targetID, b);
            return;
        }
        UUID setterID = UUID.fromString(setter);

        Player setterP = Bukkit.getPlayer(setterID);
        if(setterP != null){

            setterP.sendMessage(LANG.getText("bounty-expired")
                    .replace("[AMOUNT]", ""+b.payment.getAmount())
                    .replace("[ITEM]", ""+b.payment.getType())
                    .replace("[TARGET]", ""+Bukkit.getOfflinePlayer(targetID).getName()));

            if(setterP.getInventory().firstEmpty() == -1){
                setterP.getWorld().dropItemNaturally(setterP.getLocation(), LuckyBounties.I.cleanBountyItem(b));
            }
            else{
                setterP.getInventory().addItem(LuckyBounties.I.cleanBountyItem(b));
            }

            removeBountySilent(targetID, b);
            return;
        }

        dataContainer.set(dataKey, PersistentDataType.STRING, targetID.toString());
        b.payment.setItemMeta(meta);
        returnBuffer.computeIfAbsent(setterID, k -> new ArrayList<>()).add(b);
        removeBountySilent(targetID, b);
    }

    //This one is for when clear button is pressed (really bad style, but it works, sooooo...)
    private void transferExpiredBounty(bounty b, UUID targetID, byte context){
        ItemMeta meta = b.payment.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        String setter = dataContainer.get(dataKey, PersistentDataType.STRING);
        if(Objects.equals(setter, "CONSOLE") || Objects.equals(setter, "_NULL")){
            removeBountySilent(targetID, b);
            return;
        }
        UUID setterID = UUID.fromString(setter);

        Player setterP = Bukkit.getPlayer(setterID);
        if(setterP != null){

            setterP.sendMessage(LANG.getText("bounty-expired")
                    .replace("[AMOUNT]", ""+b.payment.getAmount())
                    .replace("[ITEM]", ""+b.payment.getType())
                    .replace("[TARGET]", ""+Bukkit.getOfflinePlayer(targetID).getName()));

            if(setterP.getInventory().firstEmpty() == -1){
                setterP.getWorld().dropItemNaturally(setterP.getLocation(), LuckyBounties.I.cleanBountyItem(b));
            }
            else{
                setterP.getInventory().addItem(LuckyBounties.I.cleanBountyItem(b));
            }

            removeBountySilent(targetID, b);
            return;
        }

        dataContainer.set(dataKey, PersistentDataType.STRING, targetID.toString());
        b.payment.setItemMeta(meta);
        returnBuffer.computeIfAbsent(setterID, k -> new ArrayList<>()).add(b);
        removeBountySilent(targetID, b);
    }

    public List<bounty> fetchBuffer(UUID id){
        return returnBuffer.computeIfAbsent(id, k -> new ArrayList<>());
    }

    public void removeFromBuffer(bounty b, UUID id){
        returnBuffer.computeIfAbsent(id, k -> new ArrayList<>()).add(b);
    }

    public void removeBuffer(UUID id){
        returnBuffer.remove(id);
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

    public Player getHighestEcoBountyOnline(){
        if(getServer().getOnlinePlayers().size() == 0)
            return null;

        Player highestEco = null;
        float highestEcoAmount = -1;
        for(Player p : getServer().getOnlinePlayers()){
            float playerBounty = getEcoBounty(p.getUniqueId()).moneyPayment;
            if(playerBounty > highestEcoAmount){
                highestEco = p;
                highestEcoAmount = playerBounty;
            }
        }

        if(highestEcoAmount == 0)
            return null;

        return highestEco;
    }

    public Player getHighestBountyOnline(){
        if(getServer().getOnlinePlayers().size() == 0)
            return null;

        Player highestBounty = null;
        int highestBountyAmount = -1;
        for(Player p : getServer().getOnlinePlayers()){
            int playerBounty = fetchPlayer(p.getUniqueId()).worth;
            if(playerBounty > highestBountyAmount){
                highestBounty = p;
                highestBountyAmount = playerBounty;
            }
        }

        if(highestBountyAmount == 0)
            return null;

        return highestBounty;
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

    public Player[] getVisiblePlayers(Player sender){
        ArrayList<Player> onlinePlayers = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()){
            if(!sender.canSee(p))
                continue;
            onlinePlayers.add(p);
        }
        return onlinePlayers.toArray(Player[]::new);
    }

    public playerData fetchPlayer(UUID uuid){
        return players.computeIfAbsent(uuid, k -> new playerData(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName(), uuid));
    }
    //endregion

    //region API-connection
    public void callEvent(BountiesEvent e){
        Bukkit.getServer().getPluginManager().callEvent(e);
    }
    //endregion
}
