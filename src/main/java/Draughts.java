package main.java;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Draughts {

    public static final String GAURAV = "Gaurav";

    private String getCsvUsingForLoops(final String... input) {
        String result = "";
        final Set<String> distinctStrings = new HashSet<>(Arrays.asList(input));
        distinctStrings.remove("");
        distinctStrings.remove(null);
        final String[] sortedStrings = distinctStrings.toArray(new String[distinctStrings.size()]);
        Arrays.sort(sortedStrings);
        for (final String x : sortedStrings) {
            result += x + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    private Optional<String> getCsvUsingStream(final String... input) {
        return Arrays.stream(input)
                .filter(x -> !x.isEmpty())
                .distinct()
                .sorted()
                .findFirst();
    }

    private boolean getLargest(final String input[]) {
        return Arrays.stream(input).max(String::compareTo).isPresent();
    }

    private void peek(final String... input) {
        Arrays.stream(input)
                .filter(x -> !x.isEmpty())
                .distinct()
                .sorted()
                .map(String::length)
                .peek(System.out::println)
                ;
    }

    public static void main(String[] args) {
        final Draughts draughts = new Draughts();
        final String[] input = {"Hahah"};
        final Map<String, List<String>> map=new ConcurrentHashMap<>();
        map.put(GAURAV,new ArrayList<>());
        map.get(GAURAV).add("Sen");
        System.out.println(map.get(GAURAV));
        deleteElements(map.get(GAURAV));
        System.out.println(map.get(GAURAV));
        draughts.peek(input);
    }

    private static void deleteElements(List<String> gaurav) {
        gaurav.remove(0);
    }
}
