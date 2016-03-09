package ru.nyakto.linguist;

public class BasicState implements State {
    private final int id;

    public BasicState(int id) {
        this.id = id;
    }

    @Override
    public final int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof BasicState) {
            return id == ((BasicState) obj).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "State #" + id;
    }
}
