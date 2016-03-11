package ru.nyakto.linguist.nfa;

import ru.nyakto.linguist.State;
import ru.nyakto.linguist.Walker;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class NFAWalker<T extends State, Symbol> implements Walker<Symbol> {
    private final NFA<T, Symbol> nfa;
    private Set<Integer> currentState;

    public NFAWalker(NFA<T, Symbol> nfa, T currentState) {
        this.nfa = nfa;
        this.currentState = nfa.findLambdaReachableStates(
            Collections.singleton(currentState.getId())
        );
    }

    public NFAWalker(NFA<T, Symbol> nfa, Set<T> currentState) {
        this.nfa = nfa;
        this.currentState = nfa.findLambdaReachableStates(
            currentState.stream()
                .map(State::getId)
                .collect(Collectors.toSet())
        );
    }

    @Override
    public boolean go(Symbol by) {
        final Set<Integer> nextState = nfa.findLambdaReachableStates(
            nfa.findDirectSymbolReachableStates(currentState, by)
        );
        if (nextState.isEmpty()) {
            return false;
        }
        currentState = nextState;
        return true;
    }

    @Override
    public boolean isInFinalState() {
        return currentState.stream()
            .map(nfa::getStateById)
            .filter(nfa::isFinal)
            .findAny()
            .isPresent();
    }

    public Collection<T> getCurrentState() {
        return currentState.stream()
            .map(nfa::getStateById)
            .collect(Collectors.toList());
    }
}
