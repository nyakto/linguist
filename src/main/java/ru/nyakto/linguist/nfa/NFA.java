package ru.nyakto.linguist.nfa;

import ru.nyakto.linguist.BasicState;
import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.StateFactory;
import ru.nyakto.linguist.dfa.DFA;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NFA<T extends State, Symbol> extends FSM<T, Symbol> {
    protected final Map<Integer, Map<Optional<Symbol>, Set<Integer>>> transitions = new HashMap<>();

    public NFA(StateFactory<T> stateFactory) {
        super(stateFactory);
    }

    public NFA(Function<Integer, T> stateConstructor) {
        super(stateConstructor);
    }

    public NFA(DFA<T, Symbol> dfa, BiConsumer<T, T> copyState) {
        super(dfa.getStateFactory().cloneFactory());
        final T finalState = createState();
        markStateAsFinal(finalState);
        addTransition(getInitialState(), dfa, copyState, finalState);
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
        transitions.computeIfAbsent(
            from.getId(), (key) -> new HashMap<>()
        ).computeIfAbsent(
            by, (key) -> new HashSet<>()
        ).add(to.getId());
    }

    public void addTransition(T from, NFA<T, Symbol> by, BiConsumer<T, T> copy, T to) {
        final Function<T, T> copyState = (oldState) -> {
            final T newState = createState();
            if (by.isFinal(oldState)) {
                addTransition(newState, to);
            }
            if (copy != null) {
                copy.accept(oldState, newState);
            }
            return newState;
        };

        final Map<Integer, T> old2new = by.getStates().stream()
            .collect(Collectors.toMap(State::getId, copyState));
        addTransition(from, old2new.get(by.getInitialState().getId()));
        old2new.forEach((oldState, newState) -> Optional.ofNullable(by.transitions.get(oldState))
            .filter(transitions -> !transitions.isEmpty())
            .ifPresent(oldTransitions -> {
                final Map<Optional<Symbol>, Set<Integer>> newStateTransitions = transitions.computeIfAbsent(
                    newState.getId(),
                    (key) -> new HashMap<>()
                );
                oldTransitions.forEach((symbol, target) -> {
                    newStateTransitions.computeIfAbsent(
                        symbol, (key) -> new HashSet<>()
                    ).addAll(
                        target.stream()
                            .map(old2new::get)
                            .map(State::getId)
                            .collect(Collectors.toSet())
                    );
                });
            }));
    }

    public void addTransition(T from, DFA<T, Symbol> by, BiConsumer<T, T> copy, T to) {
        final Function<T, T> copyState = (oldState) -> {
            final T newState = createState();
            if (by.isFinal(oldState)) {
                addTransition(newState, to);
            }
            if (copy != null) {
                copy.accept(oldState, newState);
            }
            return newState;
        };

        final Map<Integer, T> old2new = by.getStates().stream()
            .collect(Collectors.toMap(State::getId, copyState));
        addTransition(from, old2new.get(by.getInitialState().getId()));
        old2new.forEach((oldState, newState) -> Optional.ofNullable(by.transitions.get(oldState))
            .filter(transitions -> !transitions.isEmpty())
            .ifPresent(oldTransitions -> {
                final Map<Optional<Symbol>, Set<Integer>> newStateTransitions = transitions.computeIfAbsent(
                    newState.getId(),
                    (key) -> new HashMap<>()
                );
                oldTransitions.forEach((symbol, target) -> {
                    newStateTransitions.computeIfAbsent(
                        Optional.of(symbol), (key) -> new HashSet<>()
                    ).add(
                        old2new.get(target).getId()
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

    public NFA<T, Symbol> reverse() {
        final NFA<T, Symbol> result = new NFA<>(getStateFactory().cloneFactory());
        final Map<Integer, T> stateMap = new HashMap<>();
        final T finalState = result.createState();
        result.markStateAsFinal(finalState);
        for (T src : getStates()) {
            final T dst = result.createState();
            stateMap.put(src.getId(), dst);
            if (isFinal(src)) {
                result.addTransition(result.getInitialState(), dst);
            }
        }
        result.addTransition(stateMap.get(getInitialState().getId()), finalState);
        transitions.forEach((from, map) -> {
            map.forEach((by, targets) -> {
                for (int target : targets) {
                    final T src = stateMap.get(target);
                    final T dst = stateMap.get(from);
                    result.addTransition(src, by, dst);
                }
            });
        });
        return result;
    }

    protected Set<Integer> findDirectLambdaReachableStates(int from) {
        final Set<Integer> result = new HashSet<>(
            Optional.ofNullable(transitions.get(from))
                .map(transitions -> transitions.get(Optional.<Symbol>empty()))
                .orElseGet(Collections::emptySet)
        );
        result.add(from);
        return result;
    }

    protected Set<Integer> findLambdaReachableStates(Set<Integer> fromStates) {
        final Set<Integer> result = new HashSet<>();
        final Queue<Integer> task = new LinkedList<>();
        result.addAll(fromStates);
        task.addAll(fromStates);
        while (!task.isEmpty()) {
            findDirectLambdaReachableStates(task.remove()).stream()
                .filter(result::add)
                .forEach(task::add);
        }
        return result;
    }

    protected Set<Integer> findDirectSymbolReachableStates(int from, Symbol by) {
        return Optional.ofNullable(transitions.get(from))
            .map(transitions -> transitions.get(Optional.of(by)))
            .orElseGet(Collections::emptySet);
    }

    protected Set<Integer> findDirectSymbolReachableStates(Set<Integer> fromStates, Symbol by) {
        return fromStates.stream()
            .map(state -> findDirectSymbolReachableStates(state, by))
            .collect(HashSet::new, Collection::addAll, Collection::addAll);
    }

    protected Set<Symbol> findDirectTransitionSymbols(Set<Integer> fromStates) {
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
        StateFactory<S> stateFactory,
        BiConsumer<Collection<T>, S> merge
    ) {
        final DFA<S, Symbol> result = new DFA<>(stateFactory.cloneFactory());
        convertToDFA(result, merge);
        return result;
    }

    public <S extends State> DFA<S, Symbol> convertToDFA(
        Function<Integer, S> stateConstructor,
        BiConsumer<Collection<T>, S> merge
    ) {
        final DFA<S, Symbol> result = new DFA<>(stateConstructor);
        convertToDFA(result, merge);
        return result;
    }

    private <S extends State> void convertToDFA(
        DFA<S, Symbol> target,
        BiConsumer<Collection<T>, S> merge
    ) {
        final Map<Set<Integer>, S> old2new = new HashMap<>();
        final Queue<Set<Integer>> task = new LinkedList<>();
        final Set<Integer> initialState = findLambdaReachableStates(
            Collections.singleton(getInitialState().getId())
        );

        final BiConsumer<Set<Integer>, S> initNewState = (oldStateIds, newState) -> {
            final List<T> oldStates = oldStateIds.stream()
                .map(this::getStateById)
                .collect(Collectors.toList());
            oldStates.stream()
                .filter(this::isFinal)
                .findAny()
                .ifPresent((finalState) -> target.markStateAsFinal(newState));
            if (merge != null) {
                merge.accept(oldStates, newState);
            }
        };
        final Function<Set<Integer>, S> createNewState = (oldStateIds) -> {
            final S newState = target.createState();
            initNewState.accept(oldStateIds, newState);
            task.add(oldStateIds);
            return newState;
        };

        old2new.put(initialState, target.getInitialState());
        initNewState.accept(initialState, target.getInitialState());
        task.add(initialState);

        while (!task.isEmpty()) {
            final Set<Integer> oldSrcState = task.remove();
            final S newSrcState = old2new.computeIfAbsent(oldSrcState, createNewState);
            for (Symbol by : findDirectTransitionSymbols(oldSrcState)) {
                final Set<Integer> oldDstState = findLambdaReachableStates(
                    findDirectSymbolReachableStates(oldSrcState, by)
                );
                final S newDstState = old2new.computeIfAbsent(oldDstState, createNewState);
                target.addTransition(newSrcState, by, newDstState);
            }
        }
    }

    public static <S> NFA<State, S> create() {
        return new NFA<>(BasicState::new);
    }
}
