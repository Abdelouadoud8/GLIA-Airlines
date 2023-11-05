import java.util.*;
import java.util.stream.Collectors;

public class Question1_2 {
    public static void main(String[] args) {
        for (Instance instance : Instance.values()) {
            int n = instance.nb_dividers;
            int m = instance.capacity;
            int[] exits = instance.exits;
            long start = System.nanoTime();
            dividers(n, m, exits);
            long end = System.nanoTime();
            double duration = (end - start) / 60000000000d;
            System.out.print("Execution time (in minutes): ");
            System.out.println(duration);
            System.out.println("_____________________________________");
        }
    }


    public static List<Integer> dividers(int n, int m, int[] exits) {
        int[] combination = new int[n];
        combination[0] = 0;

        for (int i = 1; i < n; i++) {
            combination[i] = 2 + i - 1;
        }

        int index = 0;
        int iterations=0;

        while (true) {

            List<Integer> dividers = Arrays.stream(combination)
                    .boxed()
                    .collect(Collectors.toList());
            if(isValid(dividers,exits)){
                System.out.print("Solution found is: ");
                System.out.println(dividers);
                System.out.print("Number of iterations: ");
                System.out.println(iterations);
                return dividers;
            }

            index = n - 1;
            while (index >= 0 && combination[index] == m - n + index + 1 ) {
                iterations++;
                index--;
            }

            if (index < 0) {
                break;
            }
            combination[index]++;
            for (int i = index + 1; i < n; i++) {
                iterations++;
                combination[i] = combination[i - 1] + 1;
            }
            iterations++;

        }
        return null;
    }

    private static boolean isValid(List<Integer> dividers, int[] exits) {

        List<Boolean> exitsOccupation=new ArrayList<>();
        for (int exit : exits) exitsOccupation.add(dividers.contains(exit));
        if(!exitsOccupation.contains(false)) return false;

        if (Math.abs(dividers.get(1)-dividers.get(0) ) < 2)  return false;

        List<Integer> distances = new ArrayList<>();
        for (int i = 0; i < dividers.size(); i++) {
            for (int j = i+1; j < dividers.size(); j++) {
                int distance = Math.abs(dividers.get(i) - dividers.get(j));
                distances.add(distance);
            }
        }
        Set<Integer> uniqueDistances = new HashSet<>(distances);
        return uniqueDistances.size() >= distances.size();
    }
}