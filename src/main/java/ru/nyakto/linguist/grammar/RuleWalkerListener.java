package ru.nyakto.linguist.grammar;

public interface RuleWalkerListener {
    default public boolean visitTerminal(Rule rule, int position, Terminal item) {
        return true;
    }

    default public boolean visitNonTerminal(Rule rule, int position, NonTerminal item) {
        return true;
    }
}
