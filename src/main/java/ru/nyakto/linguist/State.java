package ru.nyakto.linguist;

import java.util.Objects;

public class State {
    private final FSM fsm;
    private final long id;

    public State(FSM fsm, long id) {
        Objects.requireNonNull(fsm);
        this.fsm = fsm;
        this.id = id;
    }

    public final long getId() {
        return id;
    }

    public final boolean isFinal() {
        return fsm.isFinal(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return id == ((State) obj).id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "State #" + id;
    }
}
