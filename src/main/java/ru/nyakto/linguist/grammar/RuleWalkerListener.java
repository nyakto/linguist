package ru.nyakto.linguist.grammar;

public interface RuleWalkerListener {
    default public boolean visitTerminal(int position, Terminal item) {
        return true;
    }

    default public boolean visitNonTerminal(int position, NonTerminal item) {
        return true;
    }
}
