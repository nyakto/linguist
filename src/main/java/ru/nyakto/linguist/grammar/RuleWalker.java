package ru.nyakto.linguist.grammar;

import java.util.ListIterator;

public final class RuleWalker {
    private final Rule rule;
    private final RuleWalkerListener listener;
    private int position;

    public RuleWalker(Rule rule, RuleWalkerListener listener) {
        this.rule = rule;
        this.listener = listener;
    }

    private void update(int position) {
        this.position = position;
    }

    protected boolean visitTerminal(Terminal item) {
        return listener.visitTerminal(rule, position, item);
    }

    protected boolean visitNonTerminal(NonTerminal item) {
        return listener.visitNonTerminal(rule, position, item);
    }

    protected boolean visitAction(Action item) {
        return listener.visitAction(rule, position, item);
    }

    public static void walk(Rule rule, int position, RuleWalkerListener listener) {
        final RuleWalker walker = new RuleWalker(rule, listener);
        final ListIterator<RuleItem> iterator = rule.getRhs().listIterator(position);
        while (iterator.hasNext()) {
            final RuleItem item = iterator.next();
            walker.update(position);
            if (!item.visit(walker)) {
                return;
            }
            position++;
        }
    }

    public static void walk(Rule rule, RuleWalkerListener listener) {
        walk(rule, 0, listener);
    }
}
