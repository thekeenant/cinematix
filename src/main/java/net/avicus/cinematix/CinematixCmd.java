package net.avicus.cinematix;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CinematixCmd implements CommandExecutor {
    private CinematixPlugin plugin;

    public CinematixCmd(CinematixPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("cinematix")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/cinematic <...>");
            sender.sendMessage(ChatColor.RED + "  add " + ChatColor.GRAY + "(adds a new point)");
            sender.sendMessage(ChatColor.RED + "  remove (number) " + ChatColor.GRAY + "(removes an existing point)");
            sender.sendMessage(ChatColor.RED + "  points " + ChatColor.GRAY + "(list existing points)");
            sender.sendMessage(ChatColor.RED + "  clear " + ChatColor.GRAY + "(clear all points)");
            sender.sendMessage(ChatColor.RED + "  version " + ChatColor.GRAY + "(check the version)");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("version")) {
            String version = plugin.getDescription().getVersion();
            sender.sendMessage(ChatColor.YELLOW + "Currently running Cinematic " + ChatColor.GOLD + version + ChatColor.YELLOW + "!");
            return true;
        }
        else if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Must be a player to use that cinematic command.");
            return true;
        }

        final Player player = (Player) sender;
        final Cinematix cine = plugin.getCinematix(player);

        if (sub.equals("add")) {
            cine.getPoints().add(player.getLocation());
        }
        else if (sub.equals("clear")) {
            cine.getPoints().clear();
        }
        else if (sub.equals("play")) {
            Runnable runnable = new Runnable() {

                int i = 0;
                List<Location> path = cine.generatePath(15.0);

                @Override
                public void run() {
                    if (i > path.size() - 1) {
                        return;
                    }

                    player.teleport(path.get(i));
                    i++;
                }
            };

            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, runnable, 0, 1);
        }
        else {
            sender.sendMessage(ChatColor.RED + "Unknown sub-command, type `/cinematix` for help.");
            return true;
        }

        return true;
    }
}
