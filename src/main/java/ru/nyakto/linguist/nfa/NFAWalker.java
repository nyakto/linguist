package ru.nyakto.linguist.nfa;

import ru.nyakto.linguist.State;
import ru.nyakto.linguist.Walker;

import java.util.Set;

public class NFAWalker<T extends State, Symbol> implements Walker<Symbol> {
    private final NFA<T, Symbol> nfa;
    private Set<T> currentState;

    public NFAWalker(NFA<T, Symbol> nfa, Set<T> currentState) {
        this.nfa = nfa;
        this.currentState = currentState;
    }

    @Override
    public boolean go(Symbol by) {
        final Set<T> nextState = nfa.findAllSymbolReachableStates(currentState, by);
        if (nextState.isEmpty()) {
            return false;
        }
        currentState = nextState;
        return true;
    }

    @Override
    public boolean isInFinalState() {
        return currentState.stream()
            .filter(State::isFinal)
            .findAny()
            .isPresent();
    }

    public Set<T> getCurrentState() {
        return currentState;
    }
}
