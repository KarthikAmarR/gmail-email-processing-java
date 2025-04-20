package org.gmailprocessor.pojos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Email {
    public String id, sender, subject, message, receivedAt;

    public Email(String id, String sender, String subject, String message, String receivedAt) {
        this.id = id;
        this.sender = sender;
        this.subject = subject;
        this.message = message;
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(this.id).append(" ").append(this.sender).append(" ").append(this.subject).toString();
    }

    public boolean matches(Rule rule) {
        String target = switch (rule.field.toLowerCase()) {
            case "sender" -> sender.toLowerCase();
            case "subject" -> subject.toLowerCase();
            case "message" -> message.toLowerCase();
            case "email_id" -> id.toLowerCase();
            case "received_at" -> receivedAt;
            default -> "";
        };

        String value = rule.value.toLowerCase();

        return switch (rule.predicate.toLowerCase()) {
            case "contains" -> target.contains(value);
            case "equals" -> target.equals(value);
            case "starts_with" -> target.startsWith(value);
            case "ends_with" -> target.endsWith(value);
            case "before" -> compareDate(target, value) < 0;
            case "after" -> compareDate(target, value) > 0;
            default -> false;
        };
    }

    private int compareDate(String d1, String d2) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dt1 = LocalDateTime.parse(d1, formatter);
            LocalDateTime dt2 = LocalDateTime.parse(d2, formatter);
            return dt1.compareTo(dt2);
        } catch (Exception e) {
            return 0;
        }
    }
}