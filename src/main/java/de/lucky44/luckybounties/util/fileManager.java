package de.lucky44.luckybounties.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.lucky44.luckybounties.LuckyBounties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class fileManager {

    public static void SaveBounties(bounty[] bounties) throws IOException {
        File save = new File("plugins/LuckyBounties/data.json");

        save.getParentFile().mkdir();
        save.createNewFile();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(bounties);

        FileWriter fw = new FileWriter("plugins/LuckyBounties/data.json");
        PrintWriter pw = new PrintWriter(fw);
        pw.print(json);
        pw.close();
    }

    public static void LoadBounties() throws IOException{
        if(!(new File("plugins/LuckyBounties/data.json").exists()))
            return;

        JsonReader jR = new JsonReader(new FileReader("plugins/LuckyBounties/data.json"));

        Gson gson = new Gson();

        bounty[] bs = gson.fromJson(jR, bounty[].class);

        for(bounty b : bs){

            if(b.type != 1)
                b.payment.convert();
        }

        LuckyBounties.bounties.addAll(Arrays.asList(bs));
    }

    public static void SavePlayers(playerData[] players) throws IOException {
        File save = new File("plugins/LuckyBounties/playerData.json");

        save.getParentFile().mkdir();
        save.createNewFile();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(players);

        FileWriter fw = new FileWriter("plugins/LuckyBounties/playerData.json");
        PrintWriter pw = new PrintWriter(fw);
        pw.print(json);
        pw.close();
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

            if(LuckyBounties.mostCol == null){
                LuckyBounties.mostCol = d;
            }
            else if(LuckyBounties.mostCol.collected < d.collected){
                LuckyBounties.mostCol = d;
            }
            else if(LuckyBounties.mostCol.collected == d.collected && d.lastUpdateCol > LuckyBounties.mostCol.lastUpdateCol){
                LuckyBounties.mostCol = d;
            }

            LuckyBounties.players.computeIfAbsent(UUID.fromString(d.playerUUID), k -> d);
        }
    }
}
