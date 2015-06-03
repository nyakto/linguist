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
			null
		);
	}

	public <S extends State> DFA<S, Symbol> minimize(
		BiFunction<FSM, Long, S> stateConstructor,
		BiPredicate<T, T> compare,
		BiConsumer<Set<T>, S> merge
	) {
		final DFA<S, Symbol> result = new DFA<>(stateConstructor);
		final Set<T> reachable = findReachableStates();
		final Map<T, Map<Symbol, T>> reverseTransitions = buildReverseTransitionsMap(reachable);
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

	protected Map<T, Map<Symbol, T>> buildReverseTransitionsMap(Set<T> states) {
		final Map<T, Map<Symbol, T>> result = new HashMap<>();
		transitions.forEach((src, srcTransitions) -> srcTransitions.forEach((by, dst) -> {
			if (states.contains(dst)) {
				result.computeIfAbsent(dst, (key) -> new HashMap<>()).put(by, src);
			}
		}));
		return result;
	}

	protected Collection<? extends Set<T>> splitByEquality(
		Set<T> states,
		Map<T, Map<Symbol, T>> reverseMap,
		BiPredicate<T, T> compare
	) {
		if (states.size() <= 1) {
			return Collections.singletonList(states);
		}
		final Map<Set<Symbol>, Set<T>> equalityClasses = states.stream()
			.collect(Collectors.groupingBy(
				(state) -> Optional.ofNullable(reverseMap.get(state))
					.map(Map::keySet)
					.orElseGet(Collections::emptySet),
				Collectors.toSet()
			));
		if (compare == null) {
			return equalityClasses.values().stream()
				.map((equalStates) -> (Set<T>) new HashSet<>(equalStates))
				.collect(Collectors.toList());
		}
		final Collection<Set<T>> result = new LinkedList<>();
		for (Set<T> equalStates : equalityClasses.values()) {
			while (!equalStates.isEmpty()) {
				final Iterator<T> iterator = equalStates.iterator();
				final T state = iterator.next();
				iterator.remove();
				final Set<T> newClass = new HashSet<>();
				newClass.add(state);
				while (iterator.hasNext()) {
					final T testState = iterator.next();
					if (compare.test(state, testState)) {
						newClass.add(testState);
						iterator.remove();
					}
				}
				result.add(newClass);
			}
		}
		return result;
	}

    public static <S> DFA<State, S> create() {
        return new DFA<>(State::new);
    }
}
