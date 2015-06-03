package ru.nyakto.linguist.dfa;

import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DFA<T extends State, Symbol> extends FSM<T, Symbol> {
    protected final Map<T, Map<Symbol, T>> transitions = new ConcurrentHashMap<>();

    public DFA(BiFunction<FSM, Long, T> stateConstructor) {
        super(stateConstructor);
    }

    @Override
    public DFAWalker<T, Symbol> walker(T state) {
        return new DFAWalker<>(this, state);
    }

    @Override
    public void addTransition(T from, Symbol by, T to) {
        transitions.computeIfAbsent(from, (src) -> new ConcurrentHashMap<>()).put(by, to);
    }

	public DFA<State, Symbol> minimize() {
		return minimize(
			State::new,
			(a, b) -> true,
			(oldStates, newState) -> {
			}
		);
	}

	public <S extends State> DFA<S, Symbol> minimize(
		BiFunction<FSM, Long, S> stateConstructor,
		BiPredicate<T, T> compare,
		BiConsumer<Set<T>, S> merge
	) {
		final DFA<S, Symbol> result = new DFA<>(stateConstructor);
		final Set<T> reachable = findReachableStates();
		return result;
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

    public static <S> DFA<State, S> create() {
        return new DFA<>(State::new);
    }
}
