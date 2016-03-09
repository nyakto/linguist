package ru.nyakto.linguist;

import java.util.Objects;

public class BasicState implements State {
    private final FSM fsm;
    private final int id;

    public BasicState(FSM fsm, int id) {
        Objects.requireNonNull(fsm);
        this.fsm = fsm;
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    public final boolean isFinal() {
        return fsm.isFinal(this);
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
