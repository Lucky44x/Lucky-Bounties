package de.lucky44.luckybounties.chat.Versions;

import de.lucky44.luckybounties.chat.ChatManager;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
public class ChatManager1_19 extends ChatManager {
    @Override
    public String getChatItem(ItemStack originalItem){
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(originalItem);
        NBTTagCompound compound = new NBTTagCompound();
        compound = nmsItem.b(compound);
        String json = compound.toString();
        return  json;
    }
}