package ru.nyakto.linguist.dfa;

import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.nfa.NFA;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class DFA<T extends State, Symbol> extends FSM<T, Symbol> {
    public final Map<T, Map<Symbol, T>> transitions = new HashMap<>();

    public DFA(BiFunction<FSM, Long, T> stateConstructor) {
        super(stateConstructor);
    }

    @Override
    public DFAWalker<T, Symbol> walker(T state) {
        return new DFAWalker<>(this, state);
    }

    @Override
    public void addTransition(T from, Symbol by, T to) {
        transitions.computeIfAbsent(from, (src) -> new HashMap<>()).put(by, to);
    }

    public T addTransition(T from, Symbol by) {
        return transitions.computeIfAbsent(from, (src) -> new HashMap<>())
            .computeIfAbsent(by, (symbol) -> createState());
    }

    public DFA<T, Symbol> minimize() {
        return minimize(
            null
        );
    }

    public DFA<T, Symbol> minimize(
        BiConsumer<Set<T>, T> merge
    ) {
        final BiConsumer<T, T> copy = (T from, T to) -> {
            if (merge != null) {
                merge.accept(Collections.singleton(from), to);
            }
        };
        final NFA<T, Symbol> rev1 = new NFA<>(this, copy).reverse();
        final DFA<T, Symbol> det1 = rev1.convertToDFA(getStateConstructor(), merge);
        final NFA<T, Symbol> rev2 = new NFA<>(det1, copy).reverse();
        return rev2.convertToDFA(getStateConstructor(), merge);
    }

    protected Set<T> findReachableStates() {
        final Set<T> reachable = new HashSet<>();
        final Queue<T> task = new LinkedList<>();
        reachable.add(getInitialState());
        task.add(getInitialState());
        while (!task.isEmpty()) {
            final T state = task.remove();
            Optional.ofNullable(transitions.get(state))
                .ifPresent(transitions -> task.addAll(
                    transitions.values().stream()
                        .filter(reachable::add)
                        .collect(Collectors.toList())
                ));
        }
        return reachable;
    }

    protected Map<T, Map<Symbol, T>> buildReverseTransitionsMap(Set<T> states) {
        final Map<T, Map<Symbol, T>> result = new HashMap<>();
        transitions.forEach((src, srcTransitions) -> srcTransitions.forEach((by, dst) -> {
            if (states.contains(dst)) {
                result.computeIfAbsent(dst, (key) -> new HashMap<>()).put(by, src);
            }
        }));
        return result;
    }

    public static <S> DFA<State, S> create() {
        return new DFA<>(State::new);
    }
}
