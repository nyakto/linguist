package ru.nyakto.linguist.dfa;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.nyakto.linguist.FSM;
import ru.nyakto.linguist.State;
import ru.nyakto.linguist.Walker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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

	protected <M> void addMarkedWordToMatcher(DFA<MarkedState<M>, Character> matcher, String word, M mark) {
		MarkedState<M> currentState = matcher.getInitialState();
		for (char by : word.toCharArray()) {
			currentState = matcher.addTransition(currentState, by);
		}
		matcher.markStateAsFinal(currentState);
		currentState.setMark(mark);
	}

	protected boolean testWord(DFA<?, Character> matcher, String word) {
		final Walker<Character> walker = matcher.walker();
		for (char by : word.toCharArray()) {
			if (!walker.go(by)) {
				return false;
			}
		}
		return walker.isInFinalState();
	}

	protected <M> boolean testWord(DFA<MarkedState<M>, Character> matcher, String word, M mark) {
		final DFAWalker<MarkedState<M>, Character> walker = matcher.walker(matcher.getInitialState());
		for (char by : word.toCharArray()) {
			if (!walker.go(by)) {
				return false;
			}
		}
		return walker.isInFinalState() && walker.getCurrentState().getMark().equals(Optional.ofNullable(mark));
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
		final DFA<State, Character> originalMatcher = createWordsMatcher(Arrays.asList(
			"this",
			"is"
		));
		final DFA<State, Character> matcher = originalMatcher.minimize();
		Assert.assertEquals(6, originalMatcher.getStates().size());
		Assert.assertEquals(4, matcher.getStates().size());
		Assert.assertTrue("shouldn't match empty string", !testWord(matcher, ""));
		Assert.assertTrue("should match word 'this'", testWord(matcher, "this"));
		Assert.assertTrue("should match word 'is'", testWord(matcher, "is"));
		Assert.assertTrue("shouldn't match word 'random'", !testWord(matcher, "random"));
		Assert.assertTrue("shouldn't match word 't'", !testWord(matcher, "t"));
		Assert.assertTrue("shouldn't match word 'th'", !testWord(matcher, "th"));
		Assert.assertTrue("shouldn't match word 'thi'", !testWord(matcher, "thi"));
		Assert.assertTrue("shouldn't match word 'i'", !testWord(matcher, "i"));
		Assert.assertTrue("shouldn't match word 's'", !testWord(matcher, "s"));
	}

	@Test
	public void markedStatesShouldObstructMinimization() {
		final DFA<MarkedState<Integer>, Character> originalMatcher = new DFA<>(MarkedState::new);
		addMarkedWordToMatcher(originalMatcher, "this", 1);
		addMarkedWordToMatcher(originalMatcher, "is", 2);
		final DFA<MarkedState<Integer>, Character> matcher = originalMatcher.minimize(
			MarkedState::new, MarkedState::compare, MarkedState::merge
		);
		Assert.assertEquals(6, matcher.getStates().size());
		Assert.assertTrue("shouldn't match empty string", !testWord(matcher, ""));
		Assert.assertTrue("should match word 'this'", testWord(matcher, "this", 1));
		Assert.assertTrue("should match word 'is'", testWord(matcher, "is", 2));
		Assert.assertTrue("shouldn't match word 'random'", !testWord(matcher, "random"));
	}

	static class MarkedState<M> extends State {
		private Optional<M> mark = Optional.empty();

		public MarkedState(FSM fsm, long id) {
			super(fsm, id);
		}

		public Optional<M> getMark() {
			return mark;
		}

		public void setMark(M mark) {
			this.mark = Optional.ofNullable(mark);
		}

		public static <M> boolean compare(MarkedState<M> a, MarkedState<M> b) {
			return a.mark.equals(b.mark);
		}

		public static <M> void merge(Set<MarkedState<M>> oldStates, MarkedState<M> newState) {
			newState.setMark(
				oldStates.iterator().next()
					.getMark()
					.orElse(null)
			);
		}
	}
}
