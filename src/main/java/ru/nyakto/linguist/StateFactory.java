package ru.nyakto.linguist;

public interface StateFactory<T extends State> {
    public T createState();

    public StateFactory<T> cloneFactory();
}
