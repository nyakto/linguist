package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.grammar.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuildNonTerminalsMap implements RuleWalkerListener {
    private final LLParser parser;
    private Rule rule;

    public BuildNonTerminalsMap(LLParser parser) {
        this.parser = parser;
    }

    public void execute() {
        final Map<NonTerminal, List<Rule>> rulesByLhs = parser.grammar.stream()
            .collect(Collectors.groupingBy(Rule::getLhs));
        rulesByLhs.forEach((lhs, rules) -> {
            rules.forEach((rule) -> {
                this.rule = rule;
                RuleWalker.walk(rule, this);
            });
        });
    }

    @Override
    public boolean visitTerminal(int position, Terminal item) {
        commit(item);
        return false;
    }

    @Override
    public boolean visitNonTerminal(int position, NonTerminal item) {
        Optional.ofNullable(parser.startingTerminals.get(item))
            .ifPresent(terminals -> terminals.forEach(this::commit));
        return parser.allowEmpty.contains(item);
    }

    private void commit(Terminal terminal) {
        parser.nonTerminalsMap.computeIfAbsent(
            rule.getLhs(), (key) -> new HashMap<>()
        ).put(terminal, rule);
    }
}
