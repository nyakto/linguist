package ru.nyakto.linguist.nfa;

import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.dfa.DFA;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NFA<T extends State, Symbol> extends FSM<T, Symbol> {
    protected final Map<T, Map<Optional<Symbol>, Set<T>>> transitions = new HashMap<>();

    public NFA(BiFunction<FSM, Long, T> stateConstructor) {
        super(stateConstructor);
    }

    @Override
    public NFAWalker<T, Symbol> walker(T state) {
        return new NFAWalker<>(
            this,
            getInitialState()
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

    public void addTransition(T from, NFA<T, Symbol> by, BiConsumer<T, T> copy, T to) {
        final Function<T, T> copyState = (oldState) -> {
            final T newState = createState();
            if (oldState.isFinal()) {
                addTransition(newState, to);
            }
            if (copy != null) {
                copy.accept(oldState, newState);
            }
            return newState;
        };

        final Map<T, T> old2new = by.getStates().stream()
            .collect(Collectors.toMap(old -> old, copyState));
        addTransition(from, old2new.get(by.getInitialState()));
        old2new.forEach((oldState, newState) -> Optional.ofNullable(by.transitions.get(oldState))
            .filter(transitions -> !transitions.isEmpty())
            .ifPresent(oldTransitions -> {
                final Map<Optional<Symbol>, Set<T>> newStateTransitions = transitions.computeIfAbsent(
                    newState,
                    key -> new HashMap<>()
                );
                oldTransitions.forEach((symbol, target) -> {
                    newStateTransitions.computeIfAbsent(symbol, key -> new HashSet<>()).addAll(
                        target.stream()
                            .map(old2new::get)
                            .collect(Collectors.toSet())
                    );
                });
            }));
    }

    public void addTransition(T from, DFA<T, Symbol> by, BiConsumer<T, T> copy, T to) {
        final Function<T, T> copyState = (oldState) -> {
            final T newState = createState();
            if (oldState.isFinal()) {
                addTransition(newState, to);
            }
            if (copy != null) {
                copy.accept(oldState, newState);
            }
            return newState;
        };

        final Map<T, T> old2new = by.getStates().stream()
            .collect(Collectors.toMap(old -> old, copyState));
        addTransition(from, old2new.get(by.getInitialState()));
        old2new.forEach((oldState, newState) -> Optional.ofNullable(by.transitions.get(oldState))
            .filter(transitions -> !transitions.isEmpty())
            .ifPresent(oldTransitions -> {
                final Map<Optional<Symbol>, Set<T>> newStateTransitions = transitions.computeIfAbsent(
                    newState,
                    key -> new HashMap<>()
                );
                oldTransitions.forEach((symbol, target) -> {
                    newStateTransitions.computeIfAbsent(Optional.of(symbol), key -> new HashSet<>()).add(
                        old2new.get(target)
                    );
                });
            }));
    }

    public T addTransition(T from, Symbol by) {
        return addTransition(from, Optional.ofNullable(by));
    }

    public T addTransition(T from) {
        return addTransition(from, Optional.empty());
    }

    public T addTransition(T from, Optional<Symbol> by) {
        final T to = createState();
        addTransition(from, by, to);
        return to;
    }

    public T addTransition(T from, NFA<T, Symbol> by, BiConsumer<T, T> copy) {
        final T to = createState();
        addTransition(from, by, copy, to);
        return to;
    }

    protected Set<T> findDirectLambdaReachableStates(T from) {
        final Set<T> result = new HashSet<>(
            Optional.ofNullable(transitions.get(from))
                .map(transitions -> transitions.get(Optional.<Symbol>empty()))
                .orElseGet(Collections::emptySet)
        );
        result.add(from);
        return result;
    }

    protected Set<T> findLambdaReachableStates(Set<T> fromStates) {
        final Set<T> result = new HashSet<>();
        final Queue<T> task = new LinkedList<>();
        result.addAll(fromStates);
        task.addAll(fromStates);
        while (!task.isEmpty()) {
            findDirectLambdaReachableStates(task.remove()).stream()
                .filter(result::add)
                .forEach(task::add);
        }
        return result;
    }

    protected Set<T> findDirectSymbolReachableStates(T from, Symbol by) {
        return Optional.ofNullable(transitions.get(from))
            .map(transitions -> transitions.get(Optional.of(by)))
            .orElseGet(Collections::emptySet);
    }

    protected Set<T> findDirectSymbolReachableStates(Set<T> fromStates, Symbol by) {
        return fromStates.stream()
            .map(state -> findDirectSymbolReachableStates(state, by))
            .collect(HashSet::new, Collection::addAll, Collection::addAll);
    }

    protected Set<Symbol> findDirectTransitionSymbols(Set<T> fromStates) {
        return fromStates.stream()
            .map(transitions::get)
            .filter(Objects::nonNull)
            .map(Map::keySet)
            .collect(HashSet<Optional<Symbol>>::new, Collection::addAll, Collection::addAll)
            .stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    public <S extends State> DFA<S, Symbol> convertToDFA(
        BiFunction<FSM, Long, S> stateConstructor,
        BiConsumer<Set<T>, S> merge
    ) {
        final DFA<S, Symbol> result = new DFA<>(stateConstructor);
        final Map<Set<T>, S> old2new = new HashMap<>();
        final Queue<Set<T>> task = new LinkedList<>();
        final Set<T> initialState = findLambdaReachableStates(
            Collections.singleton(getInitialState())
        );

        final BiConsumer<Set<T>, S> initNewState = (oldStates, newState) -> {
            oldStates.stream()
                .filter(State::isFinal)
                .findAny()
                .ifPresent((finalState) -> result.markStateAsFinal(newState));
            if (merge != null) {
                merge.accept(oldStates, newState);
            }
        };
        final Function<Set<T>, S> createNewState = (oldStates) -> {
            final S newState = result.createState();
            initNewState.accept(oldStates, newState);
            task.add(oldStates);
            return newState;
        };

        old2new.put(initialState, result.getInitialState());
        initNewState.accept(initialState, result.getInitialState());
        task.add(initialState);

        while (!task.isEmpty()) {
            final Set<T> oldSrcState = task.remove();
            final S newSrcState = old2new.computeIfAbsent(oldSrcState, createNewState);
            for (Symbol by : findDirectTransitionSymbols(oldSrcState)) {
                final Set<T> oldDstState = findLambdaReachableStates(
                    findDirectSymbolReachableStates(oldSrcState, by)
                );
                final S newDstState = old2new.computeIfAbsent(oldDstState, createNewState);
                result.addTransition(newSrcState, by, newDstState);
            }
        }
        return result;
    }

    public static <S> NFA<State, S> create() {
        return new NFA<>(State::new);
    }
}
