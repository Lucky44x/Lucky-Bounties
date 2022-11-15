package de.lucky44.luckybounties.guis;

import de.lucky44.luckybounties.LuckyBounties;
import de.lucky44.luckybounties.files.config.CONFIG;
import de.lucky44.luckybounties.files.lang.LANG;
import de.lucky44.luckybounties.util.bounty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIItems {

    public static ItemStack FillerItem(){
        ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = gray.getItemMeta();
        assert meta != null;
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName("-");
        gray.setItemMeta(meta);

        return gray;
    }

    public static ItemStack ErrorSlotItem(String name){
        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = red.getItemMeta();
        assert meta != null;
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(name);
        red.setItemMeta(meta);

        return red;
    }

    public static ItemStack BountyItem(bounty b){
        if(b.moneyPayment > 0 && b.payment == null)
            return EcoItem(b);

        ItemStack toShow = b.payment.clone();

        if(CONFIG.getBool("set-by-lore")){
            ItemMeta meta = toShow.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            String setterName = LANG.getText("unknown-setter");
            if(dataContainer.has(LuckyBounties.I.dataKey, PersistentDataType.STRING)){
                String setterID = dataContainer.get(LuckyBounties.I.dataKey, PersistentDataType.STRING);
                OfflinePlayer offP = Bukkit.getOfflinePlayer(UUID.fromString(setterID));
                setterName = offP.getName();
            }

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(LANG.getText("item-lore-set-by").replace("[PLAYERNAME]", setterName));
            meta.setLore(lore);
            toShow.setItemMeta(meta);
        }

        return toShow;
    }

    private static ItemStack EcoItem(bounty b){
        Material m = Material.getMaterial(CONFIG.getString("eco-item").toUpperCase()) == null ? Material.GOLD_NUGGET : Material.getMaterial(CONFIG.getString("eco-item").toUpperCase());
        ItemStack I = new ItemStack(m);
        ItemMeta meta = I.getItemMeta();
        assert meta != null;
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(LANG.getText("eco-bounty-name")
                .replace("[AMOUNT]", ""+b.moneyPayment)
                .replace("[SYMBOL]", ""+ LuckyBounties.I.Vault.getSymbol()));
        I.setItemMeta(meta);
        return I;
    }

    public static ItemStack ConfirmItem(){
        ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = green.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LANG.getText("confirm-button")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        green.setItemMeta(meta);
        return green;
    }

    public static ItemStack CancelItem(){
        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = red.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LANG.getText("cancel-button")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        red.setItemMeta(meta);
        return red;
    }

    public static ItemStack NextItem(){
        ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = green.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LANG.getText("next-button")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        green.setItemMeta(meta);
        return green;
    }

    public static ItemStack BackItem(){
        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = red.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LANG.getText("back-button")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        red.setItemMeta(meta);
        return red;
    }

    public static ItemStack SetItem(){
        ItemStack set = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = set.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LANG.getText("set-button")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        set.setItemMeta(meta);
        return set;
    }

    public static ItemStack ClearItem(){
        ItemStack clear = new ItemStack(Material.FEATHER);
        ItemMeta meta = clear.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', LANG.getText("clear-button")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        clear.setItemMeta(meta);
        return clear;
    }

    public static ItemStack getPlayerHead(OfflinePlayer player){
        Material type = Material.PLAYER_HEAD;
        ItemStack head = new ItemStack(type);

        SkullMeta sKM = (SkullMeta) head.getItemMeta();

        if(sKM != null){
            sKM.setOwningPlayer(player);
            sKM.setDisplayName(ChatColor.AQUA + ChatColor.BOLD.toString() + player.getName());
        }

        bounty[] bounties = LuckyBounties.I.fetchBounties(player.getUniqueId()).toArray(bounty[]::new);

        List<String> lore = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            if(i >= bounties.length)
                break;

            if(bounties[i].payment == null){
                lore.add(LANG.getText("head-lore-eco-format")
                        .replace("[SYMBOL]", CONFIG.getString("currency-symbol"))
                        .replace("[AMOUNT]", bounties[i].moneyPayment+""));
            }
            else{
                lore.add(LANG.getText("head-lore-format")
                        .replace("[TYPE]", bounties[i].payment.getType().name())
                        .replace("[AMOUNT]", bounties[i].payment.getAmount()+""));
            }
        }

        if(bounties.length > 5){
            lore.add("...");
        }

        sKM.setLore(lore);

        head.setItemMeta(sKM);

        return head;
    }
}
