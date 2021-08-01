package de.lucky44.luckybounties.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.lucky44.luckybounties.LuckyBounties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

        String s = readStream(new FileInputStream("plugins/LuckyBounties/data.json"));

        Gson gson = new Gson();

        bounty[] bs = gson.fromJson(s, bounty[].class);

        for(bounty b : bs){
            b.payment.convert();
        }

        LuckyBounties.bounties.addAll(Arrays.asList(bs));
    }

    public static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
            int c;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        is.close();
        return sb.toString();
    }
}
