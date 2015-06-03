package ru.nyakto.linguist;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

abstract public class FSM<T extends State, Symbol> implements Cloneable {
    private final T initialState;
	private final Set<Long> finalStateIds = new HashSet<>();
	protected final StateFactory<T> stateFactory;
	protected final Set<State> states = new HashSet<>();

    public FSM(BiFunction<FSM, Long, T> stateConstructor) {
        stateFactory = new BasicStateFactory<>(this, stateConstructor);
        initialState = stateFactory.createState();
    }

    public final T getInitialState() {
        return initialState;
    }

    public final void markStateAsFinal(State state) {
        finalStateIds.add(state.getId());
    }

    public final boolean isFinal(State state) {
        return finalStateIds.contains(state.getId());
    }

    public final T createState() {
        final T newState = stateFactory.createState();
        states.add(newState);
        return newState;
    }

    public final Walker<Symbol> walker() {
        return walker(getInitialState());
    }

    abstract public Walker<Symbol> walker(T state);

    abstract public void addTransition(T from, Symbol by, T to);
}
