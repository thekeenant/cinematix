package net.avicus.cinematix;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
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
            sender.sendMessage(ChatColor.GOLD + "/cinematic <...>");
            sender.sendMessage(ChatColor.GOLD + "  add " + ChatColor.GRAY + "(adds a new point)");
            sender.sendMessage(ChatColor.GOLD + "  remove (point #) " + ChatColor.GRAY + "(removes an existing point)");
            sender.sendMessage(ChatColor.GOLD + "  points " + ChatColor.GRAY + "(list existing points)");
            sender.sendMessage(ChatColor.GOLD + "  view <point #> " + ChatColor.GRAY + "(teleport to an existing point)");
            sender.sendMessage(ChatColor.GOLD + "  clear " + ChatColor.GRAY + "(clear all points)");
            sender.sendMessage(ChatColor.GOLD + "  start <seconds> (delay) " + ChatColor.GRAY + "(start the cinematic)");
            sender.sendMessage(ChatColor.GOLD + "  stop " + ChatColor.GRAY + "(stop the ongoing cinematic)");
            sender.sendMessage(ChatColor.GOLD + "  version " + ChatColor.GRAY + "(check the version)");
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
            player.sendMessage(ChatColor.GREEN + "Added point #" + cine.getPoints().size() + ".");
        }
        else if (sub.equals("remove")) {
            int index = cine.getPoints().size() - 1;
            if (args.length > 1) {
                try {
                    index = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Invalid point number provided.");
                    return true;
                }
            }
            if (index < 0 || index > cine.getPoints().size() - 1) {
                player.sendMessage(ChatColor.RED + "Invalid point.");
                return true;
            }

            cine.getPoints().remove(index);
            player.sendMessage(ChatColor.GREEN + "Removed point #" + (index + 1) + ".");
        }
        else if (sub.equals("view")) {
            int index = cine.getPoints().size() - 1;
            if (args.length > 1) {
                try {
                    index = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Invalid point number provided.");
                    return true;
                }
            }
            if (index < 0 || index > cine.getPoints().size() - 1) {
                player.sendMessage(ChatColor.RED + "Invalid point.");
                return true;
            }

            player.teleport(cine.getPoints().get(index));
            player.sendMessage(ChatColor.GREEN + "Teleported to point #" + (index + 1) + ".");
        }
        else if (sub.equals("clear")) {
            cine.getPoints().clear();
            player.sendMessage(ChatColor.GREEN + "Cleared all points.");
        }
        else if (sub.equals("points")) {
            if (cine.getPoints().size() == 0) {
                player.sendMessage(ChatColor.RED + "No points have been added.");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + "Cinematic points:");
            for (Location loc : cine.getPoints()) {
                DecimalFormat format = new DecimalFormat("#.##");
                String x = format.format(loc.getX());
                String y = format.format(loc.getY());
                String z = format.format(loc.getZ());
                String xyz = x + ", " + y + ", " + z;
                player.sendMessage(ChatColor.GREEN + "  #" + (cine.getPoints().indexOf(loc) + 1) + ": " + xyz);
            }
        }
        else if (sub.equals("start")) {
            if (cine.getPoints().size() == 0) {
                player.sendMessage(ChatColor.RED + "No points have been added.");
                return true;
            }

            int seconds;
            try {
                seconds = Integer.parseInt(args[1]);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Please provide the duration of the cinematic in seconds.");
                return true;
            }

            final double time = seconds;

            int delay = 0;

            try {
                if (args.length > 2)
                    delay = Integer.parseInt(args[2]);
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Please provide the delay of the cinematic in seconds.");
                return true;
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    cine.start(player, time);
                }
            }, delay * 20);

            player.sendMessage(ChatColor.GREEN + "Starting cinematic (" + time + " seconds, " + delay + " delay).");
        }
        else if (sub.equals("stop")) {
            if (!cine.isRunning()) {
                player.sendMessage(ChatColor.RED + "There is no cinematic in progress.");
                return true;
            }

            cine.stop(player);
            player.sendMessage(ChatColor.GREEN + "The cinematic in progress has been stopped.");
        }
        else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand, type /cinematix for help.");
        }

        return true;
    }
}
