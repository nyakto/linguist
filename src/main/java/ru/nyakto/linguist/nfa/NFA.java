package ru.nyakto.linguist.nfa;

import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;

import java.util.*;
import java.util.function.BiFunction;

public class NFA<T extends State, Symbol> extends FSM<T, Symbol> {
    protected final Map<T, Map<Optional<Symbol>, Set<T>>> transitions = new HashMap<>();

    public NFA(BiFunction<FSM, Long, T> stateConstructor) {
        super(stateConstructor);
    }

    @Override
    public NFAWalker<T, Symbol> walker(T state) {
        return new NFAWalker<>(
            this,
            findLambdaReachableStates(getInitialState())
        );
    }

    @Override
    public void addTransition(T from, Symbol by, T to) {
        addTransition(from, Optional.ofNullable(by), to);
    }

    public void addTransition(T from, T to) {
        addTransition(from, Optional.empty(), to);
    }

    public void addTransition(T from, Optional<Symbol> by, T to) {
        transitions.computeIfAbsent(from, (src) -> new HashMap<>())
            .computeIfAbsent(by, (symbol) -> new HashSet<>())
            .add(to);
    }

    public T addTransition(T from, Symbol by) {
        return addTransition(from, Optional.ofNullable(by));
    }

    public T addTransition(T from) {
        return addTransition(from, Optional.empty());
    }

    public T addTransition(T from, Optional<Symbol> by) {
        final T to = createState();
        addTransition(from, to);
        return to;
    }

    protected Set<T> findLambdaReachableStates(T from) {
        final Set<T> result = Optional.ofNullable(transitions.get(from))
            .map(Map::values)
            .orElseGet(Collections::emptySet)
            .stream()
            .collect(HashSet::new, Collection::addAll, Collection::addAll);
        result.add(from);
        return result;
    }

    protected Set<T> findLambdaReachableStates(Set<T> fromStates) {
        final Set<T> result = new HashSet<>();
        final Queue<T> task = new LinkedList<>();
        result.addAll(fromStates);
        task.addAll(fromStates);
        while (!task.isEmpty()) {
            findLambdaReachableStates(task.remove()).stream()
                .filter(result::add)
                .forEach(task::add);
        }
        return result;
    }

    protected Set<T> getDirectSymbolReachableStates(T from, Symbol by) {
        return Optional.ofNullable(transitions.get(from))
            .map(transitions -> transitions.get(Optional.of(by)))
            .orElseGet(Collections::emptySet);
    }

    protected Set<T> findAllSymbolReachableStates(Set<T> fromStates, Symbol by) {
        final Set<T> directSymbolReachable = findLambdaReachableStates(fromStates).stream()
            .map(state -> getDirectSymbolReachableStates(state, by))
            .collect(HashSet::new, Collection::addAll, Collection::addAll);
        return findLambdaReachableStates(
            directSymbolReachable
        );
    }
}
