package ru.nyakto.linguist.grammar.parser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ParserUtils {
    public static <T> void resolveDependencies(Map<T, Set<T>> deps) {
        boolean modified;
        do {
            modified = false;
            for (T item : deps.keySet()) {
                if (resolveDependencies(deps, item)) {
                    modified = true;
                }
            }
        } while (modified);
    }

    private static <T> boolean resolveDependencies(Map<T, Set<T>> deps, T item) {
        final Set<T> itemDeps = deps.get(item);
        if (itemDeps == null) {
            return false;
        }
        final Set<T> task = new HashSet<>(itemDeps);
        boolean modified = false;
        for (T dep : task) {
            final Set<T> depDeps = deps.get(dep);
            if (depDeps != null && itemDeps.addAll(depDeps)) {
                modified = true;
            }
        }
        return modified;
    }
}
