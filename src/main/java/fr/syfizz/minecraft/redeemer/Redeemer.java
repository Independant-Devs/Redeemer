package fr.syfizz.minecraft.redeemer;

import fr.syfizz.minecraft.redeemer.commands.RedeemCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Redeemer extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig("data");
        getConfig("messages");

        getCommand("redeem").setExecutor(new RedeemCommand());

        new Metrics(this);
    }

    @Override
    public void onDisable() {

    }

    public List<String> getRedeemableCommands(String redeemable) {
        return getConfig().getStringList("redeemables." + redeemable + ".commands");
    }

    public Integer getRedeemableTimes(String redeemable) {
        return Redeemer.getInstance().getConfig().getInt("redeemables." + redeemable + "timesCanRedeem");
    }

    public boolean hasRedeemablePermission(Player player, String redeemable) {
        return (Redeemer.getInstance().getConfig().getString("redeemables." + redeemable + ".permission") == null) || player.hasPermission(Redeemer.getInstance().getConfig().getString("redeemables." + redeemable + ".permission"));
    }

    public String getRedeemedUser(String redeemable, UUID uuid) {
        for (String redeemableUser:getConfig("data").getStringList(redeemable)) {
            String user = redeemableUser.split(":")[0];
            if (user.equalsIgnoreCase(uuid.toString()))
                return redeemableUser;
        }
        return uuid + ":0";
    }

    public void saveRedeemedUserToConfig(UUID uuid, String redeemable, int timesRedeemed) {
        String oldSerialise = uuid + ":" + (timesRedeemed - 1);
        String seralised = uuid + ":" + timesRedeemed;
        FileConfiguration config = getConfig("data.yml");

        List<String> stringList = config.getStringList(redeemable);
        stringList.remove(oldSerialise);
        stringList.add(seralised);
        config.set(redeemable, stringList);

        try {
            config.save(new File(getDataFolder() + "/" + "data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRedeemables() {
        return new ArrayList<>(getConfig().getConfigurationSection("redeemables").getKeys(false));
    }

    public static Redeemer getInstance() {
        return getPlugin(Redeemer.class);
    }

    public final FileConfiguration getConfig(final String name) {
        String fileName = name;
        if (!fileName.endsWith(".yml"))
            fileName += ".yml";

        final File configFile = new File(getDataFolder(), fileName);
        if (!configFile.exists()) {
            this.saveResource(fileName, true);
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        final Reader defaultConfigStream = new InputStreamReader(this.getResource(fileName), StandardCharsets.UTF_8);
        if (defaultConfigStream != null) {
            final YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
            config.setDefaults(defaultConfig);
        }

        return config;
    }
    
    public boolean hasConfig(final String name) {
        final String fileName = name + (name.endsWith(".yml") ? "" : ".yml");
        final File file = new File(getDataFolder(), fileName);
        return this.getResource(fileName) != null || (file.exists() && !file.isDirectory());
    }
}
