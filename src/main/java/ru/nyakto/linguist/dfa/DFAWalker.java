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
        final Map<Symbol, T> transitions = dfa.transitions.get(currentState);
        if (transitions == null) {
            return false;
        }
        final T newState = transitions.get(by);
        if (newState == null) {
            return false;
        }
        currentState = newState;
        return true;
    }

    @Override
    public boolean isInFinalState() {
        return currentState.isFinal();
    }

	public T getCurrentState() {
		return currentState;
	}
}
