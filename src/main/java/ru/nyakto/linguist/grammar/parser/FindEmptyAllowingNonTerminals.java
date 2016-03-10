package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.grammar.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class FindEmptyAllowingNonTerminals implements RuleWalkerListener {
    private final LLParser parser;
    private boolean emptyPossible;

    public FindEmptyAllowingNonTerminals(LLParser parser) {
        this.parser = parser;
    }

    public void execute() {
        final List<Rule> task = new ArrayList<>(parser.grammar);
        boolean modified;
        do {
            modified = false;
            final Iterator<Rule> iterator = task.iterator();
            while (iterator.hasNext()) {
                final Rule rule = iterator.next();
                emptyPossible = true;
                RuleWalker.walk(rule, this);
                if (emptyPossible) {
                    modified = true;
                    iterator.remove();
                    parser.allowEmpty.add(rule.getLhs());
                }
            }
        } while (modified);
    }

    @Override
    public boolean visitTerminal(int position, Terminal item) {
        emptyPossible = false;
        return false;
    }

    @Override
    public boolean visitNonTerminal(int position, NonTerminal item) {
        if (parser.allowEmpty.contains(item)) {
            return true;
        }
        emptyPossible = false;
        return false;
    }
}
