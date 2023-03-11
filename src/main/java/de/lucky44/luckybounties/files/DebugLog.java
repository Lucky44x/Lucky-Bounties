package de.lucky44.luckybounties.files;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLog {

    private static FileConfiguration config;
    private static String path;

    public static void info(String text){
        if(!isEnabled())
            return;

        append(getTimeStamp() + " [INFO] " + text);
    }

    public static void warn(String text){
        if(!isEnabled())
            return;

        append(getTimeStamp() + " [WARNING] " + text);
    }

    public static void error(String text){
        if(!isEnabled())
            return;

        append(getTimeStamp() + " [ERROR] " + text);
    }

    public static void init(){
        config = LuckyBounties.I.getConfig();

        if(!isEnabled())
            return;

        Bukkit.getLogger().warning("YOU ARE RUNNING LUCKYBOUNTIES WITH THE DEBUG-LOGGING ON TRUE");

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss");
        path = "debugLog" + formatter.format(date) + ".txt";
        File f = new File("logs/luckyBounties/" + path);
        f.mkdirs();
        if(f.exists()){
            f.delete();
        }
    }

    private static boolean isEnabled(){
        return config.getBoolean("debug-log");
    }

    private static String getTimeStamp(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return "["+formatter.format(date)+"]";
    }

    private static void append(String text){

        if(!isEnabled())
            return;

        try{
            File outputFile = new File("logs/luckyBounties/" + path);
            PrintWriter output;
            output = new PrintWriter(new FileWriter(outputFile, true));
            output.println(text);
            output.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
