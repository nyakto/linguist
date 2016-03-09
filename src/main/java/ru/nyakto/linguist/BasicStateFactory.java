package ru.nyakto.linguist;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

public class BasicStateFactory<T extends State> implements StateFactory<T> {
    private final FSM fsm;
    private final BiFunction<FSM, Integer, T> constructor;
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public BasicStateFactory(FSM fsm, BiFunction<FSM, Integer, T> constructor) {
        this.fsm = fsm;
        this.constructor = constructor;
    }

    @Override
    public T createState() {
        return constructor.apply(fsm, idGenerator.getAndIncrement());
    }
}
