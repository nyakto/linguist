package ru.nyakto.linguist.grammar;

import java.util.ListIterator;

public final class RuleWalker {
    private final RuleWalkerListener listener;
    private int position;
    private RuleItem item;

    private RuleWalker(RuleWalkerListener listener) {
        this.listener = listener;
    }

    private void update(int position, RuleItem item) {
        this.position = position;
        this.item = item;
    }

    protected boolean visitTerminal() {
        return listener.visitTerminal(position, (Terminal) item);
    }

    protected boolean visitNonTerminal() {
        return listener.visitNonTerminal(position, (NonTerminal) item);
    }

    public static void walk(Rule rule, int position, RuleWalkerListener listener) {
        final RuleWalker walker = new RuleWalker(listener);
        final ListIterator<RuleItem> iterator = rule.getRhs().listIterator(position);
        while (iterator.hasNext()) {
            final RuleItem item = iterator.next();
            walker.update(position, item);
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
