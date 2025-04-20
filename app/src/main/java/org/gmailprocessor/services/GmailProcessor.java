package org.gmailprocessor.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import org.gmailprocessor.pojos.Email;
import org.gmailprocessor.pojos.Rule;

public class GmailProcessor {

    private static final Path DB_PATH = Paths.get("data", "emails.db");
    private static final Path RULES_PATH = Paths.get("bin", "main", "rules.json");

    public static void processEmails() {
        try {
            validatePaths(DB_PATH, RULES_PATH);

            List<Rule> rules = loadRules(RULES_PATH);
            List<Email> emails = fetchEmailsFromDb(DB_PATH);

            System.out.println("Found " + emails.size() + " emails to process.");

            applyRulesToEmails(emails, rules);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void validatePaths(Path... paths) throws FileNotFoundException {
        for (Path path : paths) {
            if (!Files.exists(path)) {
                throw new FileNotFoundException("Required file not found: " + path.toAbsolutePath());
            }
        }
    }

    private static List<Rule> loadRules(Path rulesPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = Files.newInputStream(rulesPath)) {
            return Arrays.asList(mapper.readValue(inputStream, Rule[].class));
        }
    }

    private static List<Email> fetchEmailsFromDb(Path dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        List<Email> emails = new ArrayList<>();
    
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM emails")) {
    
            while (rs.next()) {
                Email email = new Email(
                        rs.getString("email_id"),
                        rs.getString("sender"), 
                        rs.getString("subject"),
                        rs.getString("message"), 
                        rs.getString("received_at")
                );
                emails.add(email);
            }
        }
        return emails;
    }

    private static void applyRulesToEmails(List<Email> emails, List<Rule> rules) {
        for (Email email : emails) {
            for (Rule rule : rules) {
                if (email.matches(rule)) {
                    System.out.println("Match: " + email.subject + " matches rule: " + rule);
                }
            }
        }
    }
} 
