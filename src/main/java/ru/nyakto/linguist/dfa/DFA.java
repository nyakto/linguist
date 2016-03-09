package ru.nyakto.linguist.dfa;

import ru.nyakto.linguist.BasicState;
import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.StateFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            null,
            null
        );
    }

    public DFA<T, Symbol> minimize(
        BiPredicate<T, T> compare,
        BiConsumer<Collection<T>, T> merge
    ) {
        final DFA<T, Symbol> result = new DFA<>(getStateFactory().cloneFactory());

        final Map<Symbol, List<Pair>> siblings = new HashMap<>();
        transitions.forEach((src, map) -> {
            map.forEach((by, dst) -> {
                siblings.computeIfAbsent(
                    by, (key) -> new ArrayList<>()
                ).add(new Pair(src, dst));
            });
        });

        final Map<Integer, Integer> state2class = new HashMap<>();
        final Map<Integer, Set<Integer>> class2states = new HashMap<>();
        final AtomicInteger classIdGenerator = new AtomicInteger(0);
        final Consumer<Collection<Integer>> createClass = (stateIds) -> {
            final int newClassId = classIdGenerator.getAndIncrement();
            final Set<Integer> newClass = new HashSet<>();
            class2states.put(newClassId, newClass);
            for (Integer stateId : stateIds) {
                state2class.put(stateId, newClassId);
                newClass.add(stateId);
            }
        };
        {
            final Consumer<List<T>> equalitySplitter = (states) -> {
                if (compare == null) {
                    createClass.accept(
                        states.stream()
                            .map(State::getId)
                            .collect(Collectors.toList())
                    );
                    return;
                }
                final List<List<T>> classes = new ArrayList<>();
                classLoop:
                for (T state : states) {
                    for (List<T> classStates : classes) {
                        if (compare.test(classStates.get(0), state)) {
                            classStates.add(state);
                            continue classLoop;
                        }
                    }
                    final List<T> newClass = new ArrayList<>();
                    newClass.add(state);
                    classes.add(newClass);
                }
                classes.stream()
                    .map(
                        classStates -> classStates.stream()
                            .map(State::getId)
                            .collect(Collectors.toList())
                    )
                    .forEach(createClass::accept);
            };
            equalitySplitter.accept(
                getStates().stream()
                    .filter(state -> !isFinal(state))
                    .collect(Collectors.toList())
            );
            equalitySplitter.accept(
                getStates().stream()
                    .filter(this::isFinal)
                    .collect(Collectors.toList())
            );
        }

        final BiConsumer<Integer, Collection<Integer>> extractClass = (srcClassId, stateIds) -> {
            class2states.get(srcClassId).removeAll(stateIds);
            createClass.accept(stateIds);
        };

        final AtomicBoolean modified = new AtomicBoolean();
        do {
            modified.set(false);
            siblings.forEach((by, allPairs) -> {
                final Map<Pair, List<Pair>> pairsBySrcClass = allPairs.stream()
                    .collect(Collectors.groupingBy(
                        pair -> new Pair(
                            state2class.get(pair.getLeft()),
                            state2class.get(pair.getRight())
                        )
                    ));
                pairsBySrcClass.forEach((direction, pairs) -> {
                    final Set<Integer> srcClassStateIds = class2states.get(direction.getLeft());
                    if (pairs.size() != srcClassStateIds.size()) {
                        extractClass.accept(
                            direction.getLeft(),
                            pairs.stream()
                                .map(Pair::getLeft)
                                .collect(Collectors.toList())
                        );
                        modified.set(true);
                    }
                });
            });
        } while (modified.get());

        final Map<Integer, T> mapping = new HashMap<>();
        class2states.forEach((classId, stateIds) -> {
            final T newState = stateIds.contains(getInitialState().getId())
                ? result.getInitialState()
                : result.createState();
            final List<T> states = stateIds.stream()
                .map(this::getStateById)
                .collect(Collectors.toList());
            final boolean isFinalState = states.stream()
                .filter(this::isFinal)
                .findAny()
                .isPresent();
            if (isFinalState) {
                result.markStateAsFinal(newState);
            }
            if (merge != null) {
                merge.accept(states, newState);
            }
            for (Integer stateId : stateIds) {
                mapping.put(stateId, newState);
            }
        });

        getStates().stream()
            .map(State::getId)
            .filter(stateId -> !mapping.containsKey(stateId))
            .forEach(stateId -> {
                final T newState = stateId == getInitialState().getId()
                    ? result.getInitialState()
                    : result.createState();
                if (isFinal(getStateById(stateId))) {
                    result.markStateAsFinal(newState);
                }
                mapping.put(stateId, newState);
            });
        transitions.forEach((src, map) -> {
            map.forEach((by, dst) -> {
                result.addTransition(mapping.get(src), by, mapping.get(dst));
            });
        });
        return result;
    }

    public static <S> DFA<State, S> create() {
        return new DFA<>(BasicState::new);
    }
}
