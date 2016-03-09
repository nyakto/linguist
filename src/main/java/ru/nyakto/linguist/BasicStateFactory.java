package ru.nyakto.linguist;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BasicStateFactory<T extends State> implements StateFactory<T> {
    private final Function<Integer, T> constructor;
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    public BasicStateFactory(Function<Integer, T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T createState() {
        return constructor.apply(idGenerator.getAndIncrement());
    }

    @Override
    public BasicStateFactory<T> cloneFactory() {
        return new BasicStateFactory<>(constructor);
    }
}
