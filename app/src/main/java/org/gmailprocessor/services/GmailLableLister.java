package org.gmailprocessor.services;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GmailLableLister {
    public static void listLables() throws IOException, GeneralSecurityException {
        Gmail service = GmailAuthenticator.getService();

        String user = "me";
        ListLabelsResponse listResponse = service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();

        if (labels.isEmpty()) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }
    }
}