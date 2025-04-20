package org.gmailprocessor.pojos;

import java.util.List;

public class Rule {
    public String field;
    public String predicate;
    public String value;
    public List<String> actions;  // or whatever type your actions are

    public Rule() {}

    public Rule(String field, String predicate, String value, List<String> actions) {
        this.field = field;
        this.predicate = predicate;
        this.value = value;
        this.actions = actions;
    }

    @Override
    public String toString() {
        return String.format("[%s %s %s] -> Actions: %s", field, predicate, value, actions);
    }
}
