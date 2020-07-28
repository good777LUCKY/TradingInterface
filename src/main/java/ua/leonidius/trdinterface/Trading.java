package ua.leonidius.trdinterface;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.plugin.PluginBase;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ru.nukkit.dblib.DbLib;
import ua.leonidius.trdinterface.models.BuyableItem;
import ua.leonidius.trdinterface.models.Category;
import ua.leonidius.trdinterface.models.SellableItem;
import ua.leonidius.trdinterface.models.Shop;
import ua.leonidius.trdinterface.views.screens.Screen;

import java.io.File;
import java.sql.SQLException;

/**
 * Created by Leonidius20 on 07.01.18.
 */
public class Trading extends PluginBase implements Listener {

    private static Trading plugin;
    public static Settings settings;

    private static ConnectionSource source;

    @Override
    public void onEnable() {
        plugin = this;

        settings = new Settings(this);
        settings.load();

        Message.init(this);

        getServer().getCommandMap().register("trd-interface", new ShopCommand(this));

        getServer().getPluginManager().registerEvents(this, this);

        getDataFolder().mkdirs();
        /*imageFolder = new File(getDataFolder(), "images");
        imageFolder.mkdirs();*/

        saveResource("config.yml");

        source = DbLib.getConnectionSource(DbLib.getSqliteUrl(new File(getDataFolder(), "shops.db")), null, null);

        try {
            TableUtils.createTableIfNotExists(source, Shop.class);

            // Creating the default shop (accessed with /shop)
            Dao<Shop, Integer> shopDao = DaoManager.createDao(source, Shop.class);
            shopDao.createIfNotExists(Shop.getDefault());

            TableUtils.createTableIfNotExists(source, Category.class);
            TableUtils.createTableIfNotExists(source, BuyableItem.class);
            TableUtils.createTableIfNotExists(source, SellableItem.class);
        } catch (SQLException e) {
            getLogger().critical(e.getMessage());
            getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        source.closeQuietly();
    }

    // Form response handler
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onFormResponse(PlayerFormRespondedEvent event) {
        if (event.getResponse() == null) return; // onClose()
        if (!(event.getWindow() instanceof Screen)) return;
        ((Screen) event.getWindow()).onResponse(event);
    }

    public static Trading getPlugin() {
        return plugin;
    }

    public static ConnectionSource getSource() {
        return source;
    }

    public static Settings getSettings() {
        return settings;
    }

}