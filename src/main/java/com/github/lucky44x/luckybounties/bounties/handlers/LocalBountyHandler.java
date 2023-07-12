package com.github.lucky44x.luckybounties.bounties.handlers;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.bounties.BountyHandler;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.bounties.types.ItemBounty;
import com.github.lucky44x.luckybounties.user.UserStats;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.*;

/*
This method of saving the bounties in the RAM has a bunch of redundancy, which is cancer in itself BUT:
Due to saving all EcoBounties in the targetBountiesList AND the setterBounties it allows for rapid search and finding of bounties
whereas before, the program would have had to search all bounties in the targetBountiesMap in order to find all bounties of one setter
 */
public class LocalBountyHandler extends BountyHandler {

    @Getter
    private final HashMap<UUID, ArrayList<Bounty>> targetBounties;
    private final HashMap<UUID, ArrayList<Bounty>> setterBounties;
    @Getter
    private final HashMap<UUID, ArrayList<Bounty>> returnBuffer;
    @Getter
    private final HashMap<UUID, UserStats> userStats;

    private final List<EcoBounty> ecoBounties;

    public LocalBountyHandler(LuckyBounties instance) {
        super(instance);

        targetBounties = new HashMap<>();
        setterBounties = new HashMap<>();
        returnBuffer = new HashMap<>();
        userStats = new HashMap<>();
        ecoBounties = new ArrayList<>();

        finishInit();
    }

    private ArrayList<Bounty> getTargetBountyList(UUID target){
        return targetBounties.computeIfAbsent(target, l -> new ArrayList<>());
    }

    private ArrayList<Bounty> getSetterBountyList(UUID setter){
        return setterBounties.computeIfAbsent(setter, l -> new ArrayList<>());
    }

    @Override
    public Bounty[] getBountiesByTarget(UUID target) {
        return getTargetBountyList(target).toArray(Bounty[]::new);
    }

    @Override
    public Bounty[] getBountiesBySetter(UUID setter) {
        return getSetterBountyList(setter).toArray(Bounty[]::new);
    }

    @Override
    public double getEcoAmount(UUID target) {
        double totalAmount = 0;
        for(Bounty b : getBountiesByTarget(target)){
            if(b instanceof EcoBounty)
                totalAmount += ((EcoBounty)b).getReward();
        }

        return totalAmount;
    }

    @Override
    public UUID[] getAllTargets() {
        return targetBounties.keySet().toArray(new UUID[0]);
    }

    @Override
    public UUID[] getAllUsers() {
        return userStats.keySet().toArray(new UUID[0]);
    }

    @Override
    public int getGlobalBountyNum() {
        int totalAmount = 0;
        for(UUID id : targetBounties.keySet()){
            totalAmount += getTargetBountyList(id).size();
        }
        return totalAmount;
    }


    @Override
    public int getMaxBountyNum() {
        int maxAmount = 0;

        for(UUID id : targetBounties.keySet()){
            Bounty[] tmpBounties = getBountiesByTarget(id);
            if(tmpBounties.length > maxAmount)
                maxAmount = tmpBounties.length;
        }

        return maxAmount;
    }

    @Override
    public String getMaxBountyName() {
        UUID maxBountyID = null;
        int maxBountyNum = 0;
        for(UUID id : targetBounties.keySet()){
            Bounty[] tmp = getBountiesByTarget(id);
            if(tmp.length > maxBountyNum){
                maxBountyNum = tmp.length;
                maxBountyID = id;
            }
        }
        if(maxBountyID == null)
            return "NAN";

        return Bukkit.getOfflinePlayer(maxBountyID).getName();
    }

    @Override
    public String getMaxEcoBountyName() {
        UUID maxBountyID = null;
        double maxAmount = 0;

        for(UUID id : targetBounties.keySet()){
            Bounty[] tmp = getBountiesByTarget(id);
            double tmpAmount = 0;
            for(Bounty b : tmp){
                if(!(b instanceof EcoBounty))
                    continue;

                tmpAmount += ((EcoBounty)b).getReward();
            }
            if(tmpAmount > maxAmount){
                maxBountyID = id;
                maxAmount = tmpAmount;
            }
        }

        if(maxBountyID == null)
            return "NAN";

        return Bukkit.getOfflinePlayer(maxBountyID).getName();
    }

    @Override
    public double getMaxEcoBountyAmount() {
        double maxEcoAmount = 0;

        for(UUID id : targetBounties.keySet()){
            Bounty[] tmpBounties = getBountiesByTarget(id);
            int ecoNum = 0;
            for(Bounty b : tmpBounties){
                if(!(b instanceof EcoBounty))
                    continue;

                ecoNum++;
            }
            if(ecoNum > maxEcoAmount)
                maxEcoAmount = ecoNum;
        }

        return maxEcoAmount;
    }

    @Override
    public double getGlobalEcoAmount() {
        double globalWorth = 0;
        for(EcoBounty b : ecoBounties){
            globalWorth += b.getReward();
        }
        return globalWorth;
    }

    @Override
    public void checkForExpiredTargetBounties(Player target) {
        if(!instance.configFile.isBountiesExpire())
            return;

        long maxLifeTime = instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime());

        Bounty[] bounties = getBountiesByTarget(target);
        for(Bounty b : bounties){
            if(System.currentTimeMillis() - b.getSetTime() >= maxLifeTime)
                b.returnBounty();
        }
    }

    @Override
    public void checkForExpiredBounties() {
        if(!instance.configFile.isBountiesExpire())
            return;

        long maxLifeTime = instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime());

        for(UUID uuid : targetBounties.keySet()){
            Bounty[] bounties = getBountiesByTarget(uuid);
            for(Bounty b : bounties){
                if(System.currentTimeMillis() - b.getSetTime() >= maxLifeTime)
                    b.returnBounty();
            }
        }
    }

    @Override
    public void checkForExpiredSetterBounties(Player setter) {
        if(!instance.configFile.isBountiesExpire())
            return;

        long maxLifeTime = instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime());

        Bounty[] bounties = getBountiesBySetter(setter);
        for(Bounty b : bounties){
            if(System.currentTimeMillis() - b.getSetTime() >= maxLifeTime)
                b.returnBounty();
        }
    }

    @Override
    public Bounty[] getReturnBuffer(UUID user) {
        return returnBuffer.computeIfAbsent(user, l -> new ArrayList<>()).toArray(Bounty[]::new);
    }

    @Override
    public void addBounty(Bounty bounty) {
        if(bounty instanceof EcoBounty)
            ecoBounties.add((EcoBounty) bounty);

        getSetterBountyList(bounty.getSetterID()).add(bounty);
        getTargetBountyList(bounty.getTargetID()).add(bounty);
    }

    @Override
    public boolean removeBounty(Bounty bounty) {
        if(bounty instanceof EcoBounty)
            ecoBounties.remove((EcoBounty) bounty);

        getSetterBountyList(bounty.getSetterID()).remove(bounty);
        getTargetBountyList(bounty.getTargetID()).remove(bounty);
        return true;
    }

    @Override
    public void moveBountyToReturn(Bounty bounty) {
        if(!getTargetBountyList(bounty.getTargetID()).contains(bounty))
            return;

        if(bounty instanceof EcoBounty)
            ecoBounties.remove((EcoBounty) bounty);

        getTargetBountyList(bounty.getTargetID()).remove(bounty);
        getSetterBountyList(bounty.getSetterID()).remove(bounty);

        returnBuffer.computeIfAbsent(bounty.getSetterID(), l -> new ArrayList<>()).add(bounty);
    }

    @Override
    public void addBounty(ItemStack payment, Player target, Player setter) {
        addBounty(
                new ItemBounty(payment, target, setter, instance)
        );
    }

    @Override
    public void addBounty(double payment, Player target, Player setter) {
        addBounty(
                new EcoBounty(payment, target, setter, instance)
        );
    }

    @Override
    public void clearBounties(Player target) {
        Bounty[] toRemove = getBountiesByTarget(target);

        for(Bounty b : toRemove){
            if(instance.configFile.isReturnRemovedBounties()){
                b.returnBounty();
                continue;
            }

            getTargetBountyList(target.getUniqueId()).remove(b);
            getSetterBountyList(b.getSetterID()).remove(b);
            if(b instanceof EcoBounty)
                ecoBounties.remove(b);
        }
    }

    @Override
    public void clearReturnBuffer(UUID target) {
        returnBuffer.computeIfAbsent(target, l -> new ArrayList<>()).clear();
    }

    @Override
    protected UserStats getUserStats(UUID id) {
        return userStats.computeIfAbsent(id, u -> new UserStats(0,0,0));
    }

    @Override
    public UUID getUserMaxBountiesTaken() {
        UserStats max = null;
        UUID user = null;
        for(Map.Entry<UUID, UserStats> entry : userStats.entrySet()){
            if(max == null || entry.getValue().getBountiesTaken() > max.getBountiesTaken()){
                max = entry.getValue();
                user = entry.getKey();
            }
        }

        return user;
    }

    @Override
    public UUID getUserMaxBountiesReceived() {
        UserStats max = null;
        UUID user = null;
        for(Map.Entry<UUID, UserStats> entry : userStats.entrySet()){
            if(max == null || entry.getValue().getBountiesReceived() > max.getBountiesReceived()){
                max = entry.getValue();
                user = entry.getKey();
            }
        }

        return user;
    }

    @Override
    public UUID getUserMaxBountiesSet() {
        UserStats max = null;
        UUID user = null;
        for(Map.Entry<UUID, UserStats> entry : userStats.entrySet()){
            if(max == null || entry.getValue().getBountiesSet() > max.getBountiesSet()){
                max = entry.getValue();
                user = entry.getKey();
            }
        }

        return user;
    }

    @Override
    public void resetStats(UUID user) {
        userStats.put(user, new UserStats(0,0,0));
    }

    @Override
    public void addStatTaken(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesTaken(stats.getBountiesTaken() + 1);
    }

    @Override
    public void subtractStatTaken(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesTaken(stats.getBountiesTaken() - 1);
        if(stats.getBountiesTaken() < 0)
            stats.setBountiesTaken(0);
    }

    @Override
    public void removeStatTaken(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesTaken(0);
    }

    @Override
    public void addStatSet(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesSet(stats.getBountiesSet() + 1);
    }

    @Override
    public void subtractStatSet(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesSet(stats.getBountiesSet() - 1);
        if(stats.getBountiesSet() < 0)
            stats.setBountiesSet(0);
    }

    @Override
    public void removeStatSet(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesSet(0);
    }

    @Override
    public void addStatReceived(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesReceived(stats.getBountiesSet() + 1);
    }

    @Override
    public void subtractStatReceived(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesReceived(stats.getBountiesReceived() - 1);
        if(stats.getBountiesReceived() < 0)
            stats.setBountiesReceived(0);
    }

    @Override
    public void removeStatReceived(UUID target) {
        UserStats stats = getUserStats(target);

        stats.setBountiesReceived(0);
    }

    @Override
    public void insertUser(UUID target, UserStats stats) {
        userStats.put(target, stats);
    }

    @Override
    protected void onLoad() {
        instance.getLogger().info("[LocalBountyHandler] Loading data from folder-structure");
        File folder = new File(instance.getDataFolder() + "/bounties");
        if(!folder.exists())
            return;

        if(folder.listFiles(File::isDirectory) == null)
            return;

        for(File userFolder : folder.listFiles(File::isDirectory)){
            UUID userUUID = UUID.fromString(userFolder.getName());

            File activeBounties = new File(userFolder.getAbsolutePath()+"/active.bounties");
            File returnBounties = new File(userFolder.getAbsolutePath()+"/returnBuffer.bounties");
            File userStatsFile = new File(userFolder.getAbsolutePath()+"/statistics.json");

            try{
                for(Bounty b : decodeBounties(activeBounties)){
                    UUID setterID = b.getSetterID();
                    UUID targetID = b.getTargetID();

                    getTargetBountyList(targetID).add(b);
                    getSetterBountyList(setterID).add(b);
                }

                returnBuffer.computeIfAbsent(userUUID, l -> new ArrayList<>()).addAll(decodeBounties(returnBounties));
                userStats.put(userUUID, readUserStats(userStatsFile));

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private UserStats readUserStats(File f){
        if(!f.exists())
            return UserStats.EMPTY;

        try(InputStreamReader reader = new InputStreamReader(new FileInputStream(f))){
            JsonObject mainObject = JsonParser.parseReader(reader).getAsJsonObject();

            int received = mainObject.get("receivedBounties").getAsInt();
            int set = mainObject.get("setBounties").getAsInt();
            int taken = mainObject.get("takenBounties").getAsInt();

            return new UserStats(received, set, taken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Bounty> decodeBounties(File f) throws IOException, ClassNotFoundException {
        ArrayList<Bounty> bounties = new ArrayList<>();
        if(!f.exists())
            return bounties;

        InputStream fileStream = new FileInputStream(f);
        String data = readFromInputStream(fileStream);
        fileStream.close();

        ByteArrayInputStream dataStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream objectStream = new BukkitObjectInputStream(dataStream);

        int bountiesNum = objectStream.readInt();

        for(int i = 0; i < bountiesNum; i++){
            boolean isItem = objectStream.readBoolean();
            UUID targetID = UUID.fromString(objectStream.readUTF());
            UUID setterID = UUID.fromString(objectStream.readUTF());
            long setTime = objectStream.readLong();

            if(isItem){
                ItemStack payment = (ItemStack) objectStream.readObject();
                bounties.add(new ItemBounty(payment, targetID, setterID, setTime, instance));
            }
            else{
                double payment = objectStream.readDouble();
                bounties.add(new EcoBounty(payment, targetID, setterID, setTime, instance));
            }
        }

        return bounties;
    }

    private void encodeBounties(ArrayList<Bounty> bounties, File f){
        try{
            if(!f.exists()) {
                f.getParentFile().mkdirs();
            }
            else{
                f.delete();
            }

            f.createNewFile();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(bounties.size());
            for(Bounty b : bounties){
                dataOutput.writeBoolean((b instanceof ItemBounty));
                dataOutput.writeUTF(b.getTargetID().toString());
                if(b.getSetterID() != null)
                    dataOutput.writeUTF(b.getSetterStringID());
                dataOutput.writeLong(b.getSetTime());

                if(b instanceof ItemBounty)
                    dataOutput.writeObject(((ItemBounty) b).getReward());
                else if(b instanceof EcoBounty)
                    dataOutput.writeDouble(((EcoBounty) b).getReward());
            }

            dataOutput.close();

            FileWriter fileWriter = new FileWriter(f);
            PrintWriter pW = new PrintWriter(fileWriter);
            pW.print(new String(Base64Coder.encodeLines(outputStream.toByteArray())));
            pW.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeStats(UserStats stats, File f){
        try{
            if(!f.exists())
                f.getParentFile().mkdirs();
            else
                f.delete();

            f.createNewFile();

            try(JsonWriter writer = new JsonWriter(new FileWriter(f))){
                writer.beginObject();
                writer.setIndent("   ");
                writer.name("recievedBounties");
                writer.value(stats.getBountiesReceived());
                writer.name("setBounties");
                writer.value(stats.getBountiesSet());
                writer.name("takenBounties");
                writer.value(stats.getBountiesTaken());

                writer.setIndent("");
                writer.endObject();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onSave() {
        instance.getLogger().info("[LocalBountyHandler] Saving data in folder-structure");
        File folder = new File(instance.getDataFolder() + "/bounties");
        if(!folder.exists()){
            folder.mkdirs();
        }

        for(UUID target : targetBounties.keySet()){
            File targetFile = new File(instance.getDataFolder() + "/bounties/" + target.toString() + "/active.bounties");
            encodeBounties(getTargetBountyList(target), targetFile);
        }

        for(UUID setter : returnBuffer.keySet()){
            File returnFile = new File(instance.getDataFolder() + "/bounties/" + setter.toString() + "/returnBuffer.bounties");
            encodeBounties(
                    returnBuffer.computeIfAbsent(setter, l -> new ArrayList<>()),
                    returnFile
            );
        }

        for(UUID user : userStats.keySet()){
            File userStatFile = new File(instance.getDataFolder() + "/bounties/" + user.toString() + "/stats.json");
            UserStats stats = userStats.get(user);
            writeStats(stats, userStatFile);
        }
    }

    @Override
    public void dropData() {
        setterBounties.clear();
        targetBounties.clear();
        userStats.clear();
        returnBuffer.clear();
    }

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

    public void addReturnBounty(Bounty b) {
        returnBuffer.computeIfAbsent(b.getSetterID(), k -> new ArrayList<>()).add(b);
    }
}
