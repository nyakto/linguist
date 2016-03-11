package ru.nyakto.linguist.grammar;

public class NonTerminal implements RuleItem {
    private final int id;

    public NonTerminal(int id) {
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    @Override
    public final boolean visit(RuleWalker walker) {
        return walker.visitNonTerminal(this);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof NonTerminal) {
            return id == ((NonTerminal) obj).id;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return id;
    }
}
