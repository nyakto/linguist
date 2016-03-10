package ru.nyakto.linguist.grammar;

public class Terminal implements RuleItem {
    private final int id;

    public Terminal(int id) {
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    @Override
    public final boolean visit(RuleWalker walker) {
        return walker.visitTerminal(this);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof Terminal) {
            return id == ((Terminal) obj).id;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return id;
    }
}
