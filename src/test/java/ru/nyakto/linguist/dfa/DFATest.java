package ru.nyakto.linguist.dfa;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.Walker;

import java.util.Arrays;
import java.util.Collection;

@RunWith(JUnit4.class)
public class DFATest {
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

	protected boolean testWord(DFA<State, Character> matcher, String word) {
		final Walker<Character> walker = matcher.walker();
		for (char by : word.toCharArray()) {
			if (!walker.go(by)) {
				return false;
			}
		}
		return walker.isInFinalState();
	}

	@Test
	public void basicWalkerTest() {
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
}
