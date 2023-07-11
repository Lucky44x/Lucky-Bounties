package com.github.lucky44x.luckybounties.integration.plugins;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.abstraction.integration.PluginIntegration;
import com.github.lucky44x.luckybounties.abstraction.integration.exception.IntegrationException;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PapiIntegration extends PluginIntegration {
    private PapiExpansion extension;

    public PapiIntegration(LuckyBounties instance) {
        super(instance, "PlaceholderAPI");
    }

    @Override
    public void onEnable() throws IntegrationException {
        super.onEnable();

        this.extension = new PapiExpansion();
        this.extension.register();
    }

    @Override
    public void onDisable() throws IntegrationException {
        super.onDisable();

        this.extension.unregister();
    }

    private class PapiExpansion extends PlaceholderExpansion{

        @Override
        public @NotNull String getIdentifier() {
            return "lb";
        }

        @Override
        public @NotNull String getAuthor() {
            return "Lucky4430";
        }

        @Override
        public @NotNull String getVersion() {
            return instance.getDescription().getVersion();
        }

        @Override
        public boolean persist(){
            return true;
        }

        @Override
        public String onRequest(OfflinePlayer player, String params){
            String query = params.toLowerCase();
            switch(query){
                case ("collected") -> {
                    return String.valueOf(instance.getHandler().getStatBountiesTaken(player.getPlayer()));
                }
                case("set") -> {
                    return String.valueOf(instance.getHandler().getStatBountiesSet(player.getPlayer()));
                }
                case("received") -> {
                    return String.valueOf(instance.getHandler().getStatBountiesReceived(player.getPlayer()));
                }
                case("top_set") -> {
                    return Bukkit.getOfflinePlayer(
                            instance.getHandler().getUserMaxBountiesSet()
                    ).getName();
                }
                case("top_collected") -> {
                    return Bukkit.getOfflinePlayer(
                            instance.getHandler().getUserMaxBountiesTaken()
                    ).getName();
                }
                case("top_received") -> {
                    return Bukkit.getOfflinePlayer(
                            instance.getHandler().getUserMaxBountiesReceived()
                    ).getName();
                }
                case("bounty_amount") -> {
                    return String.valueOf((instance.getHandler().getBountiesByTarget(player.getUniqueId())).length);
                }
                case("bounty_eco_amount") -> {
                    return String.valueOf(instance.getHandler().getEcoAmount(player.getUniqueId()));
                }
                case("max_bounty_amount") -> {
                    return String.valueOf(instance.getHandler().getMaxBountyNum());
                }
                case("max_bounty_eco_amount") -> {
                    return String.valueOf(instance.getHandler().getMaxEcoBountyAmount());
                }
                case("all_bounty_amount") -> {
                    return String.valueOf(instance.getHandler().getGlobalBountyNum());
                }
                case("all_bounty_eco_amount") -> {
                    return String.valueOf(instance.getHandler().getGlobalEcoAmount());
                }
            }
            return "NAN";
        }
    }
}
