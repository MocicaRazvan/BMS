package com.mocicarazvan.templatemodule.utils;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomizerTest {
    @Test
    void pickRandomItemsFromEmptyListReturnsEmptyList() {
        List<String> emptyList = Collections.emptyList();
        List<String> result = Randomizer.pickRandomItemsFromList(emptyList);
        assertTrue(result.isEmpty());
    }

    @RepeatedTest(10)
    void pickRandomItemsFromListReturnsSubsetOfOriginalList() {
        List<Integer> originalList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> result = Randomizer.pickRandomItemsFromList(originalList);

        assertTrue(originalList.containsAll(result));
        assertTrue(result.size() <= originalList.size());
    }

    @Test
    void pickRandomItemFromEmptyListReturnsNull() {
        List<String> emptyList = Collections.emptyList();
        String result = Randomizer.pickRandomItemFromList(emptyList);
        assertNull(result);
    }

    @Test
    void pickRandomItemFromSingleItemListReturnsThatItem() {
        List<String> singleItemList = Collections.singletonList("only");
        String result = Randomizer.pickRandomItemFromList(singleItemList);
        assertEquals("only", result);
    }

    @RepeatedTest(10)
    void pickRandomItemFromListReturnsItemInList() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Integer result = Randomizer.pickRandomItemFromList(list);
        assertTrue(list.contains(result));
    }

    @Test
    void shuffleEmptyListReturnsEmptyList() {
        List<String> emptyList = Collections.emptyList();
        List<String> result = Randomizer.shuffleList(emptyList);
        assertTrue(result.isEmpty());
    }

    @Test
    void shuffleListRetainsAllOriginalElements() {
        List<Integer> originalList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> result = Randomizer.shuffleList(originalList);

        assertEquals(new HashSet<>(originalList), new HashSet<>(result));
        assertEquals(originalList.size(), result.size());
    }

    @RepeatedTest(10)
    void addRandomNumberReturnsValueWithinBounds() {
        double lower = 10.0;
        double upper = 20.0;
        double result = Randomizer.addRandomNumber(lower, upper);

        assertTrue(result >= lower);
        assertTrue(result <= upper);
    }

    @Test
    void addRandomNumberWithEqualBoundsReturnsExactValue() {
        double value = 15.0;
        double result = Randomizer.addRandomNumber(value, value);
        assertEquals(value, result, 0.0001);
    }

    @Test
    void pickRandomItemsFromListWithAllIdenticalItems() {
        List<String> identicalItems = Arrays.asList("same", "same", "same");
        List<String> result = Randomizer.pickRandomItemsFromList(identicalItems);
        assertTrue(result.stream().allMatch(item -> item.equals("same")));
    }

    @Test
    void pickRandomItemsFromListWithSingleItem() {
        List<String> singleItemList = Collections.singletonList("only");
        List<String> result = Randomizer.pickRandomItemsFromList(singleItemList);
        assertEquals(singleItemList, result);
    }

    @Test
    void pickRandomItemFromListWithAllIdenticalItems() {
        List<String> identicalItems = Arrays.asList("same", "same", "same");
        String result = Randomizer.pickRandomItemFromList(identicalItems);
        assertEquals("same", result);
    }

    @Test
    void shuffleListWithAllIdenticalItems() {
        List<String> identicalItems = Arrays.asList("same", "same", "same");
        List<String> result = Randomizer.shuffleList(identicalItems);
        assertEquals(identicalItems, result);
    }

    @Test
    void addRandomNumberWithNegativeBounds() {
        double lower = -10.0;
        double upper = -5.0;
        double result = Randomizer.addRandomNumber(lower, upper);
        assertTrue(result >= lower);
        assertTrue(result <= upper);
    }

    @Test
    void addRandomNumberWithZeroBounds() {
        double lower = 0.0;
        double upper = 0.0;
        double result = Randomizer.addRandomNumber(lower, upper);
        assertEquals(0.0, result, 0.0001);
    }
}