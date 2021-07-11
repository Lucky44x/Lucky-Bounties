package de.lucky44.luckybounties.system;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class commandManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        //Check if it's our custom command
        if(cmd.getName().equalsIgnoreCase("bounties")){

            //Get Player
            Player p = null;

            if(sender instanceof Player){
                p = (Player)sender;
            }
            else{
                return true;
            }

            guiManager.ShowBountiesMenu(p);
        }

        return true;
    }
}
