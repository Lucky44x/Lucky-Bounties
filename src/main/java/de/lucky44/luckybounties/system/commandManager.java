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

                                if(args2[0].equals("eco")){
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
                                        LuckyBounties.doShit(p.getUniqueId().toString(), amount);
                                    }
                                }
                            }
                        }
                        else {

                            if(p == null)
                                return true;

                            bounty b = new bounty(set.getUniqueId().toString(), p.getInventory().getItemInMainHand());
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

            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        return null;
    }
}
