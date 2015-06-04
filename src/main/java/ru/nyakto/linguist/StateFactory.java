package ru.nyakto.linguist;

@FunctionalInterface
public interface StateFactory<T extends State> {
    public T createState();
}
