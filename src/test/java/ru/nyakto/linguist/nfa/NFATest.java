package ru.nyakto.linguist.nfa;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.nyakto.linguist.FSMTestHelper;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.dfa.DFA;

@RunWith(JUnit4.class)
public class NFATest extends FSMTestHelper {
    protected <T extends State> void addWordToMatcher(NFA<T, Character> matcher, String word) {
        T currentState = matcher.addTransition(matcher.getInitialState());
        for (char by : word.toCharArray()) {
            currentState = matcher.addTransition(currentState, by);
        }
        matcher.markStateAsFinal(currentState);
    }

    protected NFA<State, Character> wordMatcher(String word) {
        final NFA<State, Character> matcher = NFA.create();
        addWordToMatcher(matcher, word);
        return matcher;
    }

    protected NFA<State, Character> optional(NFA<State, Character> matcher) {
        final NFA<State, Character> result = NFA.create();
        final State matched = result.createState();
        result.addTransition(result.getInitialState(), matcher, null, matched);
        result.markStateAsFinal(result.getInitialState());
        result.markStateAsFinal(matched);
        return result;
    }

    protected NFA<State, Character> repeatable(NFA<State, Character> matcher, int min) {
        final NFA<State, Character> result = NFA.create();
        State matched = result.getInitialState();
        for (int i = min; i > 0; --i) {
            matched = result.addTransition(matched, matcher, null);
        }
        result.markStateAsFinal(matched);
        result.addTransition(matched, matcher, null, matched);
        return result;
    }

    @SafeVarargs
    protected final NFA<State, Character> alternative(NFA<State, Character>... matchers) {
        final NFA<State, Character> result = NFA.create();
        final State matched = result.createState();
        for (NFA<State, Character> matcher : matchers) {
            result.addTransition(result.getInitialState(), matcher, null, matched);
        }
        result.markStateAsFinal(matched);
        return result;
    }

    @SafeVarargs
    protected final NFA<State, Character> sequence(NFA<State, Character>... matchers) {
        final NFA<State, Character> result = NFA.create();
        State matched = result.getInitialState();
        for (NFA<State, Character> matcher : matchers) {
            matched = result.addTransition(matched, matcher, null);
        }
        result.markStateAsFinal(matched);
        return result;
    }

    @Test
    public void matcherShouldRecognizeLanguage() {
        final NFA<State, Character> matcher = NFA.create();
        addWordToMatcher(matcher, "this");
        addWordToMatcher(matcher, "is");
        addWordToMatcher(matcher, "test");
        addWordToMatcher(matcher, "matcher");
        Assert.assertTrue("shouldn't match empty string", !testWord(matcher, ""));
        Assert.assertTrue("should match word 'this'", testWord(matcher, "this"));
        Assert.assertTrue("should match word 'is'", testWord(matcher, "is"));
        Assert.assertTrue("should match word 'test'", testWord(matcher, "test"));
        Assert.assertTrue("should match word 'matcher'", testWord(matcher, "matcher"));
        Assert.assertTrue("shouldn't match word 'tes'", !testWord(matcher, "tes"));
        Assert.assertTrue("shouldn't match word 'tcher'", !testWord(matcher, "tcher"));
        Assert.assertTrue("shouldn't match word 'random'", !testWord(matcher, "random"));
    }

    @Test
    public void matcherCanBeConvertedToDFA() {
        final NFA<State, Character> originalMatcher = NFA.create();
        addWordToMatcher(originalMatcher, "this");
        addWordToMatcher(originalMatcher, "is");
        addWordToMatcher(originalMatcher, "test");
        addWordToMatcher(originalMatcher, "matcher");
        final DFA<State, Character> matcher = originalMatcher.convertToDFA(State::new, null);
        Assert.assertEquals(22, originalMatcher.getStates().size());
        Assert.assertEquals(17, matcher.getStates().size());
        Assert.assertTrue("shouldn't match empty string", !testWord(matcher, ""));
        Assert.assertTrue("should match word 'this'", testWord(matcher, "this"));
        Assert.assertTrue("should match word 'is'", testWord(matcher, "is"));
        Assert.assertTrue("should match word 'test'", testWord(matcher, "test"));
        Assert.assertTrue("should match word 'matcher'", testWord(matcher, "matcher"));
        Assert.assertTrue("shouldn't match word 'tes'", !testWord(matcher, "tes"));
        Assert.assertTrue("shouldn't match word 'tcher'", !testWord(matcher, "tcher"));
        Assert.assertTrue("shouldn't match word 'random'", !testWord(matcher, "random"));
    }

    @Test
    public void matcherSupportsTransitionsByNFA() {
        final NFA<State, Character> whiteSpace = repeatable(
            alternative(wordMatcher(" "), wordMatcher("\t")),
            1
        );
        final NFA<State, Character> word = alternative(
            wordMatcher("this"),
            wordMatcher("is"),
            wordMatcher("test"),
            wordMatcher("matcher")
        );
        final NFA<State, Character> matcher = sequence(
            word,
            repeatable(
                sequence(whiteSpace, word),
                0
            )
        );
        Assert.assertTrue("shouldn't match empty string", !testWord(matcher, ""));
        Assert.assertTrue("should match word 'this'", testWord(matcher, "this"));
        Assert.assertTrue("should match word 'is'", testWord(matcher, "is"));
        Assert.assertTrue("should match word 'test'", testWord(matcher, "test"));
        Assert.assertTrue("should match word 'matcher'", testWord(matcher, "matcher"));
        Assert.assertTrue("should match phrase 'this is test matcher'", testWord(matcher, "this is test matcher"));
        Assert.assertTrue("should match phrase 'this\tis test matcher'", testWord(matcher, "this\tis test matcher"));
        Assert.assertTrue("should match phrase 'test   matcher'", testWord(matcher, "test   matcher"));
    }
}
