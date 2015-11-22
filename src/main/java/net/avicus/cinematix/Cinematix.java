package net.avicus.cinematix;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Cinematix {
    private Player player;
    private List<Location> points;

    public Cinematix(Player player) {
        this.player = player;
        this.points = new ArrayList<Location>();
    }

    public List<Location> getPoints() {
        return points;
    }

    public List<Location> generatePath(double seconds) {
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
        World world = player.getWorld();

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
}
