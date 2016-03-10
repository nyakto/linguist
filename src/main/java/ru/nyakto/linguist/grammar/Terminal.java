package ru.nyakto.linguist.grammar;

public final class Terminal implements RuleItem {
    private final int id;

    public Terminal(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean visit(RuleWalker walker) {
        return walker.visitTerminal();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof Terminal) {
            return id == ((Terminal) obj).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
