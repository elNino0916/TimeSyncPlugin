package de.elnino0916.TimeSync;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

public class TimeSync extends JavaPlugin {

    private static final long DEFAULT_SYNC_INTERVAL = 20;
    private boolean isSyncing = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        startSyncTask();
    }

    @Override
    public void onDisable() {
        stopSyncTask();
    }

    private void startSyncTask() {
        if (isSyncing) return;

        isSyncing = true;
        long syncInterval = getConfig().getLong("sync-interval", DEFAULT_SYNC_INTERVAL);

        getServer().getScheduler().runTaskTimer(this, () -> {
            Set<String> worlds = getConfig().getConfigurationSection("worlds").getKeys(false);
            for (String worldName : worlds) {
                if (getConfig().getBoolean("worlds." + worldName + ".enabled")) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                        String timezone = getConfig().getString("worlds." + worldName + ".timezone", "Europe/Berlin");
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        int minute = cal.get(Calendar.MINUTE);
                        long timeTicks = calculateTimeTicks(hour, minute);
                        world.setTime(timeTicks);
                    }
                }
            }
        }, syncInterval, syncInterval);
    }

    private void stopSyncTask() {
        isSyncing = false;
    }

    private long calculateTimeTicks(int realHour, int minute) {
        long minecraftDayTicks = 24000;
        int totalRealMinutes = (realHour * 60) + minute;
        double proportionOfDay = (double) totalRealMinutes / (24 * 60);
        long timeTicks = (long) (proportionOfDay * minecraftDayTicks);
        timeTicks = timeTicks % minecraftDayTicks - 5980; // Adjust the result
        if (timeTicks < 0) {
            timeTicks += minecraftDayTicks;
        }

        return timeTicks;
    }
}
