package com.github.lucky44x.luckybounties.guis;

import com.github.lucky44x.gui.AnvilGUI;
import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckyutil.config.LangConfig;
import com.github.lucky44x.luckyutil.numbers.NumberUtilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.FileNotFoundException;

public class SetEcoBountyGUI extends AnvilGUI {

    @LangConfig.LangData(langKey = "[TARGET]", stringMethodNames = "getName")
    private final Player target;

    public SetEcoBountyGUI(LuckyBounties instance, Player user, Player target) {
        super(user, "NAN", instance);
        this.target = target;
        this.title = instance.langFile.getText("GUI-set-bounty", this);
        finishInit();
    }

    @Override
    protected void onClose() {}

    @Override
    protected void constructView() {
        setItem(0, getEcoItem());
    }

    @Override
    protected void onLoad() {}

    @LangConfig.LangData(langKey = "[INPUT]")
    private String langInputStringTMP = "NULL";

    @Override
    public void slotClickedEvent(InventoryClickEvent e){
        //instance.getLogger().info("Slot: " + e.getSlot());
        if(e.getSlot() == 2){

            if(!NumberUtilities.isStringValidFloat(getItem(2).getItemMeta().getDisplayName())){
                langInputStringTMP = getItem(2).getItemMeta().getDisplayName();
                user.sendMessage(((LuckyBounties)instance).langFile.getText("eco-not-valid", this));
                return;
            }
            ((LuckyBounties)instance).setBounty(Double.parseDouble(getItem(2).getItemMeta().getDisplayName()), target, user);

            try{
                new BountiesListGUI((LuckyBounties) instance, user, target);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private ItemStack getEcoItem(){
        ItemStack button = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName("");
        button.setItemMeta(meta);

        return button;
    }
}
