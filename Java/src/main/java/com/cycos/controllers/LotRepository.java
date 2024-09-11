package com.cycos.controllers;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@Valid
public class LotRepository {

    @Value("${url}")
    String url = "jdbc:postgresql://192.168.51.71:5432/Parking";
    @Value("${dbuser}")
    String user = "postgres";
    @Value("${dbupassword}")
    String password = "5258318";

    public Connection conn;

    @PostConstruct
    public void init() {
        log.info("from Config in init: {} {} {}", user, password, url);
        log.info("\n\nCREATED A LOT REPOSITORY.....YEAH !!!!\n\n");
        try {
            conn = DriverManager.getConnection(url, user, password);
            if (conn != null) {
                log.info("Connected to the database");
            }
        } catch (SQLException e) {
            log.error("Failed to connect to the database", e);
            throw new RuntimeException(e);
        }
    }

    public Lot selectLot(int lotNumber) {
        Lot lot = null;
        String sql = "SELECT \"Number\", \"State\", \"Rssi\", \"LastSeen\", \"Mac\",\"batterystate\" FROM \"Lots\" WHERE \"Number\"=?";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lotNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    lot = new Lot();
                    lot.setNumber(rs.getInt("Number"));
                    lot.setStatus(rs.getInt("State"));
                    lot.setRssi(rs.getInt("Rssi"));
                    lot.setLastSeen((rs.getLong("lastseen")));
                    lot.setMac(rs.getString("Mac").charAt(0));
                    lot.setBatteryState(rs.getInt("batterystate"));
                    log.trace("Lot retrieved ::= [{}]", lot);
                }
            }
        } catch (SQLException ex) {
            log.warn("Exception during database connection ::= ", ex);
        }
        return lot;
    }

    public List<Lot> selectAllLots() {
        List<Lot> lots = new ArrayList<>();
        String sql = "SELECT \"lot\", \"state\", \"rssi\", \"lastseen\", \"mac\",\"batterystate\" FROM \"states\"";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Lot lot = new Lot();
                lot.setNumber(rs.getInt("lot"));
                lot.setStatus(rs.getInt("state"));
                lot.setRssi(rs.getInt("rssi"));
                lot.setLastSeen(rs.getLong("lastseen"));
                lot.setMac(rs.getString("mac").charAt(0));
                lot.setBatteryState(rs.getInt("batterystate"));
                lots.add(lot);
            }

        } catch (SQLException e) {
            log.error("Exception during selectAllLots operation", e);
        }

        return lots;
    }

    public int returnLot(String mac) {
        String sql = "SELECT lot FROM sensors WHERE mac=?";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mac);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("lot");
                } else {
                    log.debug("No lot found for this MAC address");
                    return -1;
                }
            }
        } catch (SQLException e) {
            log.error("Exception during returnLot operation", e);
            throw new RuntimeException(e);
        }
    }

    public String upsertLots(String mac, int lot, int state, float batteryState, int rssi) {
        long lastSeen = System.currentTimeMillis();

        BigDecimal roundedBatteryState = new BigDecimal(Float.toString(batteryState));
        roundedBatteryState = roundedBatteryState.setScale(2, BigDecimal.ROUND_HALF_UP);

        String sql = "INSERT INTO states (mac, lot, state, rssi, lastseen, batterystate) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (mac) DO UPDATE SET state = EXCLUDED.state, rssi = EXCLUDED.rssi, lastseen = EXCLUDED.lastseen, batterystate = EXCLUDED.batterystate";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mac);
            pstmt.setInt(2, lot);
            pstmt.setInt(3, state);
            pstmt.setInt(4, rssi);
            pstmt.setLong(5, lastSeen);
            pstmt.setBigDecimal(6, roundedBatteryState);
            pstmt.executeUpdate();
            return "Success";
        } catch (SQLException e) {
            log.error("Exception during upsertLots operation", e);
            return "Error: " + e.getMessage();
        }
    }

    public String upsertSensors(String mac, int lot) {
        String sql = "INSERT INTO sensors (mac, lot) VALUES (?, ?) " +
                "ON CONFLICT (mac) DO UPDATE SET lot = EXCLUDED.lot";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mac);
            pstmt.setInt(2, lot);
            pstmt.executeUpdate();
            return "Success";
        } catch (SQLException e) {
            log.error("Exception during upsertSensors operation", e);
            return "Error: " + e.getMessage();
        }
    }

    public String deleteSensors(String mac) {
        String sql = "DELETE FROM sensors WHERE mac= ?;";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mac);
            pstmt.executeUpdate();
            return "Success";
        } catch (SQLException e) {
            log.error("Exception during deleteSensors operation", e);
            throw new RuntimeException(e);
        }
    }

    public String deleteLot(String macAddress) {
        String sql = "DELETE FROM states WHERE mac = ?;";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, macAddress);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return "Success";
            } else {
                return "No lot found with the given MAC address.";
            }
        } catch (SQLException e) {
            log.error("Failed to delete lot", e);
            return "Error: " + e.getMessage();
        }
    }

    public int macAddressExists(String mac) {
        String sql = "SELECT lot FROM sensors WHERE mac = ?;";
        log.debug("SQL statement ::= [{}]", sql);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mac);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("lot");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to check MAC address", e);
            return -1;
        }
    }

    // Methode zum Schließen der Datenbankverbindung
    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                log.info("Database connection closed");
            } catch (SQLException e) {
                log.error("Failed to close the database connection", e);
            }
        }
    }
}
