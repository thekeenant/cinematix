package net.avicus.cinematix;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class CinematixPlugin extends JavaPlugin {
    private HashMap<Player,Cinematix> cinematix;

    public void onEnable() {
        getCommand("cinematix").setExecutor(new CinematixCmd(this));
        cinematix = new HashMap<Player,Cinematix>();
    }

    public void onDisable() {
        cinematix.clear();
    }

    public Cinematix getCinematix(Player player) {
        if (cinematix.containsKey(player))
            return cinematix.get(player);
        cinematix.put(player, new Cinematix(this));
        return getCinematix(player);
    }
}
