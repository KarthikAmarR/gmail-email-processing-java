package org.gmailprocessor.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {
    public static void setup() {
        String url = "jdbc:sqlite:data/emails.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS emails (
                    email_id TEXT PRIMARY KEY,
                    sender TEXT,
                    subject TEXT,
                    message TEXT,
                    received_at TEXT,
                    is_read BOOLEAN DEFAULT 0
                );
            """);
            System.out.println("Database and table created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}