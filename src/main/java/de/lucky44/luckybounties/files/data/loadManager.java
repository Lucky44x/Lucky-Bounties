package de.lucky44.luckybounties.files.data;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.deprecated.bountyOld;
import de.lucky44.luckybounties.util.bounty;
import de.lucky44.luckybounties.util.playerData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

public class loadManager {

    public static void loadBounties() throws IOException, ClassNotFoundException {
        File folder = new File("plugins/LuckyBounties/bounties");
        if(!folder.exists())
            return;

        File[] allFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".bounties");
            }
        });

        for(File f : allFiles){
            UUID target = UUID.fromString(f.getName().split("\\.")[0]);

            InputStream stream = new FileInputStream(f);
            String data = readFromInputStream(stream);
            stream.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();

            //LuckyBounties.I.getLogger().info("Loading " + size + " bounties");

            float moneyBounty = dataInput.readFloat();
            if(moneyBounty != -1){
                //LuckyBounties.I.getLogger().info("Found money bounty: " + moneyBounty);
                LuckyBounties.I.bounties.computeIfAbsent(target, k -> new ArrayList<bounty>()).add(new bounty(moneyBounty));
            }

            for(int i = 0; i < size; i++){
                //LuckyBounties.I.getLogger().info("Loading " + i + "th bounty");
                bounty b = new bounty((ItemStack)dataInput.readObject());
                LuckyBounties.I.bounties.computeIfAbsent(target, k -> new ArrayList<bounty>()).add(b);
            }

            dataInput.close();
        }
    }

    public static void LoadOldBounties() throws IOException {
        if(!(new File("plugins/LuckyBounties/data.json").exists()))
            return;

        JsonReader jR = new JsonReader(new FileReader("plugins/LuckyBounties/data.json"));

        Gson gson = new Gson();

        bountyOld[] bs = gson.fromJson(jR, bountyOld[].class);

        for(bountyOld b : bs){
            if(b.type != 1)
                b.payment.convert();
            UUID owner = UUID.fromString(b.UUID);

            bounty transformed = b.type == 1 ? new bounty(b.moneyPayment) : new bounty(b.payment.converted);

            LuckyBounties.I.bounties.computeIfAbsent(owner, k -> new ArrayList<bounty>()).add(transformed);
        }
    }

    public static void LoadPlayers() throws IOException{
        if(!(new File("plugins/LuckyBounties/playerData.json").exists()))
            return;

        JsonReader jR = new JsonReader(new FileReader("plugins/LuckyBounties/playerData.json"));

        Gson gson = new Gson();

        playerData[] players = gson.fromJson(jR, playerData[].class);

        for(playerData d : players){
            if(LuckyBounties.mostWorth == null){
                LuckyBounties.mostWorth = d;
            }
            else if(LuckyBounties.mostWorth.worth < d.worth){
                LuckyBounties.mostWorth = d;
            }
            else if(LuckyBounties.mostWorth.worth == d.worth && d.lastUpdateWor > LuckyBounties.mostWorth.lastUpdateWor){
                LuckyBounties.mostWorth = d;
            }

            if(LuckyBounties.mostCollected == null){
                LuckyBounties.mostCollected = d;
            }
            else if(LuckyBounties.mostCollected.collected < d.collected){
                LuckyBounties.mostCollected = d;
            }
            else if(LuckyBounties.mostCollected.collected == d.collected && d.lastUpdateCol > LuckyBounties.mostCollected.lastUpdateCol){
                LuckyBounties.mostCollected = d;
            }

            LuckyBounties.I.players.computeIfAbsent(UUID.fromString(d.playerUUID), k -> d);
        }
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
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
