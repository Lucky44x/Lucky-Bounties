package com.github.lucky44x.luckybounties.commands;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.guis.BountiesListGUI;
import com.github.lucky44x.luckybounties.guis.PlayerListGUI;
import com.github.lucky44x.luckybounties.integration.plugins.WorldGuardIntegration;
import com.github.lucky44x.luckyutil.config.LangConfig;
import com.github.lucky44x.luckyutil.numbers.NumberUtilities;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BountiesCommand implements CommandExecutor, TabCompleter {
    private final LuckyBounties instance;

    public BountiesCommand(LuckyBounties instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("bounties"))
            return true;

        if(!(sender instanceof Player p))
            return true;

        if(instance.getIntegrationManager().isIntegrationActive("WGex")){
            if(!instance.getIntegrationManager().getIntegration("WGex", WorldGuardIntegration.class).isLBEnabled((Player) sender)){
                sender.sendMessage(instance.langFile.getText("region-flag-not-allowed", this));
                return true;
            }
        }

        for(CommandExecutor executor : instance.getBridge().getCommandExtensions().keySet()){
            executor.onCommand(sender, cmd, command, args);
        }

        if(args.length == 0){
            try{
                new PlayerListGUI(instance, (Player) sender);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("set")) {

            if(args.length < 2)
                return true;

            if(args.length < 3){
                handleSetItemBounty(args[1], p);
            }
            else{
                handleSetEcoBounty(args[1], args[2], p);
            }
        }

        if(args[0].equalsIgnoreCase("open")){
            if(args.length < 2)
                return true;

            Player target = Bukkit.getPlayer(args[1]);

            if(target == null){
                instance.getChatManager().sendLangMessage("player-not-found", p, new LangStringCarrier(args[1]));
                return true;
            }

            try{
                new BountiesListGUI(instance, p, target);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private void handleSetEcoBounty(String targetName, String amount, Player p){
        if(!instance.getIntegrationManager().isEconomyActive()){
            instance.getChatManager().sendLangMessage("eco-disabled", p);
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if(target == null){
            instance.getChatManager().sendLangMessage("player-not-found", p, new LangStringCarrier(targetName));
            return;
        }

        if(!NumberUtilities.isStringValidFloat(amount)){
            instance.getChatManager().sendLangMessage("eco-not-valid", p, new LangStringCarrier(amount));
            return;
        }

        instance.setBounty(Double.parseDouble(amount), target, p);
    }

    private void handleSetItemBounty(String targetName, Player p){
        if(!instance.configFile.isItemsEnabled()){
            instance.getChatManager().sendLangMessage("items-disabled", p);
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if(target == null){
            instance.getChatManager().sendLangMessage("player-not-found", p, new LangStringCarrier(targetName));
            return;
        }

        ItemStack item = p.getInventory().getItem(p.getInventory().getHeldItemSlot());
        if(item == null){
            instance.getChatManager().sendLangMessage("empty-hand", p);
            return;
        }

        if(instance.setBounty(item, target, p)){
            p.getInventory().removeItem(p.getInventory().getItemInMainHand());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String command, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("bounties"))
            return null;

        List<String> ret = new ArrayList<>();
        List<String> playerNames = new ArrayList<>();

        for(Player p : Bukkit.getOnlinePlayers()){
            playerNames.add(p.getName());
        }

        for(Map.Entry<CommandExecutor, TabCompleter> entry : instance.getBridge().getCommandExtensions().entrySet()){
            if(entry.getValue() == null)
                continue;

            List<String> toAdd = entry.getValue().onTabComplete(sender, cmd, command, args);
            if(toAdd == null)
                continue;

            ret.addAll(toAdd);
        }

        switch(args.length){
            case(1) -> {
                ret.addAll(List.of("set", "open"));
            }
            case(2) -> {
                switch (args[0].toLowerCase()){
                    case("set"):
                    case("open"):
                        ret.addAll(playerNames);
                        break;
                }
            }
        }

        return ret;
    }

    public static class LangStringCarrier {
        @LangConfig.LangData(langKey = "[NAME]")
        public String name = "NAN";
        @LangConfig.LangData(langKey = "[INPUT]")
        public String input = "NAN";

        public LangStringCarrier(String data){
            name = data;
            input = data;
        }
    }
}
