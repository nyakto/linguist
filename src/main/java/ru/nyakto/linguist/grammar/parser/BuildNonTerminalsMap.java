package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.grammar.*;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;

class BuildNonTerminalsMap implements RuleWalkerListener {
    private final LLParser parser;
    private boolean allowEmpty = true;

    public BuildNonTerminalsMap(LLParser parser) {
        this.parser = parser;
    }

    public void execute() {
        for (Rule rule : parser.grammar) {
            allowEmpty = true;
            RuleWalker.walk(rule, this);
            if (allowEmpty) {
                commit(Optional.empty(), rule);
            }
        }
    }

    @Override
    public boolean visitTerminal(Rule rule, int position, Terminal item) {
        commit(Optional.of(item), rule);
        allowEmpty = false;
        return false;
    }

    @Override
    public boolean visitNonTerminal(Rule rule, int position, NonTerminal item) {
        Optional.ofNullable(parser.startingTerminals.get(item))
            .ifPresent(terminals -> terminals.forEach(
                terminal -> commit(Optional.of(terminal), rule)
            ));
        return allowEmpty = parser.allowEmpty.contains(item);
    }

    private void commit(Optional<Terminal> optionalTerminal, Rule rule) {
        parser.nonTerminalsMap.computeIfAbsent(
            rule.getLhs(), (key) -> new HashMap<>()
        ).computeIfAbsent(
            optionalTerminal, (key) -> new LinkedHashSet<>()
        ).add(rule);
    }
}
