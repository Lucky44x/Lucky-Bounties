package com.github.lucky44x.luckybounties.migration;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.bounties.handlers.LocalBountyHandler;
import com.github.lucky44x.luckybounties.bounties.handlers.PooledSQLBountyHandler;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.bounties.types.ItemBounty;
import com.github.lucky44x.luckybounties.user.UserStats;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.*;

/**
 * @author Lucky44x
 * Migrator class for automatically upgrading 2.0 data to the new 3.0 standart
 */
public class Migrator {

    private final LuckyBounties instance;
    @Getter
    private boolean hasOldData = false;
    private final File oldDataFolder;

    private final NamespacedKey dataKey;
    private final NamespacedKey timeKey;

    public Migrator(LuckyBounties instance){
        this.instance = instance;

        dataKey = new NamespacedKey(instance, "lbData");
        timeKey = new NamespacedKey(instance, "lbTime");

        File directory = new File(instance.getDataFolder().toURI());
        oldDataFolder = new File("plugins\\LuckyBountiesOLD");

        if(oldDataFolder.exists())
            directory = oldDataFolder;

        if(!directory.exists()){
            return;
        }

        File playerData = new File(directory + "\\playerdata.json");
        if(!playerData.exists()){
            File playerDataCapital = new File(directory + "\\playerData.json");
            File commandsFile = new File(directory + "\\commands.yml");
            if(!commandsFile.exists() && !playerDataCapital.exists())
                return;
        }

        instance.getLogger().warning("LuckyBounties has detected old bounty-data from plugin version 2.7.3 or prior... The folder LuckyBounties will be renamed to \"LuckyBountiesOLD\", please make a backup of the data, and when ready, execute the \"/lb update bounties\" command");

        if(directory != oldDataFolder){
            if(!directory.renameTo(oldDataFolder))
                instance.getLogger().warning("Could not rename old LuckyBounties folder which can result in errors occuring, please manually rename the folder to \"LuckyBountiesOLD\" and restart before trying to migrate version 2.0 data to newer versions");
        }
        hasOldData = true;
    }

    /**
     * Confirms the decision to migrate all data. Is called via command, and essentially logs progress through chat
     * @param sender the CommandSender which to send logs to
     */
    public void migrate(CommandSender sender){

        sender.sendMessage(ChatColor.GOLD + "Trying to transfer old bounty-data");

        try{
            sender.sendMessage(ChatColor.YELLOW + "Transferring user-data");
            for(Map.Entry<UUID, UserStats> statsEntry : readUsers().entrySet()){
                instance.getHandler().insertUser(statsEntry.getKey(), statsEntry.getValue());
            }

            sender.sendMessage(ChatColor.YELLOW + "Transferring bounties");
            for(Bounty b : readBounties()){
                if(instance.getHandler() instanceof PooledSQLBountyHandler){
                    ((PooledSQLBountyHandler)instance.getHandler()).addInternalBounty(b, true);
                    continue;
                }

                instance.getHandler().addBounty(b);
            }

            sender.sendMessage(ChatColor.YELLOW + "Transferring return-bounties");
            for(Bounty b : readReturnBuffer()){
                if(instance.getHandler() instanceof PooledSQLBountyHandler){
                    ((PooledSQLBountyHandler)instance.getHandler()).addInternalBounty(b, false);
                    continue;
                }

                ((LocalBountyHandler)instance.getHandler()).addReturnBounty(b);
            }

            sender.sendMessage(ChatColor.GREEN + "Transferred old bounty-, user-, and return-buffer-data from version 2.7.3 to 3.x");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * reads all bounties from the old data-folder
     * @return an array of upgraded Bounties
     * @throws IOException when something goes wrong
     * @throws ClassNotFoundException ? (idk when this gets thrown...)
     */
    private Bounty[] readBounties() throws IOException, ClassNotFoundException {
        List<Bounty> bounties = new ArrayList<>();

        File folder = new File("plugins\\LuckyBountiesOLD\\bounties");
        if(!folder.exists())
            return bounties.toArray(Bounty[]::new);

        File[] allFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".bounties") && !name.toLowerCase().contains(".return-buffer");
            }
        });

        for(File file : allFiles){
            UUID target = UUID.fromString(file.getName().split("\\.")[0]);

            InputStream stream = new FileInputStream(file);
            String data = readFromInputStream(stream);
            stream.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();
            float moneyBounty = dataInput.readFloat();
            if(moneyBounty != -1){
                bounties.add(new EcoBounty(moneyBounty, target, instance.getServerUUID(), System.currentTimeMillis(), instance));
            }

            for(int i = 0; i < size; i++){
                ItemStack item = (ItemStack)dataInput.readObject();
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                long setTime = 0L;
                String setter = "";

                UUID setterID = instance.getServerUUID();

                if(container.has(dataKey, PersistentDataType.STRING)){
                    setter = container.get(dataKey, PersistentDataType.STRING);

                    if(!Objects.equals(setter, "CONSOLE")){
                        setterID = UUID.fromString(setter);
                    }

                    container.remove(dataKey);
                    if(container.has(timeKey, PersistentDataType.LONG)){
                        setTime = container.get(timeKey, PersistentDataType.LONG);
                        container.remove(timeKey);
                    }
                    item.setItemMeta(meta);
                }

                bounties.add(new ItemBounty(item, target, setterID, setTime, instance));
            }

            dataInput.close();
        }

        return bounties.toArray(Bounty[]::new);
    }

    /**
     * Upgrades the return buffer stuff
     * @return an array of upgraded bounties which were saved as the return buffer
     * @throws IOException when the file is not found
     * @throws ClassNotFoundException (idk)
     */
    private Bounty[] readReturnBuffer() throws IOException, ClassNotFoundException {
        List<Bounty> bounties = new ArrayList<>();

        File folder = new File("plugins\\LuckyBountiesOLD\\bounties");
        if(!folder.exists())
            return bounties.toArray(Bounty[]::new);

        File[] allFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".return-buffer.bounties");
            }
        });

        for(File file : allFiles){
            UUID target = UUID.fromString(file.getName().split("\\.")[0]);

            InputStream stream = new FileInputStream(file);
            String data = readFromInputStream(stream);
            stream.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();
            float moneyBounty = dataInput.readFloat();
            if(moneyBounty != -1){
                bounties.add(new EcoBounty(moneyBounty, target, instance.getServerUUID(), System.currentTimeMillis(), instance));
            }

            for(int i = 0; i < size; i++){
                ItemStack item = (ItemStack)dataInput.readObject();
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                long setTime = 0L;
                String setter = "";

                UUID setterID = instance.getServerUUID();

                if(container.has(dataKey, PersistentDataType.STRING)){
                    setter = container.get(dataKey, PersistentDataType.STRING);

                    if(!Objects.equals(setter, "CONSOLE")){
                        setterID = UUID.fromString(setter);
                    }

                    container.remove(dataKey);
                    if(container.has(timeKey, PersistentDataType.LONG)){
                        setTime = container.get(timeKey, PersistentDataType.LONG);
                        container.remove(timeKey);
                    }
                    item.setItemMeta(meta);
                }

                bounties.add(new ItemBounty(item, target, setterID, setTime, instance));
            }

            dataInput.close();
        }

        return bounties.toArray(Bounty[]::new);
    }

    /**
     * @return a map of UserStats mapped to UUIDs
     */
    private Map<UUID, UserStats> readUsers(){
        Map<UUID, UserStats> stats = new HashMap<>();

        if(!(new File("plugins\\LuckyBountiesOLD\\playerData.json")).exists())
            return stats;

        JsonArray array = getObject();

        for(int i = 0; i < array.size(); i++){
            JsonObject subObject = array.get(i).getAsJsonObject();
            int taken = subObject.get("collected").getAsInt();
            int set = subObject.get("set").getAsInt();
            int received = subObject.get("worth").getAsInt();
            String uuid = subObject.get("playerUUID").getAsString();

            stats.put(UUID.fromString(uuid), new UserStats(set, received, taken));
        }

        return stats;
    }

    /**
     * Just gets a JSON array
     * @return a JsonArray
     */
    private JsonArray getObject(){
        File playerDataFile = new File("plugins\\LuckyBountiesOLD\\playerData.json");
        try(FileReader reader = new FileReader(playerDataFile)){
            JsonArray mainArray = JsonParser.parseReader(reader).getAsJsonArray();

            return mainArray;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a String from an inputstream
     * @param inputStream the inputstream to be read from
     * @return the read string
     * @throws IOException should the file not exists
     */
    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
