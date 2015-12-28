package ru.nyakto.linguist.dfa;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.FSMTestHelper;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.Walker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@RunWith(JUnit4.class)
public class DFATest extends FSMTestHelper {
    protected DFA<State, Character> createWordsMatcher(Collection<String> words) {
        DFA<State, Character> matcher = DFA.create();
        for (String word : words) {
            State currentState = matcher.getInitialState();
            for (char by : word.toCharArray()) {
                currentState = matcher.addTransition(currentState, by);
            }
            matcher.markStateAsFinal(currentState);
        }
        return matcher;
    }

    @Test
    public void matcherShouldRecognizeLanguage() {
        final DFA<State, Character> matcher = createWordsMatcher(Arrays.asList(
            "this",
            "is",
            "test",
            "matcher"
        ));
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
    public void minimizedMatcherShouldRecognizeSameLanguage() {
        final DFA<State, Character> originalMatcher1 = createWordsMatcher(Arrays.asList(
            "this",
            "is",
            "isa"
        ));
        final DFA<State, Character> matcher1 = originalMatcher1.minimize();
        Assert.assertEquals(8, originalMatcher1.getStates().size());
        Assert.assertEquals(7, matcher1.getStates().size());
        Assert.assertTrue("shouldn't match empty string", !testWord(matcher1, ""));
        Assert.assertTrue("should match word 'this'", testWord(matcher1, "this"));
        Assert.assertTrue("should match word 'is'", testWord(matcher1, "is"));
        Assert.assertTrue("should match word 'isa'", testWord(matcher1, "isa"));
        Assert.assertTrue("shouldn't match word 'random'", !testWord(matcher1, "random"));
        Assert.assertTrue("shouldn't match word 't'", !testWord(matcher1, "t"));
        Assert.assertTrue("shouldn't match word 'th'", !testWord(matcher1, "th"));
        Assert.assertTrue("shouldn't match word 'thi'", !testWord(matcher1, "thi"));
        Assert.assertTrue("shouldn't match word 'i'", !testWord(matcher1, "i"));
        Assert.assertTrue("shouldn't match word 's'", !testWord(matcher1, "s"));
        Assert.assertTrue("shouldn't match word 'thisa'", !testWord(matcher1, "thisa"));
    }
}
