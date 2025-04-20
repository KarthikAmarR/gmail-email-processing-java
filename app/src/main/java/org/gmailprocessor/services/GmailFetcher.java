package org.gmailprocessor.services;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.List;

import org.gmailprocessor.pojos.Email;

public class GmailFetcher {

    public static void fetchEmails() throws IOException, GeneralSecurityException, SQLException {
        Gmail service = GmailAuthenticator.getService();
        List<Message> messages = fetchLatestEmails(service);

        if (messages == null || messages.isEmpty()) {
            System.out.println("No new emails found.");
            return;
        }

        try (Connection conn = connectToDatabase()) {
            createEmailsTable(conn);

            for (Message msg : messages) {
                Email email = processEmail(service, msg);
                insertEmail(conn, email);
            }
        }
    }

    private static List<Message> fetchLatestEmails(Gmail service) throws IOException {
        ListMessagesResponse response = service.users().messages()
                .list("me")
                .setMaxResults(10L)
                .execute();
        return response.getMessages();
    }

    private static Connection connectToDatabase() throws SQLException {
        String dbUrl = "jdbc:sqlite:data/emails.db";
        Connection conn = DriverManager.getConnection(dbUrl);
        if (conn == null) throw new SQLException("Failed to connect to the database.");
        return conn;
    }

    private static void createEmailsTable(Connection conn) throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS emails (
                email_id TEXT PRIMARY KEY,
                sender TEXT,
                subject TEXT,
                message TEXT,
                received_at TEXT
            );
            """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    private static Email processEmail(Gmail service, Message msg) throws IOException {
        String msgId = msg.getId();
        System.out.println("🔍 Processing email with Gmail ID: " + msgId);

        Message fullMessage = service.users().messages().get("me", msgId).execute();

        String sender = extractHeader(fullMessage, "From");
        String subject = extractHeader(fullMessage, "Subject");
        String receivedAt = extractReceivedTimestamp(fullMessage);
        String messageBody = extractMessageBody(fullMessage.getPayload());

        return new Email(msgId, sender, subject, messageBody, receivedAt);
    }

    private static void insertEmail(Connection conn, Email email) throws SQLException {
        String insertSQL = "INSERT OR IGNORE INTO emails (email_id, sender, subject, message, received_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, email.id);
            pstmt.setString(2, email.sender);
            pstmt.setString(3, email.subject);
            pstmt.setString(4, email.message);
            pstmt.setString(5, email.receivedAt);
            pstmt.executeUpdate();
        }
    }

    private static String extractHeader(Message message, String headerName) {
        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if (header.getName().equalsIgnoreCase(headerName)) {
                return header.getValue();
            }
        }
        return "Unknown";
    }

    private static String extractReceivedTimestamp(Message message) {
        Long timestamp = message.getInternalDate(); 
        if (timestamp != null) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date(timestamp)); 
        }
        return "Unknown";
    }
    private static String extractMessageBody(MessagePart payload) {
        if (payload.getParts() != null) {
            for (MessagePart part : payload.getParts()) {
                if (part.getBody() != null && part.getBody().getData() != null) {
                    return new String(java.util.Base64.getUrlDecoder().decode(part.getBody().getData()));
                }
            }
        } else if (payload.getBody() != null && payload.getBody().getData() != null) {
            return new String(java.util.Base64.getUrlDecoder().decode(payload.getBody().getData()));
        }
        return "(No message body found)";
    }
}