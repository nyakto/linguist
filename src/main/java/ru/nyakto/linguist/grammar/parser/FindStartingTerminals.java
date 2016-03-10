package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.grammar.*;

import java.util.*;

class FindStartingTerminals implements RuleWalkerListener {
    private final LLParser parser;
    private final Map<NonTerminal, Set<NonTerminal>> deps = new HashMap<>();
    private NonTerminal lhs;

    public FindStartingTerminals(LLParser parser) {
        this.parser = parser;
    }

    public void execute() {
        for (Rule rule : parser.grammar) {
            lhs = rule.getLhs();
            RuleWalker.walk(rule, this);
        }
        ParserUtils.resolveDependencies(deps);
        deps.forEach((item, itemDeps) -> {
            if (itemDeps == null) {
                return;
            }
            final Set<Terminal> terminals = parser.startingTerminals.computeIfAbsent(
                item, (key) -> new HashSet<>()
            );
            for (NonTerminal dep : itemDeps) {
                Optional.ofNullable(parser.startingTerminals.get(dep))
                    .ifPresent(terminals::addAll);
            }
        });
    }

    @Override
    public boolean visitTerminal(int position, Terminal item) {
        parser.startingTerminals.computeIfAbsent(
            lhs, (key) -> new HashSet<>()
        ).add(item);
        return false;
    }

    @Override
    public boolean visitNonTerminal(int position, NonTerminal item) {
        deps.computeIfAbsent(
            lhs, (key) -> new HashSet<>()
        ).add(item);
        return parser.allowEmpty.contains(item);
    }
}
