package ru.nyakto.linguist;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

public class BasicStateFactory<T extends State> implements StateFactory<T> {
    private final FSM fsm;
    private final BiFunction<FSM, Long, T> constructor;
    private final AtomicLong idGenerator = new AtomicLong(0l);

    public BasicStateFactory(FSM fsm, BiFunction<FSM, Long, T> constructor) {
        this.fsm = fsm;
        this.constructor = constructor;
    }

    @Override
    public T createState() {
        return constructor.apply(fsm, idGenerator.getAndIncrement());
    }
}
