package ru.nyakto.linguist;

public interface Walker<Symbol> {
    public boolean go(Symbol by);

    public boolean isInFinalState();
}
