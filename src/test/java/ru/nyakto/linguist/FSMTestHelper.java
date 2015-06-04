package ru.nyakto.linguist;

abstract public class FSMTestHelper {
    protected boolean testWord(FSM<?, Character> matcher, String word) {
        final Walker<Character> walker = matcher.walker();
        for (char by : word.toCharArray()) {
            if (!walker.go(by)) {
                return false;
            }
        }
        return walker.isInFinalState();
    }
}
