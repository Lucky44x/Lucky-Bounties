package de.lucky44.luckybounties.chat.Versions;
import de.lucky44.luckybounties.chat.ChatManager;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ChatManager1_16_5 extends ChatManager {
    @Override
    public String getChatItem(ItemStack originalItem){
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(originalItem);
        net.minecraft.server.v1_16_R3.NBTTagCompound compound = new net.minecraft.server.v1_16_R3.NBTTagCompound();
        compound = nmsItem.b(compound.toString());
        String json = compound.toString();
        return  json;
    }
}