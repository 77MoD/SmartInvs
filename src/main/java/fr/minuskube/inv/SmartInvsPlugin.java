package fr.minuskube.inv;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class SmartInvsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "&aSuccessfully loaded SmartInventories plugin, modified by 77mod");
    }
}
