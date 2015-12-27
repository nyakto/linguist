package ru.nyakto.linguist.dfa;

import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
        final Queue<Integer> task = new LinkedList<>();
        final Set<T> finalStates = reachable.stream()
            .filter(State::isFinal)
            .collect(Collectors.toSet());
        if (finalStates.isEmpty()) {
            return result;
        }
        task.add(1);
        final AtomicInteger classCounter = new AtomicInteger(2);
        final Map<T, Integer> stateClass = new HashMap<>();
        final Map<Integer, Set<T>> statesByClass = new HashMap<>();
        final Set<T> commonStates = reachable.stream()
            .filter(state -> !isFinal(state))
            .collect(Collectors.toSet());
        statesByClass.put(0, commonStates);
        statesByClass.put(1, new HashSet<>(finalStates));
        commonStates.forEach((state) -> stateClass.put(state, 0));
        finalStates.forEach((state) -> stateClass.put(state, 1));
        if (compare != null) {
            final List<Set<T>> introduceClasses = new LinkedList<>();
            statesByClass.forEach((classId, states) -> {
                boolean equal = true;
                if (states.size() > 1) {
                    final Iterator<T> iterator = states.iterator();
                    final T state = iterator.next();
                    while (iterator.hasNext()) {
                        if (!compare.test(state, iterator.next())) {
                            equal = false;
                        }
                    }
                }
                if (!equal) {
                    final List<Set<T>> newClasses = new LinkedList<>();
                    final Iterator<T> iterator = states.iterator();
                    final T original = iterator.next();
                    while (iterator.hasNext()) {
                        final T state = iterator.next();
                        if (compare.test(original, state)) {
                            continue;
                        }
                        iterator.remove();
                        newClasses.stream()
                            .filter(classStates -> compare.test(classStates.iterator().next(), state))
                            .findFirst()
                            .orElseGet(() -> {
                                final Set<T> newClass = new HashSet<>();
                                newClasses.add(newClass);
                                return newClass;
                            })
                            .add(state);
                    }
                    introduceClasses.addAll(newClasses);
                }
            });
            for (Set<T> states : introduceClasses) {
                final int newClassId = classCounter.getAndIncrement();
                statesByClass.put(newClassId, states);
                task.add(newClassId);
            }
        }
        while (!task.isEmpty()) {
            final int currentClass = task.remove();
            final Set<T> states = statesByClass.get(currentClass);
            final Map<T, Set<Symbol>> incomingTransitions = new HashMap<>();
            final Map<Set<Symbol>, Set<T>> incomingStateClasses = new HashMap<>();
            states.forEach(state -> {
                Optional.ofNullable(reverseTransitions.get(state)).ifPresent(transitions -> {
                    transitions.forEach((by, from) -> {
                        incomingTransitions.computeIfAbsent(from, (symbol) -> new HashSet<>()).add(by);
                    });
                });
            });
            incomingTransitions.forEach((from, by) -> {
                incomingStateClasses.computeIfAbsent(by, symbols -> new HashSet<>()).add(from);
            });
            incomingStateClasses.values().forEach(possiblyEqualStates -> possiblyEqualStates.stream()
                .collect(Collectors.groupingBy(stateClass::get, Collectors.toSet()))
                .forEach((oldClassId, equalStates) -> {
                    final Set<T> oldClassStates = statesByClass.get(oldClassId);
                    if (!oldClassStates.equals(equalStates)) {
                        int newClassId = classCounter.getAndIncrement();
                        statesByClass.put(newClassId, equalStates);
                        oldClassStates.removeAll(equalStates);
                        equalStates.forEach(state -> stateClass.put(state, newClassId));
                        task.add(newClassId);
                        if (!task.contains(oldClassId)) {
                            task.add(oldClassId);
                        }
                    }
                }));
        }
        final Map<T, S> old2new = new HashMap<>();
        statesByClass.values().forEach((states) -> {
            if (!states.isEmpty()) {
                final boolean isInitial = states.contains(getInitialState());
                final S newState = isInitial ? result.getInitialState() : result.createState();
                Optional.ofNullable(merge).ifPresent(fn -> fn.accept(states, newState));
                if (isFinal(states.iterator().next())) {
                    result.markStateAsFinal(newState);
                }
                states.forEach(state -> old2new.put(state, newState));
            }
        });
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

    public static <S> DFA<State, S> create() {
        return new DFA<>(State::new);
    }
}
