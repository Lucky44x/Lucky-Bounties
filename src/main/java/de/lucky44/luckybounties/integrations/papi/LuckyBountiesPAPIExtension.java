package de.lucky44.luckybounties.integrations.papi;

import de.lucky44.luckybounties.LuckyBounties;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LuckyBountiesPAPIExtension extends PlaceholderExpansion {
    //region settings
    @Override
    public @NotNull String getIdentifier() { return "lb"; }

    @Override
    public @NotNull String getAuthor() { return "Lucky44"; }

    @Override
    public @NotNull String getVersion() { return "1.0.4"; }

    @Override
    public boolean persist(){ return true; }
    //endregion

    private final LuckyBounties plugin;

    public LuckyBountiesPAPIExtension(){
        plugin = LuckyBounties.I;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params){
        String query = params.toLowerCase();
        String ret = "NAN";

        switch (query) {
            case "collected" -> ret = "" + LuckyBounties.I.fetchPlayer(player.getUniqueId()).collected;
            case "bounty" -> ret = "" + LuckyBounties.I.fetchPlayer(player.getUniqueId()).worth;
            case "top_bounty" -> ret = LuckyBounties.mostWorth == null ? "No one" : LuckyBounties.mostWorth.playerName;
            case "top_col" -> ret = LuckyBounties.mostCollected == null ? "No one" : LuckyBounties.mostCollected.playerName;
            case "top_eco_bounty" -> ret = LuckyBounties.ecoMostWorth == null ? "No one" : LuckyBounties.ecoMostWorth.playerName;
        }

        return ret;
    }
}
