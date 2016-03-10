package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.grammar.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuildNonTerminalsMap implements RuleWalkerListener {
    private final LLParser parser;

    public BuildNonTerminalsMap(LLParser parser) {
        this.parser = parser;
    }

    public void execute() {
        final Map<NonTerminal, List<Rule>> rulesByLhs = parser.grammar.stream()
            .collect(Collectors.groupingBy(Rule::getLhs));
        rulesByLhs.forEach((lhs, rules) -> {
            rules.forEach((rule) -> {
                RuleWalker.walk(rule, this);
            });
        });
    }

    @Override
    public boolean visitTerminal(Rule rule, int position, Terminal item) {
        commit(item, rule);
        return false;
    }

    @Override
    public boolean visitNonTerminal(Rule rule, int position, NonTerminal item) {
        Optional.ofNullable(parser.startingTerminals.get(item))
            .ifPresent(terminals -> terminals.forEach(
                terminal -> commit(terminal, rule)
            ));
        return parser.allowEmpty.contains(item);
    }

    private void commit(Terminal terminal, Rule rule) {
        parser.nonTerminalsMap.computeIfAbsent(
            rule.getLhs(), (key) -> new HashMap<>()
        ).put(terminal, rule);
    }
}
