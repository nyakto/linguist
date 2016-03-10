package ru.nyakto.linguist.grammar;

public interface RuleItem {
    default public boolean visit(RuleWalker walker) {
        return true;
    }
}
