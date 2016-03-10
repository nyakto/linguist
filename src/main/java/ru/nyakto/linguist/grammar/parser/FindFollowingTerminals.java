package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.grammar.*;

import java.util.*;

class FindFollowingTerminals implements RuleWalkerListener {
    private final LLParser parser;
    private final Map<NonTerminal, Set<NonTerminal>> deps = new HashMap<>();
    private final Map<NonTerminal, Set<NonTerminal>> finals = new HashMap<>();
    private Set<NonTerminal> current;

    public FindFollowingTerminals(LLParser parser) {
        this.parser = parser;
    }

    public void execute() {
        for (Rule rule : parser.grammar) {
            current = new HashSet<>();
            RuleWalker.walk(rule, this);
            for (NonTerminal nt : current) {
                finals.computeIfAbsent(
                    nt, (key) -> new HashSet<>()
                ).add(rule.getLhs());
            }
        }
        ParserUtils.resolveDependencies(deps);
        deps.forEach((item, itemDeps) -> {
            if (itemDeps == null) {
                return;
            }
            final Set<Terminal> terminals = parser.followingTerminals.computeIfAbsent(
                item, (key) -> new HashSet<>()
            );
            for (NonTerminal dep : itemDeps) {
                Optional.ofNullable(parser.startingTerminals.get(dep))
                    .ifPresent(terminals::addAll);
            }
        });
        boolean modified;
        do {
            modified = false;
            for (Map.Entry<NonTerminal, Set<NonTerminal>> entry : finals.entrySet()) {
                final NonTerminal item = entry.getKey();
                final Set<NonTerminal> followingNonTerminals = entry.getValue();
                final Set<Terminal> terminals = parser.followingTerminals.computeIfAbsent(
                    item, (key) -> new HashSet<>()
                );
                for (NonTerminal nt : followingNonTerminals) {
                    final Set<Terminal> followingTerminals = parser.followingTerminals.get(nt);
                    if (followingTerminals != null && terminals.addAll(followingTerminals)) {
                        modified = true;
                    }
                }
            }
        } while (modified);
    }

    @Override
    public boolean visitTerminal(int position, Terminal item) {
        commit(Collections.singleton(item));
        current.clear();
        return true;
    }

    @Override
    public boolean visitNonTerminal(int position, NonTerminal item) {
        if (parser.allowEmpty.contains(item)) {
            for (NonTerminal nt : current) {
                deps.computeIfAbsent(
                    nt, (key) -> new HashSet<>()
                ).add(item);
            }
        } else {
            Optional.ofNullable(parser.startingTerminals.get(item))
                .ifPresent(this::commit);
            current.clear();
        }
        current.add(item);
        return true;
    }

    private void commit(Collection<Terminal> terminals) {
        if (!terminals.isEmpty()) {
            for (NonTerminal nt : current) {
                parser.followingTerminals.computeIfAbsent(
                    nt, (key) -> new HashSet<>()
                ).addAll(terminals);
            }
        }
    }
}
