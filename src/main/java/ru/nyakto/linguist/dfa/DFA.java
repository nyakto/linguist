package ru.nyakto.linguist.dfa;

import ru.nyakto.linguist.BasicState;
import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.StateFactory;
import ru.nyakto.linguist.nfa.NFA;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DFA<T extends State, Symbol> extends FSM<T, Symbol> {
    public final Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();

    public DFA(StateFactory<T> stateFactory) {
        super(stateFactory);
    }

    public DFA(Function<Integer, T> stateConstructor) {
        super(stateConstructor);
    }

    @Override
    public DFAWalker<T, Symbol> walker(T state) {
        return new DFAWalker<>(this, state);
    }

    @Override
    public void addTransition(T from, Symbol by, T to) {
        transitions.computeIfAbsent(
            from.getId(), (key) -> new HashMap<>()
        ).put(by, to.getId());
    }

    public T addTransition(T from, Symbol by) {
        return getStateById(transitions.computeIfAbsent(
            from.getId(), (key) -> new HashMap<>()
        ).computeIfAbsent(
            by, (key) -> createState().getId()
        ));
    }

    public DFA<T, Symbol> minimize() {
        return minimize(
            null
        );
    }

    public DFA<T, Symbol> minimize(
        BiConsumer<Collection<T>, T> merge
    ) {
        final BiConsumer<T, T> copy = (T from, T to) -> {
            if (merge != null) {
                merge.accept(Collections.singleton(from), to);
            }
        };
        final NFA<T, Symbol> rev1 = new NFA<>(this, copy).reverse();
        final DFA<T, Symbol> det1 = rev1.convertToDFA(getStateFactory().cloneFactory(), merge);
        final NFA<T, Symbol> rev2 = new NFA<>(det1, copy).reverse();
        return rev2.convertToDFA(getStateFactory().cloneFactory(), merge);
    }

    public static <S> DFA<State, S> create() {
        return new DFA<>(BasicState::new);
    }
}
