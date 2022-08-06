package de.lucky44.luckybounties.papi;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.playerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class LuckyBountiesPAPIExtension extends PlaceholderExpansion {

    private final LuckyBounties plugin; // The instance is created in the constructor and won't be modified, so it can be final

    public LuckyBountiesPAPIExtension(LuckyBounties plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "Lucky44";
    }

    @Override
    public String getName(){
        return "LuckyBounties_PAPI_Integration";
    }

    @Override
    public String getIdentifier() {
        return "lb";
    }

    @Override
    public String getVersion() {
        return "1.0.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params){
        String query = params.toLowerCase();
        String ret = "NAN";

        switch(query){

            case "collected":
                ret = "" + LuckyBounties.players.computeIfAbsent(player.getUniqueId(), k -> new playerData(player.getName(), player.getUniqueId())).collected;
                break;

            case "bounty":
                ret = "" + LuckyBounties.players.computeIfAbsent(player.getUniqueId(), k -> new playerData(player.getName(), player.getUniqueId())).worth;
                break;

            case "top_bounty":
                String name = "No one";
                if(LuckyBounties.mostWorth != null)
                    name = LuckyBounties.mostWorth.playerName;
                return name;

            case "top_col":
                String colname = "No one";
                if(LuckyBounties.mostCol != null)
                    colname = LuckyBounties.mostCol.playerName;
                return colname;
        }

        return ret;
    }
}
