package ru.nyakto.linguist;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

abstract public class FSM<T extends State, Symbol> implements Cloneable {
    private final T initialState;
    private final Set<Integer> finalStateIds = new HashSet<>();
    private final StateFactory<T> stateFactory;
    private final Map<Integer, T> states = new HashMap<>();

    public FSM(StateFactory<T> stateFactory) {
        this.stateFactory = stateFactory;
        initialState = createState();
    }

    public FSM(Function<Integer, T> stateConstructor) {
        this.stateFactory = new BasicStateFactory<>(stateConstructor);
        initialState = createState();
    }

    public StateFactory<T> getStateFactory() {
        return stateFactory;
    }

    public final T getInitialState() {
        return initialState;
    }

    public final Collection<T> getStates() {
        return Collections.unmodifiableCollection(states.values());
    }

    public final void markStateAsFinal(State state) {
        finalStateIds.add(state.getId());
    }

    public final boolean isFinal(State state) {
        return finalStateIds.contains(state.getId());
    }

    public final T createState() {
        final T newState = stateFactory.createState();
        states.put(newState.getId(), newState);
        return newState;
    }

    public final Walker<Symbol> walker() {
        return walker(getInitialState());
    }

    abstract public Walker<Symbol> walker(T state);

    abstract public void addTransition(T from, Symbol by, T to);
}
