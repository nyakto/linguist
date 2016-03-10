package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.AbstractGrammar;
import ru.nyakto.linguist.grammar.NonTerminal;
import ru.nyakto.linguist.grammar.Rule;
import ru.nyakto.linguist.grammar.Terminal;

import java.util.*;

public class LLParser {
    protected final AbstractGrammar grammar;
    protected final Set<NonTerminal> nonTerminals = new HashSet<>();
    protected final Set<NonTerminal> allowEmpty = new HashSet<>();
    protected final Map<NonTerminal, Set<Terminal>> startingTerminals = new HashMap<>();
    protected final Map<NonTerminal, Set<Terminal>> followingTerminals = new HashMap<>();
    protected final Map<NonTerminal, Map<Optional<Terminal>, Rule>> nonTerminalsMap = new HashMap<>();

    public LLParser(AbstractGrammar grammar) {
        this.grammar = grammar;
        new FindAllNonTerminals(this).execute();
        checkRuleSet();
        new FindEmptyAllowingNonTerminals(this).execute();
        new FindStartingTerminals(this).execute();
        new FindFollowingTerminals(this).execute();
        checkForConflicts();
        new BuildNonTerminalsMap(this).execute();
    }

    private void checkRuleSet() {
        // TODO: all non terminals must have rules
    }

    private void checkForConflicts() {
        // TODO: search for Fi/Fi or Fi/Fo conflicts
    }

    public Set<NonTerminal> getNonTerminals() {
        return nonTerminals;
    }

    public Map<Optional<Terminal>, Rule> getNonTerminalRuleMap(NonTerminal nonTerminal) {
        return nonTerminalsMap.get(nonTerminal);
    }
}
