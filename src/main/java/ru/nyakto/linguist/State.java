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

    public State copy(FSM fsm) {
        Objects.requireNonNull(fsm);
        return new State(fsm, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State cmp = (State) o;

        return id == cmp.id && fsm.equals(cmp.fsm);
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
