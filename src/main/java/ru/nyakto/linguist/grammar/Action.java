package ru.nyakto.linguist.grammar;

public class Action implements RuleItem {
    @Override
    public final boolean visit(RuleWalker walker) {
        return walker.visitAction(this);
    }
}
