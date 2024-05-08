package com.github.lucky44x.luckybounties.guis;

import com.github.lucky44x.gui.FileGUI;
import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckyutil.config.LangConfig;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;

/**
 * @author Lucky44x
 * File GUI for the setBounty GUI
 */
public class SetBountyGUI extends FileGUI {

    private boolean setBountyConfirmed = false;

    @LangConfig.LangData(langKey="[TARGET]", stringMethodNames="getName")
    private final Player target;

    public SetBountyGUI(LuckyBounties instance, Player user, Player target) throws FileNotFoundException {
        super(instance, user, "SetBounty", instance.langFile, instance.getBridge().getGUIExtensions("SetBounty"));
        this.target = target;
        finishInit();
    }

    @GUITag("cancel")
    public void cancelSetBounty(){
        /*
        if(getItem("BountyItem") != null){
            if(user.getInventory().firstEmpty() == -1){
                user.getWorld().dropItemNaturally(user.getLocation(), getItem("BountyItem"));
            }
            else{
                user.getInventory().addItem(getItem("BountyItem"));
            }
        }
        */

        try{
            new BountiesListGUI((LuckyBounties) instance, user, target);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GUITag("confirm")
    public void confirmSetBounty(){
        if(getItem("BountyItem") == null)
            return;

        if(!((LuckyBounties)instance).setBounty(getItem("BountyItem"), target, user)){
            cancelSetBounty();
            return;
        }

        setBountyConfirmed = true;

        try{
            new BountiesListGUI((LuckyBounties) instance, user, target);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(){
        if(getItem("BountyItem") == null || setBountyConfirmed)
            return;

        if(getItem("BountyItem") != null){
            if(user.getInventory().firstEmpty() == -1){
                user.getWorld().dropItemNaturally(user.getLocation(), getItem("BountyItem"));
            }
            else{
                user.getInventory().addItem(getItem("BountyItem"));
            }
        }
    }
}
