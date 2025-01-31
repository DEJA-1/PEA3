package km.algorithms;

import km.model.TSPProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealing extends Algorithm {
    private final TSPProblem problem;
    private final double initialTemperature;
    private final double coolingRate;
    private final long stopTime;
    private final String initialSolutionMethod;
    private final String coolingMethod;

    public SimulatedAnnealing(TSPProblem problem, double initialTemperature, double coolingRate, long stopTime, String initialSolutionMethod, String coolingMethod) {
        this.problem = problem;
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.stopTime = stopTime;
        this.initialSolutionMethod = initialSolutionMethod;
        this.coolingMethod = coolingMethod;
    }

    @Override
    public List<Integer> solve(int optimalSolution) {
        int citiesCount = problem.getCitiesCount();
        Random random = new Random();

        List<Integer> currentSolution = generateInitialSolution(citiesCount);
        List<Integer> bestSolution = new ArrayList<>(currentSolution);

        int currentDistance = calculateTotalDistance(currentSolution);
        int bestDistance = currentDistance;

        double temperature = initialTemperature;
        long startTime = System.currentTimeMillis();
        int iteration = 1;

        while ((System.currentTimeMillis() - startTime) < stopTime * 1000) {
            List<Integer> newSolution = generateNeighbor(currentSolution);
            int newDistance = calculateTotalDistance(newSolution);

            if (acceptanceProbability(currentDistance, newDistance, temperature) > random.nextDouble()) {
                currentSolution = new ArrayList<>(newSolution);
                currentDistance = newDistance;
            }

            if (currentDistance < bestDistance) {
                bestSolution = new ArrayList<>(currentSolution);
                bestDistance = currentDistance;
            }

            // Chłodzenie: wybór metody na podstawie konfiguracji
            if ("logarithmic".equalsIgnoreCase(coolingMethod)) {
                temperature = logCooling(iteration); // Logarytmiczne chłodzenie
            } else {
                temperature *= coolingRate;
            }

            // Jeśli znaleziono optymalne rozwiązanie, zakończ wcześniej
            if (currentDistance == optimalSolution) {
                System.out.printf("Optimal solution found: %d. Terminating early.%n", currentDistance);
                break;
            }

            iteration++;
        }

        return bestSolution;
    }

    // Logarytmiczne chłodzenie
    private double logCooling(int iteration) {
        return initialTemperature / (1 + coolingRate * Math.log(1 + iteration));
    }


    // Wybór metody generowania początkowego rozwiązania
    private List<Integer> generateInitialSolution(int citiesCount) {
        if ("random".equalsIgnoreCase(initialSolutionMethod)) {
            return generateRandomSolution(citiesCount);
        } else if ("nearestNeighbor".equalsIgnoreCase(initialSolutionMethod)) {
            return generateNearestNeighborSolution(citiesCount);
        } else {
            throw new IllegalArgumentException("Unknown initial solution method: " + initialSolutionMethod);
        }
    }

    // Metoda losowego generowania rozwiązania
    private List<Integer> generateRandomSolution(int citiesCount) {
        List<Integer> solution = new ArrayList<>();
        for (int i = 0; i < citiesCount; i++) {
            solution.add(i);
        }
        Collections.shuffle(solution);
        return solution;
    }

    // Metoda zachłanna (nearest neighbor) do generowania rozwiązania
    private List<Integer> generateNearestNeighborSolution(int citiesCount) {
        boolean[] visited = new boolean[citiesCount];
        List<Integer> path = new ArrayList<>();

        // Losowy wybór pierwszego miasta
        Random random = new Random();
        int currentCity = random.nextInt(citiesCount);
        path.add(currentCity);
        visited[currentCity] = true; // Dodanie miasta początkowego do ścieżki i oznaczenie jako odwiedzone

        for (int i = 1; i < problem.getCitiesCount(); i++) {
            int nextCity = -1;
            int shortestDistance = Integer.MAX_VALUE;

            for (int j = 0; j < problem.getCitiesCount(); j++) {
                if (!visited[j] && problem.getDistance(currentCity, j) < shortestDistance) {
                    nextCity = j;
                    shortestDistance = problem.getDistance(currentCity, j);
                }
            }

            currentCity = nextCity;
            path.add(currentCity);
            visited[currentCity] = true; // Dodanie wybranego miasta do ścieżki i oznaczenie jako odwiedzone
        }

        return path;
    }


    private int calculateTotalDistance(List<Integer> solution) {
        int distance = 0;
        for (int i = 0; i < solution.size() - 1; i++) {
            distance += problem.getDistance(solution.get(i), solution.get(i + 1));
        }
        distance += problem.getDistance(solution.get(solution.size() - 1), solution.get(0));
        return distance;
    }

    private double acceptanceProbability(int currentDistance, int newDistance, double temperature) {
        if (newDistance < currentDistance) {
            return 1.0;
        }
        return Math.exp((currentDistance - newDistance) / temperature);
    }

    private List<Integer> generateNeighbor(List<Integer> solution) {
        List<Integer> neighbor = new ArrayList<>(solution);
        Random random = new Random();
        int i = random.nextInt(neighbor.size());
        int j = random.nextInt(neighbor.size());
        Collections.swap(neighbor, i, j);
        return neighbor;
    }
}
