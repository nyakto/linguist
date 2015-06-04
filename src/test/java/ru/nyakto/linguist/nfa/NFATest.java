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
        Assert.assertEquals(21, originalMatcher.getStates().size());
        Assert.assertEquals(16, matcher.getStates().size());
        Assert.assertTrue("shouldn't match empty string", !testWord(matcher, ""));
        Assert.assertTrue("should match word 'this'", testWord(matcher, "this"));
        Assert.assertTrue("should match word 'is'", testWord(matcher, "is"));
        Assert.assertTrue("should match word 'test'", testWord(matcher, "test"));
        Assert.assertTrue("should match word 'matcher'", testWord(matcher, "matcher"));
        Assert.assertTrue("shouldn't match word 'tes'", !testWord(matcher, "tes"));
        Assert.assertTrue("shouldn't match word 'tcher'", !testWord(matcher, "tcher"));
        Assert.assertTrue("shouldn't match word 'random'", !testWord(matcher, "random"));
    }
}
