package de.lucky44.luckybounties.system;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import de.lucky44.luckybounties.util.permissionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

                    if(!sender.hasPermission("lb.set")){
                        sender.sendMessage(ChatColor.RED + "You are not allowed to set bounties");
                        return true;
                    }

                    Player set = Bukkit.getPlayer(args[1]);

                    if(set != null) {

                        if(set.hasPermission("lb.exempt")){
                            sender.sendMessage(ChatColor.RED + set.getName() + " is exempt from bounties");
                            return true;
                        }

                        if(args.length == 3) {
                            if(args[2].contains(":")){

                                String[] args2 = args[2].split(":");

                                if(args2[0].equals("eco") && LuckyBounties.instance.economy){
                                    float amount = Float.parseFloat(args2[1]);

                                    if(amount <= 0){
                                        if(p != null)
                                            p.sendMessage(ChatColor.RED + "Nice try, but your amount must be positive");

                                        return true;
                                    }

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

                            ItemStack holding = null;

                            if(p == null) {
                                return true;
                            } else {
                                holding = p.getInventory().getItemInMainHand();
                            }

                            if(holding == null)
                                return true;

                            bounty b = new bounty(set.getUniqueId().toString(), holding);
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

                    if((p != null && !p.isOp()) || (p!= null && !p.hasPermission("lb.reload")) || p != null)
                        return true;

                    LuckyBounties.instance.loadConfig(p);
                }
                else if(args[0].equals("remove")){
                    p = sender instanceof Player ? (Player)sender : null;

                    if(p == null)
                        return true;

                    if((LuckyBounties.instance.remove == permissionType.OP && p.isOp()) || (LuckyBounties.instance.remove == permissionType.LB && p.hasPermission("lb.op")) || (LuckyBounties.instance.remove == permissionType.BOTH && (p.isOp() || p.hasPermission("lb.op")))){

                        //Do more stuff
                        Player target = Bukkit.getPlayer(args[1]);

                        if(target == null){
                            p.sendMessage(ChatColor.RED + "This player does not exist");
                            return true;
                        }

                        float amount = -1;

                        if(args[2].contains("eco:")){
                            amount = Float.parseFloat(args[2].split(":")[1]);

                            if(amount <= 0){
                                p.sendMessage(ChatColor.RED + "Please input a positive value");
                                return true;
                            }
                        }

                        if(amount == -1)
                            return true;

                        bounty mb = LuckyBounties.getEcoBounty(target.getUniqueId().toString());
                        if(mb == null)
                            return true;

                        mb.moneyPayment -= mb.moneyPayment - amount < 0 ? mb.moneyPayment : amount;
                        if(mb.moneyPayment <= 0){
                            LuckyBounties.bounties.remove(mb);
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        ArrayList<String> ret = new ArrayList<>();

        Player p = sender instanceof Player ? (Player)sender : null;

        if(p == null)
            return null;

        if(cmd.getName().equals("bounties")){
            if(args.length == 1){
                ret.add("-leave blank for menu-");
                ret.add("set");
                if(p.isOp()) {
                    ret.add("reload");
                }

                //Do someting wong
                if((LuckyBounties.instance.remove == permissionType.OP && p.isOp()) || (LuckyBounties.instance.remove == permissionType.LB && p.hasPermission("lb.op")) || (LuckyBounties.instance.remove == permissionType.BOTH && (p.isOp() || p.hasPermission("lb.op")))){
                    ret.add("remove");
                }
            }
            else if(args.length == 2){
                if(args[0].equals("set") || args[0].equals("remove")){
                    for(Player p1 : Bukkit.getOnlinePlayers()){
                        ret.add(p1.getName());
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
