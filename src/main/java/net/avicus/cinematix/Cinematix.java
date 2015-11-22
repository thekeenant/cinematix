package net.avicus.cinematix;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cinematix {
    private final JavaPlugin plugin;
    private final List<Location> points;
    private final HashMap<Player,Integer> runnables;
    private final HashMap<Player,Boolean> stopping;

    /**
     * Create a new Cinematix instance. Each has its own set of
     * points and its own path to follow. Any number of players
     * may view a single cinematix.
     * @param plugin
     */
    public Cinematix(JavaPlugin plugin) {
        this.plugin = plugin;
        this.points = new ArrayList<Location>();
        this.runnables = new HashMap<Player, Integer>();
        this.stopping = new HashMap<Player, Boolean>();
    }

    /**
     * @return The list of points to travel between.
     */
    public List<Location> getPoints() {
        return points;
    }

    /**
     * Generates a list of locations (often in the hundreds, thousands) that forms
     * a smooth path between all the points in this cinematix.
     *
     * @param seconds The time to travel between locations.
     * @return A path of locations.
     */
    public List<Location> generatePath(double seconds) {
        if (points.size() == 0)
            return new ArrayList<Location>();

        World world = points.get(0).getWorld();

        double totalTime = seconds * (20.0 / 1.0);

        List<Double> distances = new ArrayList<Double>();
        List<Double> times = new ArrayList<Double>();

        double totalDistance = 0;

        // Calculate distances
        for (int i = 0; i < points.size() - 1; i++) {
            Location from = points.get(i);
            Location to = points.get(i + 1);

            double distance = from.distance(to);

            totalDistance += distance;
            distances.add(distance);
        }

        // Calculate portion of time each point should take
        for (Double distance : distances) {
            double percent = distance / totalDistance;
            times.add(percent * totalTime);
        }

        // path are the places to teleport to every tick (1/20th second)
        List<Location> path = new ArrayList<Location>();

        for (int i = 0; i < points.size() - 1; i++) {
            Location from = points.get(i);
            Location to = points.get(i + 1);
            double time = times.get(i);

            double dX = to.getX() - from.getX();
            double dY = to.getY() - from.getY();
            double dZ = to.getZ() - from.getZ();
            float dYaw = Math.abs(to.getYaw() - from.getYaw());
            float dPitch = to.getPitch() - from.getPitch();

            if (dYaw <= 180.0) {
                if (from.getYaw() >= to.getYaw())
                    dYaw = -dYaw;
            }
            else if (from.getYaw() < to.getYaw()) {
                dYaw = dYaw - 360.0F;
            }
            else {
                dYaw = 360.0F - dYaw;
            }

            for (double t = 0; t < time; t++) {
                double x = from.getX() + (dX / time) * t;
                double y = from.getY() + (dY / time) * t;
                double z = from.getZ() + (dZ / time) * t;
                float yaw = (float) (from.getYaw() + (dYaw / time) * t);
                float pitch = (float) (from.getPitch() + (dPitch / time) * t);

                Location loc = new Location(world, x, y, z, yaw, pitch);
                path.add(loc);
            }
        }

        return path;
    }

    /**
     * Start a cinematix for the given duration of time.
     * @param player The player to engage in a cinematix.
     * @param time The time in seconds.
     * @return True if successful, false if the player is already in a cinematix.
     */
    public boolean start(final Player player, final double time) {
        if (isRunning(player))
            return false;

        Runnable task = new Runnable() {

            int i = 0;
            List<Location> path = generatePath(time);

            @Override
            public void run() {
                boolean stop = stopping.get(player);

                if (i > path.size() - 1 || stop) {
                    int taskId = runnables.get(player);
                    Bukkit.getScheduler().cancelTask(taskId);
                    runnables.remove(player);
                    stopping.remove(player);
                    return;
                }

                for (int x = 0; x < 5; x++)
                    player.teleport(path.get(i));

                i++;
            }
        };

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, 0, 1);

        stopping.put(player, false);
        runnables.put(player, taskId);
        return true;
    }

    /**
     * Stop an ongoing cinematix.
     * @param player The player who's cinematix should stop.
     * @return True if a cinematix was in progress, false if otherwise.
     */
    public boolean stop(Player player) {
        if (!isRunning(player))
            return false;
        stopping.put(player, true);
        return true;
    }

    /**
     * @param player The player to check.
     * @return True if a player's cinematix is in progress, false if otherwise.
     */
    public boolean isRunning(Player player) {
        return runnables.containsKey(player);
    }
}
