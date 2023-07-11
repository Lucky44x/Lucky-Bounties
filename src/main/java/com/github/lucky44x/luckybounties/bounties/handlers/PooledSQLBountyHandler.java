package com.github.lucky44x.luckybounties.bounties.handlers;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.github.lucky44x.luckybounties.SQL.HikariConnectionPool;
import com.github.lucky44x.luckybounties.abstraction.bounties.Bounty;
import com.github.lucky44x.luckybounties.abstraction.bounties.BountyHandler;
import com.github.lucky44x.luckybounties.bounties.types.EcoBounty;
import com.github.lucky44x.luckybounties.bounties.types.ItemBounty;
import com.github.lucky44x.luckybounties.user.UserStats;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class PooledSQLBountyHandler extends BountyHandler {
    private final HikariConnectionPool pool;
    private final String sqlPassword;
    private final String sqlUserName;
    @Getter
    private final String url;

    public PooledSQLBountyHandler(LuckyBounties instance, String hostName, int port, String databaseName, String userName, String password) {
        super(instance);

        pool = new HikariConnectionPool(instance);
        url = "jdbc:" + instance.configFile.getSqlSystemName().toLowerCase() + "://" + hostName + ":" + port + "/" + databaseName;
        sqlUserName = userName;
        sqlPassword = password;

        instance.getLogger().info("[SQL Handler] Opening Hikari-Connection-Pool");
        pool.openPool(url, userName, password, instance.configFile.getSQLDriverClassName());

        finishInit();
    }

    @Override
    public Bounty[] getBountiesByTarget(UUID target) {
        try(Connection connection = this.pool.getConnection()) {
            ArrayList<Bounty> bounties = new ArrayList<>();

            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM bounties WHERE target_uuid = ? AND is_active = true")){
                statement.setString(1, target.toString());

                ResultSet rs = statement.executeQuery();
                while(rs.next()){
                    UUID setterUUID = UUID.fromString(rs.getString("setter_uuid"));
                    UUID targetUUID = UUID.fromString(rs.getString("target_uuid"));
                    long setEpochTime = rs.getLong("set_at");
                    double ecoReward = rs.getDouble("eco_reward");
                    byte[] itemReward = rs.getBytes("item_reward");

                    if(ecoReward < 0){
                        bounties.add(new ItemBounty(blobDecode(itemReward), targetUUID, setterUUID, setEpochTime, instance));
                    }
                    else{
                        bounties.add(new EcoBounty(ecoReward, targetUUID, setterUUID, setEpochTime, instance));
                    }
                }

            }

            return bounties.toArray(Bounty[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Bounty[0];
    }

    @Override
    public Bounty[] getBountiesBySetter(UUID setter) {
        try(Connection connection = this.pool.getConnection()) {
            ArrayList<Bounty> bounties = new ArrayList<>();

            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM bounties WHERE setter_uuid = ? AND is_active = true")){
                statement.setString(1, setter.toString());

                ResultSet rs = statement.executeQuery();
                while(rs.next()){
                    UUID setterUUID = UUID.fromString(rs.getString("setter_uuid"));
                    UUID targetUUID = UUID.fromString(rs.getString("target_uuid"));
                    long setEpochTime = rs.getLong("set_at");
                    double ecoReward = rs.getDouble("eco_reward");
                    byte[] itemReward = rs.getBytes("item_reward");

                    if(ecoReward < 0){
                        bounties.add(new ItemBounty(blobDecode(itemReward), targetUUID, setterUUID, setEpochTime, instance));
                    }
                    else{
                        bounties.add(new EcoBounty(ecoReward, targetUUID, setterUUID, setEpochTime, instance));
                    }
                }

            }

            return bounties.toArray(Bounty[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Bounty[0];
    }

    @Override
    public double getEcoAmount(UUID target) {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT CAST(SUM(eco_reward) as double) as ecosum FROM bounties WHERE" +
                    " eco_reward > 0 AND" +
                    " target_uuid = ? AND" +
                    " is_active = true")){
                statement.setString(1, target.toString());

                ResultSet rs = statement.executeQuery();
                if(!rs.next())
                    return 0;

                return rs.getDouble("ecosum");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public UUID[] getAllTargets() {
        try(Connection connection = pool.getConnection()) {
            ArrayList<UUID> uuids = new ArrayList<>();

            try(PreparedStatement statement = connection.prepareStatement(
                    "SELECT target_uuid FROM bounties"
            )){
                ResultSet set = statement.executeQuery();
                while(set.next()){
                    String id = set.getString("target_uuid");
                    uuids.add(UUID.fromString(id));
                }
            }

            return uuids.toArray(UUID[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new UUID[0];
    }

    @Override
    public UUID[] getAllUsers() {
        try(Connection connection = pool.getConnection()) {
            ArrayList<UUID> uuids = new ArrayList<>();

            try(PreparedStatement statement = connection.prepareStatement(
                    "SELECT user_uuid FROM users"
            )){
                ResultSet set = statement.executeQuery();
                while(set.next()){
                    String id = set.getString("user_uuid");
                    uuids.add(UUID.fromString(id));
                }
            }

            return uuids.toArray(UUID[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new UUID[0];
    }

    @Override
    public int getGlobalBountyNum() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT CAST(COUNT(target_uuid) AS int) AS bountiesnum FROM bounties WHERE is_active = true")){

                ResultSet rs = statement.executeQuery();
                if(!rs.next())
                    return 0;

                return rs.getInt("bountiesnum");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int getMaxBountyNum() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT CAST(COUNT(*) AS int) AS entry_count " +
                    "FROM bounties " +
                    "WHERE is_active = true " +
                    "GROUP BY target_uuid " +
                    "ORDER BY entry_count DESC " +
                    "LIMIT 1")){

                ResultSet rs = statement.executeQuery();
                if(!rs.next())
                    return 0;

                return rs.getInt("entry_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public String getMaxBountyName() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT target_uuid " +
                    "FROM bounties " +
                    "WHERE is_active = true " +
                    "GROUP BY target_uuid " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT 1")){

                ResultSet rs = statement.executeQuery();
                if(!rs.next())
                    return "NAN";

                return Bukkit.getOfflinePlayer(
                        UUID.fromString(
                                rs.getString("target_uuid")
                        )
                ).getName();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getMaxEcoBountyName() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT target_uuid " +
                    "FROM bounties " +
                    "WHERE is_active = true " +
                    "AND eco_reward > 0 " +
                    "GROUP BY target_uuid " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT 1")){

                ResultSet rs = statement.executeQuery();
                if(!rs.next())
                    return "NAN";

                return Bukkit.getOfflinePlayer(
                        UUID.fromString(
                                rs.getString("target_uuid")
                        )
                ).getName();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public double getMaxEcoBountyAmount() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT target_uuid, " +
                    "CAST(SUM(eco_reward) AS double) AS ecosum " +
                    "FROM bounties " +
                    "WHERE is_active = true " +
                    "AND eco_reward > 0 " +
                    "GROUP BY target_uuid " +
                    "ORDER BY ecosum DESC " +
                    "LIMIT 1")){

                ResultSet rs = statement.executeQuery();
                if(!rs.next())
                    return 0;

                return rs.getDouble("ecosum");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public double getGlobalEcoAmount() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT CAST(SUM(eco_reward) as double) as eco_sum " +
                    "FROM bounties " +
                    "WHERE is_active = true " +
                    "AND eco_reward > 0 ")){

                ResultSet rs = statement.executeQuery();
                if(!rs.next())
                    return 0;

                return rs.getDouble("ecosum");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void checkForExpiredTargetBounties(Player target) {
        try(Connection connection = this.pool.getConnection()) {

            if(!instance.configFile.isBountiesExpire())
                return;

            if(instance.configFile.isExpiredBountiesReturn()){
                try(PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bounties SET is_active = false " +
                                "WHERE target_uuid = ? " +
                                "AND (? - set_at) > ? " +
                                "AND is_active = true")){

                    statement.setString(1, target.getUniqueId().toString());
                    statement.setLong(2, System.currentTimeMillis());
                    statement.setLong(3, instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime()));

                    statement.executeUpdate();
                }

                returnExpiredBounties();
            }
            else{
                try(PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bounties " +
                                "WHERE target_uuid = ? " +
                                "AND (? - set_at) > ? " +
                                "AND is_active = true")){

                    statement.setString(1, target.getUniqueId().toString());
                    statement.setLong(2, System.currentTimeMillis());
                    statement.setLong(3, instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime()));

                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checkForExpiredBounties() {
        try(Connection connection = this.pool.getConnection()) {
            if(!instance.configFile.isBountiesExpire())
                return;

            if(instance.configFile.isExpiredBountiesReturn()){
                try(PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bounties SET is_active = false " +
                                "WHERE (? - set_at) > ? " +
                                "AND is_active = true")){

                    statement.setLong(1, System.currentTimeMillis());
                    statement.setLong(2, instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime()));

                    statement.executeUpdate();
                }

                returnExpiredBounties();
            }
            else{
                try(PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bounties " +
                                "WHERE (? - set_at) > ? " +
                                "AND is_active = true")){

                    statement.setLong(1, System.currentTimeMillis());
                    statement.setLong(2, instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime()));

                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checkForExpiredSetterBounties(Player setter) {
        try(Connection connection = this.pool.getConnection()) {
            if(!instance.configFile.isBountiesExpire())
                return;

            if(instance.configFile.isExpiredBountiesReturn()){
                try(PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bounties SET is_active = false " +
                                "WHERE setter_uuid = ? " +
                                "AND (? - set_at) > ? " +
                                "AND is_active = true")){

                    statement.setString(1, setter.getUniqueId().toString());
                    statement.setLong(2, System.currentTimeMillis());
                    statement.setLong(3, instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime()));

                    statement.executeUpdate();
                }

                returnExpiredBounties(setter);
            }
            else{
                try(PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bounties " +
                                "WHERE setter_uuid = ? " +
                                "AND (? - set_at) > ? " +
                                "AND is_active = true")){

                    statement.setString(1, setter.getUniqueId().toString());
                    statement.setLong(2, System.currentTimeMillis());
                    statement.setLong(3, instance.configFile.toMillisecTime(instance.configFile.getBountyLifetime()));

                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bounty[] getReturnBuffer(UUID user) {
        try(Connection connection = this.pool.getConnection()) {
            ArrayList<Bounty> bounties = new ArrayList<>();
            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM bounties " +
                    "WHERE is_active = false " +
                    "AND setter_uuid = ?")){
                statement.setString(1, user.toString());
                ResultSet rs = statement.executeQuery();

                while(rs.next()){
                    UUID setterUUID = UUID.fromString(rs.getString("setter_uuid"));
                    UUID targetUUID = UUID.fromString(rs.getString("target_uuid"));
                    long setEpochTime = rs.getLong("set_at");
                    double ecoReward = rs.getDouble("eco_reward");
                    byte[] itemReward = rs.getBytes("item_reward");

                    if(ecoReward < 0){
                        bounties.add(new ItemBounty(blobDecode(itemReward), targetUUID, setterUUID, setEpochTime, instance));
                    }
                    else{
                        bounties.add(new EcoBounty(ecoReward, targetUUID, setterUUID, setEpochTime, instance));
                    }
                }
            }
            return bounties.toArray(Bounty[]::new);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Bounty[0];
    }

    @Override
    public void addBounty(Bounty bounty) {
        try(Connection connection = this.pool.getConnection()) {
            if(!checkUserExists(bounty.getSetterID()))
                createUser(bounty.getSetterID());

            if(!checkUserExists(bounty.getTargetID()))
                createUser(bounty.getTargetID());

            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO bounties VALUES (" +
                    "?," +
                    "?," +
                    "?," +
                    "?," +
                    "?," +
                    "?" +
                    ")")){
                statement.setString(1, bounty.getSetterStringID());
                statement.setString(2, bounty.getTargetID().toString());
                statement.setBoolean(3, true);
                statement.setLong(4, bounty.getSetTime());
                statement.setDouble(5, bounty instanceof ItemBounty ? -1 : ((EcoBounty)bounty).getReward());
                statement.setBytes(6, bounty instanceof EcoBounty ? new byte[0] : blobEncode(((ItemBounty) bounty).getReward()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addInternalBounty(Bounty bounty, boolean active){

        try(Connection connection = pool.getConnection()){
            if(!checkUserExists(bounty.getSetterID()))
                createUser(bounty.getSetterID());

            if(!checkUserExists(bounty.getTargetID()))
                createUser(bounty.getTargetID());

            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO bounties VALUES (" +
                    "?," +
                    "?," +
                    "?," +
                    "?," +
                    "?," +
                    "?" +
                    ")")){
                statement.setString(1, bounty.getSetterStringID());
                statement.setString(2, bounty.getTargetID().toString());
                statement.setBoolean(3, active);
                statement.setLong(4, bounty.getSetTime());
                statement.setDouble(5, bounty instanceof ItemBounty ? -1 : ((EcoBounty)bounty).getReward());
                statement.setBytes(6, bounty instanceof EcoBounty ? new byte[0] : blobEncode(((ItemBounty) bounty).getReward()));
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeBounty(Bounty bounty) {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM bounties WHERE " +
                    "target_uuid = ? AND " +
                    "setter_uuid = ? AND " +
                    "set_at = ?")){

                statement.setString(1, bounty.getTargetID().toString());
                statement.setString(2, bounty.getSetterStringID());
                statement.setLong(3, bounty.getSetTime());
                statement.executeUpdate();
                return true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void moveBountyToReturn(Bounty bounty) {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("UPDATE bounties SET is_active = false WHERE " +
                    "target_uuid = ? AND " +
                    "setter_uuid = ? AND " +
                    "set_at = ?")){

                statement.setString(1, bounty.getTargetID().toString());
                statement.setString(2, bounty.getSetterStringID());
                statement.setLong(3, bounty.getSetTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addBounty(ItemStack payment, Player target, Player setter) {
        addBounty(new ItemBounty(payment, target, setter, instance));
    }

    @Override
    public void addBounty(double payment, Player target, Player setter) {
        addBounty(new EcoBounty(payment, target, setter, instance));
    }

    @Override
    public void clearBounties(Player target) {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM bounties WHERE " +
                    "target_uuid = ? " +
                    "AND is_active = true")){
                statement.setString(1, target.getUniqueId().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearReturnBuffer(UUID target) {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM bounties WHERE " +
                    "setter_uuid = ? AND " +
                    "is_active = false")){

                statement.setString(1, target.toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected UserStats getUserStats(UUID id) {
        try(Connection connection = this.pool.getConnection()) {
            if(!checkUserExists(id))
                createUser(id);

            try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE user_uuid = ?")){
                statement.setString(1, id.toString());

                ResultSet rs = statement.executeQuery();
                UserStats stats = null;
                while(rs.next()){
                    int statTaken = rs.getInt("stat_taken");
                    int statReceived = rs.getInt("stat_received");
                    int statSet = rs.getInt("stat_set");

                    stats = new UserStats(statSet, statReceived, statTaken);
                }

                return stats;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public UUID getUserMaxBountiesTaken() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT user_uuid FROM users WHERE stat_taken = " +
                    "(SELECT MAX(stat_taken) FROM users)")){
                ResultSet rs = statement.executeQuery();

                UUID ret = null;
                while(rs.next()){
                    ret = UUID.fromString(rs.getString("user_uuid"));
                }

                return ret;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public UUID getUserMaxBountiesReceived() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT user_uuid FROM users WHERE stat_received = " +
                    "(SELECT MAX(stat_received) FROM users)")){
                ResultSet rs = statement.executeQuery();

                UUID ret = null;
                while(rs.next()){
                    ret = UUID.fromString(rs.getString("user_uuid"));
                }

                return ret;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public UUID getUserMaxBountiesSet() {
        try(Connection connection = this.pool.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("SELECT user_uuid FROM users WHERE stat_set = " +
                    "(SELECT MAX(stat_set) FROM users)")){
                ResultSet rs = statement.executeQuery();

                UUID ret = null;
                while(rs.next()){
                    ret = UUID.fromString(rs.getString("user_uuid"));
                }

                return ret;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void resetStats(UUID user) {
        try(Connection connection = this.pool.getConnection()) {
            if(!checkUserExists(user))
                createUser(user);

            try(PreparedStatement statement = connection.prepareStatement("UPDATE users SET " +
                    "stat_taken = 0, " +
                    "stat_received = 0, " +
                    "stat_set = 0 " +
                    "WHERE user_uuid = ?")){
                statement.setString(1, user.toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addStatTaken(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_taken = stat_taken + 1 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subtractStatTaken(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_taken = stat_taken - 1 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeStatTaken(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_taken = 0 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addStatSet(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_set = stat_set + 1 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subtractStatSet(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_set = stat_set - 1 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeStatSet(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_set = 0 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addStatReceived(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_received = stat_received + 1 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subtractStatReceived(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_received = stat_received - 1 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeStatReceived(UUID target) {
        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target))
                createUser(target);

            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET stat_received = 0 WHERE user_uuid = ?"
            )){
                statement.setString(1, target.toString());
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertUser(UUID target, UserStats stats) {

        if(target == null)
            target = instance.getServerUUID();

        try(Connection connection = pool.getConnection()){

            if(!checkUserExists(target)){
                try(PreparedStatement statement = connection.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?)")){
                    statement.setString(1, target.toString());
                    statement.setInt(2, stats.getBountiesTaken());
                    statement.setInt(3, stats.getBountiesReceived());
                    statement.setInt(4, stats.getBountiesSet());
                    statement.executeUpdate();
                }
            }
            else{
                try(PreparedStatement statement = connection.prepareStatement("UPDATE users SET stat_taken = ?, stat_received = ?, stat_set = ? WHERE user_uuid = ?")){
                    statement.setInt(1, stats.getBountiesTaken());
                    statement.setInt(2, stats.getBountiesReceived());
                    statement.setInt(3, stats.getBountiesSet());
                    statement.setString(4, target.toString());
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onLoad() {
        instance.getLogger().info("[Pooled-SQL-Handler] Creating Tables");
        createUsersTable();
        createBountiesTable();
    }

    @Override
    protected void onSave() {
        instance.getLogger().info("[SQL Handler] Closing Hikari-Connection-Pool");
        this.pool.closePool();
    }

    @Override
    public void dropData() {
        try(Connection connection = this.pool.getConnection()) {
            /*
            This is by far not the best way to do this, see (https://stackoverflow.com/a/253858),
            but I'm too lazy to drop the constraints truncate the tables and then reapply the constraints
            */
            try(Statement stm = connection.createStatement()){
                stm.executeUpdate("DROP TABLE bounties");
                stm.executeUpdate("DROP TABLE users");
            }

            createUsersTable();
            createBountiesTable();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void transferDataFromLocal(LocalBountyHandler localHandler){
        for(Map.Entry<UUID, ArrayList<Bounty>> entry : localHandler.getTargetBounties().entrySet()){
            for(Bounty b : entry.getValue()){
                addInternalBounty(b, true);
            }
        }

        for(Map.Entry<UUID, ArrayList<Bounty>> entry : localHandler.getReturnBuffer().entrySet()){
            for(Bounty b : entry.getValue()){
                addInternalBounty(b, false);
            }
        }

        try(Connection connection = pool.getConnection()){
            for(Map.Entry<UUID, UserStats> entry : localHandler.getUserStats().entrySet()){
                if(!checkUserExists(entry.getKey()))
                    createUser(entry.getKey());

                try(PreparedStatement statement = connection.prepareStatement("UPDATE users SET stat_taken = ?, stat_received = ?, stat_set = ? WHERE user_uuid = ?")){
                    statement.setInt(1, entry.getValue().getBountiesTaken());
                    statement.setInt(2, entry.getValue().getBountiesReceived());
                    statement.setInt(3, entry.getValue().getBountiesSet());
                    statement.setString(4, entry.getKey().toString());
                    statement.executeUpdate();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkUserExists(UUID id){
        try(Connection connection = pool.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement("SELECT user_uuid FROM users WHERE user_uuid = ?")){
                statement.setString(1, id.toString());
                ResultSet rs = statement.executeQuery();
                return rs.next();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void createUser(UUID id){
        try(Connection connection = pool.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO users(user_uuid) VALUES (?)")){
                statement.setString(1, id.toString());
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createBountiesTable(){
        try(Connection connection = pool.getConnection()){
            try(Statement stm = connection.createStatement()){
                stm.executeUpdate("CREATE TABLE IF NOT EXISTS bounties(" +
                        "setter_uuid varchar(255), " +
                        "target_uuid varchar(255), " +
                        "is_active boolean DEFAULT true, " +
                        "set_at long, " +
                        "eco_reward double, " +
                        "item_reward BLOB, " +
                        "FOREIGN KEY (setter_uuid) REFERENCES users(user_uuid)" +
                        "ON DELETE CASCADE, " +
                        "FOREIGN KEY (target_uuid) REFERENCES users(user_uuid)" +
                        "ON DELETE CASCADE)");
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createUsersTable(){
        try(Connection connection = pool.getConnection()){
            try(Statement stm = connection.createStatement()){
                stm.executeUpdate("CREATE TABLE IF NOT EXISTS users(" +
                        "user_uuid varchar(255), " +
                        "stat_taken int unsigned DEFAULT 0, " +
                        "stat_received int unsigned DEFAULT 0, " +
                        "stat_set int unsigned DEFAULT 0, " +
                        "PRIMARY KEY (user_uuid))");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] blobEncode(ItemStack item){
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ItemStack blobDecode(byte[] data){
        try{
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            inputStream.close();

            return item;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //region LocalTransfer
    public void saveToDisk(){
        instance.getLogger().info("[Pooled-SQL-Handler] Saving sql data in folder-structure");
        File folder = new File(instance.getDataFolder() + "/bounties");
        if(!folder.exists()){
            folder.mkdirs();
        }

        for(UUID target : getAllTargets()){
            File targetFile = new File(instance.getDataFolder() + "/bounties/" + target.toString() + "/active.bounties");
            encodeBounties(getBountiesByTarget(target), targetFile);
        }

        for(UUID user : getAllUsers()){
            File userStatFile = new File(instance.getDataFolder() + "/bounties/" + user.toString() + "/stats.json");
            UserStats stats = getUserStats(user);
            writeStats(stats, userStatFile);

            if(getReturnBuffer(user).length == 0)
                continue;

            File returnFile = new File(instance.getDataFolder() + "/bounties/" + user.toString() + "/returnBuffer.bounties");
            encodeBounties(
                    getReturnBuffer(user),
                    returnFile
            );
        }
        instance.getLogger().info("[Pooled-SQL-Handler] Saved all sql data in folder-structure");
    }

    private void encodeBounties(Bounty[] bounties, File f){
        try{
            if(!f.exists()) {
                f.getParentFile().mkdirs();
            }
            else{
                f.delete();
            }

            f.createNewFile();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(bounties.length);
            for(Bounty b : bounties){
                dataOutput.writeBoolean((b instanceof ItemBounty));
                dataOutput.writeUTF(b.getTargetID().toString());
                dataOutput.writeUTF(b.getSetterStringID());
                dataOutput.writeLong(b.getSetTime());

                if(b instanceof ItemBounty)
                    dataOutput.writeObject(((ItemBounty) b).getReward());
                else if(b instanceof EcoBounty)
                    dataOutput.writeDouble(((EcoBounty) b).getReward());
            }

            dataOutput.close();

            FileWriter fileWriter = new FileWriter(f);
            PrintWriter pW = new PrintWriter(fileWriter);
            pW.print(new String(Base64Coder.encodeLines(outputStream.toByteArray())));
            pW.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeStats(UserStats stats, File f){
        try{
            if(!f.exists())
                f.getParentFile().mkdirs();
            else
                f.delete();

            f.createNewFile();

            try(JsonWriter writer = new JsonWriter(new FileWriter(f))){
                writer.setIndent("    ");
                writer.beginObject();
                writer.name("recievedBounties").value(stats.getBountiesReceived());
                writer.name("setBounties").value(stats.getBountiesSet());
                writer.name("takenBounties").value(stats.getBountiesTaken());
                writer.endObject();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean credentialsEqual(String password, String user) {
        return password.equals(sqlPassword) && user.equals(sqlUserName);
    }

    //endregion
}
