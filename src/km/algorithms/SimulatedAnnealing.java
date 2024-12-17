package km.algorithms;

import km.model.TSPProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealing extends Algorithm {
    private final TSPProblem problem;
    private final double initialTemperature; // Początkowa temperatura algorytmu
    private final double coolingRate;        // Współczynnik chłodzenia (zmniejszania temperatury)
    private final long stopTime;             // Czas działania algorytmu w sekundach

    public SimulatedAnnealing(TSPProblem problem, double initialTemperature, double coolingRate, long stopTime) {
        this.problem = problem;
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.stopTime = stopTime;
    }

    @Override
    public List<Integer> solve() {
        int citiesCount = problem.getCitiesCount();
        Random random = new Random();

        // Rozwiązanie początkowe - losowa kolejność miast
        List<Integer> currentSolution = generateInitialSolution(citiesCount);
        List<Integer> bestSolution = new ArrayList<>(currentSolution);

        // Obliczenie długości początkowej trasy
        int currentDistance = calculateTotalDistance(currentSolution);
        int bestDistance = currentDistance;

        double temperature = initialTemperature; // Ustawienie temperatury początkowej
        long startTime = System.currentTimeMillis(); // Zapisanie czasu startu algorytmu

        // Główna pętla algorytmu symulowanego wyżarzania
        while (temperature > 1 && (System.currentTimeMillis() - startTime) < stopTime * 1000) {
            // Generowanie nowego rozwiązania w sąsiedztwie obecnego
            List<Integer> newSolution = generateNeighbor(currentSolution);
            int newDistance = calculateTotalDistance(newSolution);

            // Sprawdzenie warunku akceptacji nowego rozwiązania
            if (acceptanceProbability(currentDistance, newDistance, temperature) > random.nextDouble()) {
                currentSolution = new ArrayList<>(newSolution); // Akceptacja nowego rozwiązania
                currentDistance = newDistance; // Aktualizacja kosztu trasy
            }

            // Aktualizacja najlepszego znalezionego rozwiązania
            if (currentDistance < bestDistance) {
                bestSolution = new ArrayList<>(currentSolution);
                bestDistance = currentDistance;
            }

            // Obniżenie temperatury
            temperature *= coolingRate;
        }

        return bestSolution; // Zwrócenie najlepszego znalezionego rozwiązania
    }

    /**
     * Generowanie początkowego losowego rozwiązania.
     */
    private List<Integer> generateInitialSolution(int citiesCount) {
        List<Integer> solution = new ArrayList<>();
        for (int i = 0; i < citiesCount; i++) {
            solution.add(i); // Dodanie wszystkich miast do listy
        }
        Collections.shuffle(solution); // Losowe przetasowanie miast
        return solution;
    }

    /**
     * Generowanie rozwiązania sąsiedniego poprzez zamianę dwóch miast.
     */
    private List<Integer> generateNeighbor(List<Integer> solution) {
        List<Integer> neighbor = new ArrayList<>(solution);
        Random random = new Random();
        int i = random.nextInt(neighbor.size()); // Losowy indeks i
        int j = random.nextInt(neighbor.size()); // Losowy indeks j
        Collections.swap(neighbor, i, j); // Zamiana dwóch miast
        return neighbor;
    }

    /**
     * Obliczanie całkowitej długości trasy.
     */
    private int calculateTotalDistance(List<Integer> solution) {
        int distance = 0;
        for (int i = 0; i < solution.size() - 1; i++) {
            distance += problem.getDistance(solution.get(i), solution.get(i + 1)); // Odległość między kolejnymi miastami
        }
        distance += problem.getDistance(solution.get(solution.size() - 1), solution.get(0)); // Powrót do miasta startowego
        return distance;
    }

    /**
     * Obliczanie prawdopodobieństwa akceptacji gorszego rozwiązania.
     * Jeśli nowe rozwiązanie jest lepsze, prawdopodobieństwo wynosi 1.
     * Jeśli jest gorsze, prawdopodobieństwo zależy od temperatury.
     */
    private double acceptanceProbability(int currentDistance, int newDistance, double temperature) {
        if (newDistance < currentDistance) {
            return 1.0; // Akceptuj lepsze rozwiązanie bezwarunkowo
        }
        // Prawdopodobieństwo akceptacji gorszego rozwiązania
        return Math.exp((currentDistance - newDistance) / temperature);
    }
}
