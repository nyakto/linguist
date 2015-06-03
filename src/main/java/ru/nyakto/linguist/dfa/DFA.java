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

	public T addTransition(T from, Symbol by) {
		return transitions.computeIfAbsent(from, (src) -> new ConcurrentHashMap<>())
			.computeIfAbsent(by, (symbol) -> createState());
	}

	public DFA<State, Symbol> minimize() {
		return minimize(
			State::new,
			null,
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
		final Queue<Set<T>> task = new LinkedList<>();
		final Set<T> processedStates = new HashSet<>();
		task.add(
			reachable.stream()
				.filter(State::isFinal)
				.collect(Collectors.toSet())
		);
		final Map<T, S> old2new = new HashMap<>();
		while (!task.isEmpty()) {
			final Set<T> states = task.remove();
			processedStates.addAll(states);
			for (Set<T> equalStates : splitByEquality(states, reverseTransitions, compare)) {
				final S newState = equalStates.contains(getInitialState())
					? result.getInitialState()
					: result.createState();
				if (equalStates.iterator().next().isFinal()) {
					result.markStateAsFinal(newState);
				}
				if (merge != null) {
					merge.accept(equalStates, newState);
				}
				equalStates.forEach(oldState -> old2new.put(oldState, newState));
				final Set<T> unhandledSourceStates = equalStates.stream()
					.map(
						state -> Optional.ofNullable(reverseTransitions.get(state))
							.map(Map::values)
							.orElseGet(Collections::emptySet)
					)
					.collect(HashSet<T>::new, Collection::addAll, Collection::addAll)
					.stream()
					.filter(state -> !processedStates.contains(state))
					.collect(Collectors.toSet());
				if (!unhandledSourceStates.isEmpty()) {
					task.add(unhandledSourceStates);
				}
			}
		}
		old2new.forEach((oldState, newState) -> Optional.ofNullable(transitions.get(oldState))
			.ifPresent(transitions -> transitions.forEach((by, to) -> {
				Optional.ofNullable(old2new.get(to)).ifPresent(newTargetState -> {
					result.addTransition(newState, by, newTargetState);
				});
			})));
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
		Map<T, Map<Symbol, T>> reverseTransitions,
		BiPredicate<T, T> compare
	) {
		if (states.size() <= 1) {
			return Collections.singletonList(states);
		}
		final Map<Set<Symbol>, Set<T>> equalityClasses = states.stream()
			.collect(Collectors.groupingBy(
				(state) -> Optional.ofNullable(reverseTransitions.get(state))
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
