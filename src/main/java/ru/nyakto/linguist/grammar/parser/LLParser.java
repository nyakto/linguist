package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.Grammar;
import ru.nyakto.linguist.grammar.NonTerminal;
import ru.nyakto.linguist.grammar.Terminal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LLParser {
    protected final Grammar grammar;
    protected final Set<NonTerminal> allowEmpty = new HashSet<>();
    protected final Map<NonTerminal, Set<Terminal>> startingTerminals = new HashMap<>();
    protected final Map<NonTerminal, Set<Terminal>> followingTerminals = new HashMap<>();

    public LLParser(Grammar grammar) {
        this.grammar = grammar;
        new FindEmptyAllowingNonTerminals(this).execute();
        new FindStartingTerminals(this).execute();
        new FindFollowingTerminals(this).execute();
    }
}
