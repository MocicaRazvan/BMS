package com.mocicarazvan.templatemodule.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Randomizer {
    public static <T> List<T> pickRandomItemsFromList(List<T> items) {
        if (items.isEmpty()) {
            return items;
        }

        int randomAmount = 1 + (int) (Math.random() * items.size());
        List<T> copy = new ArrayList<>(items);
        Collections.shuffle(copy);
        return copy.subList(0, randomAmount);
    }

    public static <T> T pickRandomItemFromList(List<T> items) {
        if (items.isEmpty()) {
            return null;
        }

        int randomIndex = (int) (Math.random() * items.size());
        return items.get(randomIndex);
    }

    public static <T> List<T> shuffleList(List<T> items) {
        List<T> copy = new ArrayList<>(items);
        Collections.shuffle(copy);
        return copy;
    }

    public static double addRandomNumber(double lower, double upper) {
        return lower + Math.random() * (upper - lower);
    }
}
