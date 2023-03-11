package de.lucky44.luckybounties.files.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.deprecated.bountyOld;
import de.lucky44.luckybounties.util.bounty;
import de.lucky44.luckybounties.util.playerData;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class saveManager {

    public static void SaveReturnBuffer(UUID player, List<bounty> bounties) throws IOException{
        File save = new File("plugins/LuckyBounties/bounties/" + player.toString() + ".return-buffer.bounties");

        File dir = new File("plugins/LuckyBounties/bounties");
        if(!dir.exists()){
            save.getParentFile().mkdir();
        }

        save.createNewFile();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        bounty eco = LuckyBounties.I.getEcoBounty(player);

        int actualSize = bounties.size();
        if(eco != null)
            actualSize--;

        dataOutput.writeInt(actualSize);
        dataOutput.writeFloat(eco == null ? -1 : eco.moneyPayment);
        for(bounty b : bounties){
            if(b.moneyPayment > 0)
                continue;

            dataOutput.writeObject(b.payment);
        }

        dataOutput.close();
        FileWriter fw = new FileWriter(save);
        PrintWriter pw = new PrintWriter(fw);
        pw.print(new String(Base64Coder.encodeLines(outputStream.toByteArray())));
        pw.close();
    }

    public static void SaveBounties(UUID player, List<bounty> bounties) throws IOException {
        File save = new File("plugins/LuckyBounties/bounties/" + player.toString() + ".bounties");

        File dir = new File("plugins/LuckyBounties/bounties");
        if(!dir.exists()){
            save.getParentFile().mkdir();
        }

        save.createNewFile();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        bounty eco = LuckyBounties.I.getEcoBounty(player);

        int actualSize = bounties.size();
        if(eco != null)
            actualSize--;

        dataOutput.writeInt(actualSize);
        dataOutput.writeFloat(eco == null ? -1 : eco.moneyPayment);
        for(bounty b : bounties){
            if(b.moneyPayment > 0)
                continue;

            dataOutput.writeObject(b.payment);
        }

        dataOutput.close();
        FileWriter fw = new FileWriter(save);
        PrintWriter pw = new PrintWriter(fw);
        pw.print(new String(Base64Coder.encodeLines(outputStream.toByteArray())));
        pw.close();
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
}
