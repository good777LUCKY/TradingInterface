package ua.leonidius.trdinterface;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ShopHelper {

    public static Map<Item, Double> getItemAndPrice(int itemRecordId) throws SQLException, IOException {
        String query = "SELECT id, price, nbt FROM buy_items WHERE record_id = ?";
        PreparedStatement statement = Trading.getDbConnection().prepareStatement(query);
        statement.setInt(1, itemRecordId);
        ResultSet results = statement.executeQuery();
        results.next();
        Item item = Item.fromString(results.getString("id"));
        byte[] nbtBytes = results.getBytes("nbt");
        if (nbtBytes != null && nbtBytes.length != 0) {
            CompoundTag nbt = NBTIO.read(nbtBytes);
            item.setCompoundTag(nbt);
        }
        double price = results.getDouble("price");

        results.close();
        statement.close();

        LinkedHashMap<Item, Double> map = new LinkedHashMap<>();
        map.put(item, price);
        return map;
    }

    public static Item getItem(int itemRecordId) throws SQLException, IOException {
        String query = "SELECT id, nbt FROM buy_items WHERE record_id = ?";
        PreparedStatement statement = Trading.getDbConnection().prepareStatement(query);
        statement.setInt(1, itemRecordId);
        ResultSet results = statement.executeQuery();
        results.next();
        Item item = Item.fromString(results.getString("id"));
        byte[] nbtBytes = results.getBytes("nbt");

        results.close();
        statement.close();

        if (nbtBytes != null && nbtBytes.length != 0) {
            CompoundTag nbt = NBTIO.read(nbtBytes);
            item.setCompoundTag(nbt);
        }

        return item;
    }

    public static String buildDescription(int itemRecordId) throws SQLException, IOException {
        return null;
    }

    public static void addBuyItem(int shopId, int categoryId, Item item, double price) throws SQLException, IOException {
        String query = "INSERT INTO buy_items(shop_id, category_id, id, price, nbt) VALUES(?, ?, ?, ?, ?)";
        PreparedStatement statement = Trading.getDbConnection().prepareStatement(query);
        String id = item.getId() + ":" + item.getDamage();
        byte[] nbtBytes = item.getNamedTag() == null ? null : NBTIO.write(item.getNamedTag());
        statement.setInt(1, shopId);
        statement.setInt(2, categoryId);
        statement.setString(3, id);
        statement.setDouble(4, price);
        statement.setBytes(5, nbtBytes);
        statement.executeUpdate();
        statement.close();
    }

    public static void deleteBuyItem(int itemId) throws SQLException {
        String query = "DELETE FROM buy_items WHERE record_id = ?";
        PreparedStatement statement = Trading.getDbConnection().prepareStatement(query);
        statement.setInt(1, itemId);
        statement.executeUpdate();
        statement.close();
    }

    public static void addBuyEnchantment(int itemId, int id, int level) throws SQLException, IOException {
        String query1 = "SELECT id, nbt FROM buy_items WHERE record_id = ?";
        PreparedStatement statement1 = Trading.getDbConnection().prepareStatement(query1);
        statement1.setInt(1, itemId);
        ResultSet result = statement1.executeQuery();
        result.next();
        Item item = Item.fromString(result.getString("id"));
        byte[] nbtBytes = result.getBytes("nbt");
        statement1.close();
        if (nbtBytes != null && nbtBytes.length != 0) {
            CompoundTag nbt1 = NBTIO.read(nbtBytes);
            item.setCompoundTag(nbt1);
        }

        Enchantment enchantment = Enchantment.get(id).setLevel(level);
        item.addEnchantment(enchantment);

        byte[] newNbtBytes = NBTIO.write(item.getNamedTag()); // certainly not null

        String query2 = "UPDATE buy_items SET nbt = ? WHERE record_id = ?";
        PreparedStatement statement2 = Trading.getDbConnection().prepareStatement(query2);
        statement2.setBytes(1, newNbtBytes);
        statement2.setInt(2, itemId);
        statement2.executeUpdate();
        statement2.close();
    }

    public static void removeBuyEnchantment(int itemId, Enchantment enchantment) throws SQLException, IOException {
        Item item = getItem(itemId);

        Item newItem = Item.fromString(item.getId() + ":" + item.getDamage());
        if (!item.getCustomName().equals("")) newItem.setCustomName(item.getCustomName());
        if (item.getLore() != null && item.getLore().length != 0) {
            newItem.setLore(item.getLore());
        }


        for (Enchantment enchantment1 : item.getEnchantments()) {
            if (enchantment1.getId() != enchantment.getId()) {
                newItem.addEnchantment(enchantment1);
            }
        }

        byte[] newNbtBytes = newItem.getNamedTag() == null ? null : NBTIO.write(newItem.getNamedTag());

        String query = "UPDATE buy_items SET nbt = ? WHERE record_id = ?";
        PreparedStatement statement = Trading.getDbConnection().prepareStatement(query);
        statement.setBytes(1, newNbtBytes);
        statement.setInt(2, itemId);
        statement.executeUpdate();
        statement.close();
    }

}