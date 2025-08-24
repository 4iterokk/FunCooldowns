package me.jetby.cmdTimer.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;

import java.util.List;

public class Region {

    public boolean regionCheck(Location location, List<String> disallowedRegions) {
        if (disallowedRegions == null || disallowedRegions.isEmpty()) {
            return false;
        }

        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());
        Location wgLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());

        com.sk89q.worldedit.math.BlockVector3 blockVector = com.sk89q.worldedit.math.BlockVector3.at(wgLocation.getX(), wgLocation.getY(), wgLocation.getZ());

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = regionContainer.get(weWorld);
        if (regionManager != null) {
            ApplicableRegionSet regions = regionManager.getApplicableRegions(blockVector);
            com.sk89q.worldguard.protection.regions.ProtectedRegion highestPriorityRegion = null;
            for (com.sk89q.worldguard.protection.regions.ProtectedRegion region : regions) {
                if (highestPriorityRegion == null || region.getPriority() > highestPriorityRegion.getPriority())
                    highestPriorityRegion = region;
            }
            if (highestPriorityRegion != null && disallowedRegions.contains(highestPriorityRegion.getId()))
                return true;
        }
        return false;
    }
}
