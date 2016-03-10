package ru.nyakto.linguist.grammar;

import java.util.List;

public class Rule {
    private final NonTerminal lhs;
    private final List<RuleItem> rhs;

    public Rule(NonTerminal lhs, List<RuleItem> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public NonTerminal getLhs() {
        return lhs;
    }

    public List<RuleItem> getRhs() {
        return rhs;
    }
}
