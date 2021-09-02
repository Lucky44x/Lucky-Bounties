package de.lucky44.luckybounties.system;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class commandManager implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        //Check if it's our custom command
        if(cmd.getName().equalsIgnoreCase("bounties")){

            Player p = null;
            if(sender instanceof Player) {
                p = (Player)sender;
            }

            if(args.length == 0) {

                if(p == null)
                    return true;

                guiManager.ShowBountiesMenu(p);
            }
            else if(args.length > 1) {

                if(args[0].equals("set")) {

                    Player set = Bukkit.getPlayer(args[1]);

                    if(set != null) {

                        if(args.length == 3) {
                            if(args[2].contains(":")){

                                String[] args2 = args[2].split(":");

                                if(args2[0].equals("eco") && LuckyBounties.instance.economy){
                                    float amount = Float.parseFloat(args2[1]);

                                    bounty mB = LuckyBounties.getEcoBounty(set.getUniqueId().toString());

                                    if(mB != null) {
                                        mB.moneyPayment += amount;
                                    }
                                    else {
                                        bounty b = new bounty(set.getUniqueId().toString(), amount);
                                        LuckyBounties.bounties.add(b);
                                    }

                                    if(p != null){
                                        LuckyBounties.doShit(p, amount, 0);

                                        if(LuckyBounties.instance.useMessages){
                                            String mS = LuckyBounties.instance.setPlayerMessage.replace("{player}",p.getDisplayName()).replace("{amount}", amount + LuckyBounties.instance.economy_name).replace("{target}", set.getDisplayName());
                                            Bukkit.broadcastMessage(mS);
                                        }
                                    }
                                    else if(LuckyBounties.instance.useMessages){
                                        String mS = LuckyBounties.instance.setConsoleMessage.replace("{target}",set.getDisplayName()).replace("{amount}",amount + LuckyBounties.instance.economy_name);
                                        Bukkit.broadcastMessage(mS);
                                    }
                                }
                                else if(!LuckyBounties.instance.economy){
                                    if(p != null){
                                        p.sendMessage(ChatColor.RED + "Economy support is disabled");
                                    }
                                    else{
                                        Bukkit.getLogger().info(ChatColor.RED + "Economy support is disabled");
                                    }
                                }
                            }
                        }
                        else {

                            if(p == null)
                                return true;

                            bounty b = new bounty(set.getUniqueId().toString(), p.getInventory().getItemInMainHand());
                            p.getInventory().remove(p.getInventory().getItemInMainHand());
                            LuckyBounties.bounties.add(b);
                        }
                    }
                    else {
                        if(p == null)
                            Bukkit.getLogger().info(ChatColor.RED + "This player does not exist");
                        else
                            p.sendMessage(ChatColor.RED + "This player does not exist");
                    }

                }
                else if(args[0].equals("reload")){
                    p = sender instanceof Player ? (Player)sender : null;

                    if(p != null && !p.isOp())
                        return true;

                    LuckyBounties.instance.loadConfig(p);
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        ArrayList<String> ret = new ArrayList<>();

        if(cmd.getName().equals("bounties")){
            if(args.length == 1){
                ret.add("-leave blank for menu-");
                ret.add("set");
                if(sender instanceof Player && ((Player)sender).isOp()){
                    ret.add("reload");
                }
            }
            else if(args.length == 2){
                if(args[0].equals("set")){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        ret.add(p.getName());
                    }
                }
            }
            else if(args.length == 3){
                ret.add("-leave blank to set your current item-");
                if(LuckyBounties.instance.economy){
                    ret.add("eco:" + LuckyBounties.instance.eco_amount);
                }
            }
        }

        return ret;
    }
}
