package de.lucky44.luckybounties.system;

import de.lucky44.api.luckybounties.events.BountySetEvent;
import de.lucky44.api.luckybounties.events.EcoBountyRemoveEvent;
import de.lucky44.api.luckybounties.events.EcoBountySetEvent;
import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.gui.guis.GUI_PlayerList;
import de.lucky44.luckybounties.timers.CooldownManager;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        Player p = null;
        if(sender instanceof Player)
            p = (Player)sender;
        else
            return true;

        if(LuckyBounties.I.executors.size() > 0){
            for(CommandExecutor e : LuckyBounties.I.executors){
                e.onCommand(sender, cmd, s, args);
            }
        }

        if(cmd.getName().equals("bounties")){

            if(args.length == 0){
                GUI_PlayerList gui = new GUI_PlayerList(0 ,null);
                gui.open((Player) sender);
            }
            else {
                if(Objects.equals(args[0].toLowerCase(), "reload")){
                    if(sender.hasPermission("lb.reload")){
                        LuckyBounties.I.reloadConfigData();
                        sender.sendMessage(LANG.getText("reload-complete"));
                    }
                }
                else if(args[0].equalsIgnoreCase("set")){
                    if(args.length < 2)
                        return true;

                    if(!p.hasPermission("lb.set")){
                        p.sendMessage(LANG.getText("missing-set-permission"));
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null){
                        sender.sendMessage(LANG.getText("player-not-found").replace("[PLAYERNAME]", args[1]));
                        return true;
                    }

                    if(!CooldownManager.I.isAllowedToSet(target, p)){
                        p.sendMessage(LANG.getText("cooldown-not-done").replace("[TARGET]", target.getName()));
                        return true;
                    }

                    if(target.hasPermission("lb.exempt")){
                        p.sendMessage(LANG.getText("target-exempt").replace("[PLAYERNAME]", target.getName()));
                        return true;
                    }

                    bounty b = null;
                    if(args.length == 3){
                        if(LuckyBounties.I.Vault == null){
                            sender.sendMessage(LANG.getText("economy-disabled"));
                            return true;
                        }

                        float payment = 0;
                        payment = Float.parseFloat(args[2]);

                        if(payment <= 0){
                            p.sendMessage(LANG.getText("bounty-below-zero"));
                            return true;
                        }

                        if(LuckyBounties.I.Vault.getBalance(p) < payment){
                            p.sendMessage(LANG.getText("cannot-afford")
                                    .replace("[AMOUNT]", ""+payment)
                                    .replace("[SYMBOL]", CONFIG.getString("currency-symbol")));
                            return true;
                        }

                        b = new bounty(payment);

                        EcoBountySetEvent event = new EcoBountySetEvent(p, target, payment);
                        LuckyBounties.I.callEvent(event);
                        if(event.isCancelled())
                            return true;

                        LuckyBounties.I.Vault.withdraw(p, payment);

                        if(!CONFIG.getBool("bounty-set-global"))
                            p.sendMessage(LANG.getText("eco-bounty-set")
                                .replace("[AMOUNT]", ""+payment)
                                .replace("[SYMBOL]", CONFIG.getString("currency-symbol"))
                                .replace("[TARGET]", target.getName()));
                        else
                            Bukkit.getServer().broadcastMessage(LANG.getText("eco-bounty-set-global")
                                    .replace("[AMOUNT]", ""+payment)
                                    .replace("[SYMBOL]", CONFIG.getString("currency-symbol"))
                                    .replace("[PLAYERNAME]", p.getName())
                                    .replace("[TARGET]", target.getName()));
                    }
                    else{
                        if(CONFIG.getBool("disable-items")){
                            p.sendMessage(LANG.getText("items-disabled"));
                            return true;
                        }

                        ItemStack holding = p.getInventory().getItemInMainHand();
                        if(p.getInventory().getItem(p.getInventory().getHeldItemSlot()) == null){
                            p.sendMessage(LANG.getText("not-holding-anything"));
                            return true;
                        }

                        b = new bounty(holding);

                        BountySetEvent event = new BountySetEvent(p, target, b);
                        LuckyBounties.I.callEvent(event);
                        if(event.isCancelled())
                            return true;

                        p.getInventory().remove(p.getInventory().getItemInMainHand());
                    }

                    CooldownManager.I.setBounty(target, p);
                    LuckyBounties.I.addBounty(target.getUniqueId(), b, p.getUniqueId());
                }
                else if(args[0].equalsIgnoreCase("remove")){
                    if(LuckyBounties.I.Vault == null){
                        p.sendMessage(LANG.getText("economy-disabled"));
                        return true;
                    }

                    if(!(p.hasPermission("lb.remove") && p.hasPermission("lb.op")))
                        return true;

                    if(args.length < 3)
                        return true;

                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null){
                        sender.sendMessage(LANG.getText("player-not-found").replace("[PLAYERNAME]", args[1]));
                        return true;
                    }

                    float amount = Float.parseFloat(args[2]);

                    bounty ecoBounty = LuckyBounties.I.getEcoBounty(target.getUniqueId());
                    if(ecoBounty == null)
                        return true;

                    EcoBountyRemoveEvent event = new EcoBountyRemoveEvent(p, target, amount);
                    LuckyBounties.I.callEvent(event);
                    if(event.isCancelled())
                        return true;

                    ecoBounty.moneyPayment -= amount;
                    if(ecoBounty.moneyPayment <= 0){
                        LuckyBounties.I.removeBounty(target.getUniqueId(), ecoBounty);
                    }

                    LuckyBounties.I.fetchPlayer(target.getUniqueId()).ecoWorth = ecoBounty.moneyPayment;
                    LuckyBounties.I.getHighestEcoBounty();

                    p.sendMessage(LANG.getText("removed-bounty").replace("[PLAYERNAME]", target.getName()));
                }
            }
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        List<String> ret = new ArrayList<>();

        if(sender == null)
            return ret;

        if(LuckyBounties.I.completers.size() > 0){
            for(TabCompleter t : LuckyBounties.I.completers){
                List<String> returnFromAPI = t.onTabComplete(sender, cmd, s, args);
                if(returnFromAPI == null)
                    continue;
                ret.addAll(returnFromAPI);
            }
        }

        if(!cmd.getName().equals("bounties"))
            return ret;

        if(args.length == 0)
            return ret;

        if(args.length == 1){
            ret.add("--leave blank to open the GUI--");

            if(LuckyBounties.I.Vault != null){
                if(sender.hasPermission("lb.set")){
                    ret.add("set");
                }

                if(sender.hasPermission("lb.remove") && sender.hasPermission("lb.op")){
                    ret.add("remove");
                }
            }

            if(sender.hasPermission("lb.op")){
                ret.add("reload");
            }
        }
        else if(args.length == 2){
            if(args[0].equals("set") || args[0].equals("remove")){
                if(LuckyBounties.I.Vault != null){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        ret.add(p.getName());
                    }
                }
            }
        }
        else if(args.length == 3){
            if(args[0].equals("remove") || args[0].equals("set")){
                ret.add("-leave blank to set the item you are holding-");
                if(LuckyBounties.I.Vault != null){
                    ret.add(CONFIG.getString("default-amount"));
                }
            }
        }

        return ret.stream().filter(f -> f.startsWith(args[args.length-1])).toList();
    }
}
