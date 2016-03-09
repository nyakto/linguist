package ru.nyakto.linguist.dfa;

import ru.nyakto.linguist.State;
import ru.nyakto.linguist.Walker;

import java.util.Map;

public class DFAWalker<T extends State, Symbol> implements Walker<Symbol> {
    private final DFA<T, Symbol> dfa;
    private T currentState;

    public DFAWalker(DFA<T, Symbol> dfa, T currentState) {
        this.dfa = dfa;
        this.currentState = currentState;
    }

    @Override
    public boolean go(Symbol by) {
        final Map<Symbol, Integer> transitions = dfa.transitions.get(currentState.getId());
        if (transitions == null) {
            return false;
        }
        final Integer newStateId = transitions.get(by);
        if (newStateId == null) {
            return false;
        }
        currentState = dfa.getStateById(newStateId);
        return true;
    }

    @Override
    public boolean isInFinalState() {
        return dfa.isFinal(currentState);
    }

    public T getCurrentState() {
        return currentState;
    }
}
