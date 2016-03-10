package ru.nyakto.linguist.grammar.parser;

import ru.nyakto.linguist.grammar.NonTerminal;
import ru.nyakto.linguist.grammar.Rule;
import ru.nyakto.linguist.grammar.RuleWalker;
import ru.nyakto.linguist.grammar.RuleWalkerListener;

public class FindAllNonTerminals implements RuleWalkerListener {
    private final LLParser parser;

    public FindAllNonTerminals(LLParser parser) {
        this.parser = parser;
    }

    public void execute() {
        for (Rule rule : parser.grammar) {
            parser.nonTerminals.add(rule.getLhs());
            RuleWalker.walk(rule, this);
        }
    }

    @Override
    public boolean visitNonTerminal(int position, NonTerminal item) {
        parser.nonTerminals.add(item);
        return true;
    }
}
