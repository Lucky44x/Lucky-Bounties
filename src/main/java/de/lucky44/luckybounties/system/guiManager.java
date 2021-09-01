package de.lucky44.luckybounties.system;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.util.bounty;
import de.lucky44.luckybounties.util.permissionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.ArrayList;
import java.util.List;

public class guiManager {

    public static void ShowBountiesMenu(Player p){

        ArrayList<ItemStack> tabs = new ArrayList<>();

        for(Player p1 : Bukkit.getOnlinePlayers()){
            tabs.add(getPlayerHead(p1));
        }

        int size = 1;
        int counter = 0;

        for(int i = 0; i < tabs.size(); i++){
            counter++;

            if(counter > 4){
                size++;
                counter = 1;
            }
        }

        Inventory gui = Bukkit.createInventory(p, size * 9,ChatColor.BOLD + "BOUNTIES");

        fillInventory(gui,LuckyBounties.gray);

        for(int i = 0; i < tabs.size(); i++){
            gui.setItem(i+(i+1),tabs.get(i));
        }

        p.openInventory(gui);
    }

    public static void showSpecificMenu(Player sender, Player toShow){
        sender.closeInventory();

        String UUID_SHOW = toShow.getUniqueId().toString();

        ArrayList<bounty> bounties = new ArrayList<>();

        for(bounty b : LuckyBounties.bounties){
            if(b.UUID.equals(UUID_SHOW)){
                bounties.add(b);
            }
        }

        Inventory gui = Bukkit.createInventory(sender, 54, ChatColor.BOLD + toShow.getDisplayName() + "'s bounties");

        fillInventory(gui,LuckyBounties.gray);

        gui.setItem(4,getPlayerHead(toShow));

        gui.setItem(13,LuckyBounties.set);

/*        if(sender.getUniqueId() != toShow.getUniqueId()){
            gui.setItem(13,LuckyBounties.set);
        }*/

        if((sender.isOp() && LuckyBounties.instance.Clear == permissionType.OP) || (sender.hasPermission("lb.op") && LuckyBounties.instance.Clear == permissionType.LB) || ((sender.hasPermission("lb.op") || sender.isOp()) && LuckyBounties.instance.Clear == permissionType.BOTH)){
            gui.setItem(8,LuckyBounties.clear);
        }

        for(int i = 0; i < bounties.size(); i++){
            if(bounties.get(i).type == 1){

                if(LuckyBounties.instance.economy) {
                    ItemStack heGotThemMoneyz = new ItemStack(LuckyBounties.moneyz);
                    ItemMeta iM =heGotThemMoneyz.getItemMeta();
                    iM.setDisplayName(ChatColor.BOLD + ChatColor.GOLD.toString() + "Money on kill");
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "" + bounties.get(i).moneyPayment + LuckyBounties.instance.economy_name);
                    iM.setLore(lore);
                }

            }
            else if(LuckyBounties.instance.useItems){
                gui.setItem(i+(i+1) + 18,bounties.get(i).payment.converted);
            }
        }

        sender.openInventory(gui);
    }

    public static void showBountySetMenu(Player sender, Player toSet){
        sender.closeInventory();

        Inventory gui = Bukkit.createInventory(sender,InventoryType.DISPENSER,ChatColor.BOLD + "Set bounty on " + toSet.getDisplayName() + "'s head");

        gui.setItem(1,getPlayerHead(toSet));
        gui.setItem(6,LuckyBounties.red);
        gui.setItem(8,LuckyBounties.green);

        //Gray shit
        for(int i = 0; i < 9; i++){
            if( i != 1 && i != 4 && i != 6 && i != 8){
                gui.setItem(i,LuckyBounties.gray);
            }
        }

        sender.openInventory(gui);
    }

    public static void confirmBounty(Player p,Player toSet){

        ItemStack iS = p.getOpenInventory().getItem(4);
        if(iS == null){
            p.sendMessage(ChatColor.RED + "Please give an item as payment");
            return;
        }

        bounty b = new bounty(toSet.getUniqueId().toString(),iS);
        LuckyBounties.bounties.add(b);

        showSpecificMenu(p,toSet);

        String s = "";
        if(iS.getAmount() > 1){s = "s";}
        p.sendMessage(ChatColor.GREEN + "Set a bounty of " + iS.getAmount() + " " + iS.getType().toString().toLowerCase() + s + " on " + toSet.getDisplayName() + "'s head.");
    }

    public static void cancelBounty(Player p, Player toSet){
        showSpecificMenu(p,toSet);
    }

    public static ItemStack getPlayerHead(Player player){

        Material type = Material.PLAYER_HEAD;
        ItemStack head = new ItemStack(type);

        SkullMeta sKM = (SkullMeta) head.getItemMeta();

        if(sKM != null){
            sKM.setOwningPlayer(player);
            sKM.setDisplayName(ChatColor.AQUA + ChatColor.BOLD.toString() + player.getDisplayName());
        }

        head.setItemMeta(sKM);

        return head;
    }

    public static void fillInventory(Inventory I,ItemStack _i){
        for(int i = 0; i < I.getSize(); i++){
            I.setItem(i,_i);
        }
    }
}
