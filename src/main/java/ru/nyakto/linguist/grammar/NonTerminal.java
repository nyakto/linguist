package ru.nyakto.linguist.grammar;

public final class NonTerminal implements RuleItem {
    private final int id;

    public NonTerminal(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean visit(RuleWalker walker) {
        return walker.visitNonTerminal(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof NonTerminal) {
            return id == ((NonTerminal) obj).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
