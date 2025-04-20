package org.gmailprocessor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

import org.gmailprocessor.services.DatabaseSetup;
import org.gmailprocessor.services.GmailFetcher;
import org.gmailprocessor.services.GmailLableLister;
import org.gmailprocessor.services.GmailProcessor;

public class App {
    public static void main(String[] args) throws IOException, GeneralSecurityException, SQLException {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "setup":
                    DatabaseSetup.setup();
                    break;

                case "fetch":
                    GmailFetcher.fetchEmails();
                    break;

                case "labels":
                    GmailLableLister.listLables();
                    break;

                case "process":
                    GmailProcessor.processEmails();
                    break;

                default:
                    System.out.println("⚠️ Unknown command. Use 'setup', 'fetch', 'labels', or 'process'");
            }
        } else {
            System.out.println("📨 Running email processor in default mode...");
        }
    }
}