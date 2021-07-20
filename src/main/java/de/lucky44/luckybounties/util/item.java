package de.lucky44.luckybounties.util;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.util.Map;

public class item {
    public String DisplayName = "";
    public String _Material = "";

    public String[] enchantments;
    public int[] enchantmentLevels;

    public int number = 0;

    public int dur = 0;

    public String[] extras;

    public ItemStack converted = null;

    public item(ItemStack iS){

        //Convert ItemStack to custom ItemData which is better suited to be saved and loaded by gson
        //I did this 'cause i'm to lazy to figure out how to load and save ItemStacks directly to json

        Material m = iS.getType();
        ItemMeta iM = iS.getItemMeta();

        DisplayName = iM.getDisplayName();
        _Material = m.toString();

        if(m.equals(Material.ENCHANTED_BOOK)){
            EnchantmentStorageMeta eSM = (EnchantmentStorageMeta) iM;

            extras = new String[eSM.getStoredEnchants().size()];

            enchantments = new String[eSM.getStoredEnchants().size()];
            enchantmentLevels = new int[eSM.getStoredEnchants().size()];

            int i = 0;
            for(Map.Entry<Enchantment,Integer> e : eSM.getStoredEnchants().entrySet()){
                enchantments[i] = e.getKey().getName();
                enchantmentLevels[i] = e.getValue();
                i++;
            }
        }
        else if(m == Material.POTION || m == Material.SPLASH_POTION || m == Material.LINGERING_POTION){
            PotionMeta pM = (PotionMeta) iM;
            enchantments = new String[pM.getCustomEffects().size() + 1];
            enchantments[0] = pM.getBasePotionData().getType().toString();
            enchantmentLevels = new int[1];
            int a = 0;
            if(pM.getBasePotionData().isExtended()){
                a = 1;
            }else if(pM.getBasePotionData().isUpgraded()){
                a += 2;
            }
            enchantmentLevels[0] = a;
            for(int i = 0; i < pM.getCustomEffects().size(); i++){
                enchantments[i+1] = pM.getCustomEffects().get(i).getType().getName();
            }
        }
        else if(m == Material.WRITTEN_BOOK || m == Material.WRITABLE_BOOK){

            BookMeta bM = (BookMeta) iM;

            enchantments = new String[bM.getPageCount()];
            bM.getPages().toArray(enchantments);

            extras = new String[3];
            extras[0] = bM.getAuthor();
            extras[1] = bM.getTitle();
            String gen = "";
            if(bM.getGeneration() != null)
                gen = bM.getGeneration().toString();
            extras[2] = gen;
        }
        else{
            dur = iS.getDurability();

            enchantments = new String[iS.getItemMeta().getEnchants().size()];
            enchantmentLevels = new int[iS.getItemMeta().getEnchants().size()];

            int i = 0;
            for(Map.Entry<Enchantment,Integer> e : iS.getItemMeta().getEnchants().entrySet()){
                enchantments[i] = e.getKey().getName();
                enchantmentLevels[i] = e.getValue();
                i++;
            }
        }

        number = iS.getAmount();

        //Convert the itemstack only once on creation
        converted = toItem();
    }

    private ItemStack toItem(){

        //Convert custom ItemData to ItemStack

        if(org.bukkit.Material.getMaterial(_Material) == null)
            return null;

        ItemStack i = new ItemStack(org.bukkit.Material.getMaterial(_Material));
        ItemMeta meta = i.getItemMeta();

        if(i.getType().equals(Material.ENCHANTED_BOOK)){
            EnchantmentStorageMeta eSM = (EnchantmentStorageMeta) meta;

            for (int idx = 0; idx < enchantments.length; idx++) {
                eSM.addEnchant(Enchantment.getByName(enchantments[idx]), enchantmentLevels[idx], false);
            }

            i.setItemMeta(eSM);
        }
        else if(i.getType() == Material.POTION || i.getType() == Material.SPLASH_POTION || i.getType() == Material.LINGERING_POTION){

            PotionMeta pM = (PotionMeta) meta;

            boolean extended = false;
            boolean upgraded = false;

            if(enchantmentLevels[0] == 1){
                extended = true;
            }
            else if(enchantmentLevels[0] == 2){
                upgraded = true;
            }
            else if(enchantmentLevels[0] == 3){
                extended = true;
                upgraded = true;
            }

            pM.setBasePotionData(new PotionData(PotionType.valueOf(enchantments[0]), extended, upgraded));

            for(int idx = 0; idx < enchantments.length; idx++){
            }

            i.setItemMeta(pM);
        }
        else if(i.getType() == Material.WRITTEN_BOOK || i.getType() == Material.WRITABLE_BOOK){
            BookMeta bM = (BookMeta) meta;

            for(String s : enchantments){
                bM.addPage(s);
            }

            bM.setAuthor(extras[0]);
            bM.setTitle(extras[1]);

            if(!extras[2].equals("")){
                bM.setGeneration(BookMeta.Generation.valueOf(extras[2]));
            }

            i.setItemMeta(bM);
        }
        else {
            meta.setDisplayName(DisplayName);

            for (int idx = 0; idx < enchantments.length; idx++) {
                meta.addEnchant(Enchantment.getByName(enchantments[idx]), enchantmentLevels[idx], false);
            }

            i.setItemMeta(meta);
        }

        i.setAmount(number);

        i.setDurability((short) dur);

        return i;
    }
}
