package km;

import km.algorithms.SimulatedAnnealing;
import km.data.ConfigLoader;
import km.data.CSVWriter;
import km.model.TSPProblem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CSVWriter csvWriter = null;
        Scanner scanner = new Scanner(System.in);

        try {
            // Wczytanie pliku konfiguracyjnego
            ConfigLoader configLoader = new ConfigLoader("pea3_config.txt");

            // Pobieranie konfiguracji
            String inputFilePath = configLoader.getProperty("inputData");
            double coolingRate = configLoader.getDoubleProperty("coolingRate");
            int stopTime = configLoader.getIntProperty("stopTime");
            double initialTemperature = configLoader.getDoubleProperty("initialTemperature");
            String outputFilePath = configLoader.getProperty("outputFile");
            int testMode = configLoader.getIntProperty("testMode"); // Wartość trybu testowego (0 lub 1)
            String initialSolutionMethod = configLoader.getProperty("initialSolutionMethod"); // Metoda generowania rozwiązania początkowego
            String coolingMethod = configLoader.getProperty("coolingMethod"); // Domyślnie geometric

            // Inicjalizacja CSVWriter
            csvWriter = new CSVWriter();
            csvWriter.setFilePath(outputFilePath);
            csvWriter.writeRecordHeader("Plik", "Iteracja", "Najlepsza sciezka", "Blad wzgledny (%)", "Czas wykonania (ns)", "Czas wykonania (ms)", "Sredni blad wzgledny (%)", "Sredni czas wykonania (ns)", "Sredni czas wykonania (ms)");

            if (testMode == 1) {
                // Tryb testowy - algorytm uruchamiany raz
                System.out.println("Plik: " + inputFilePath);
                TSPProblem problem = TSPProblem.loadFromFile(inputFilePath);

                long startTimeNano = System.nanoTime();
                SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(problem, initialTemperature, coolingRate, stopTime, initialSolutionMethod, coolingMethod); // Zakładamy czas wykonania w sekundach
                List<Integer> solution = simulatedAnnealing.solve(1000000);
                long elapsedTimeNano = System.nanoTime() - startTimeNano;
                long elapsedTimeMs = elapsedTimeNano / 1_000_000;

                int bestDistance = calculateTotalDistance(solution, problem);
                System.out.printf("Najlepsza odleglosc = %d, Czas wykonania = %d ns (%d ms)%n",
                        bestDistance, elapsedTimeNano, elapsedTimeMs);
            } else {
                // Standardowy tryb - przetwarzanie plików i wielokrotne uruchomienia
                int[] executionTimes = {60, 120, 180}; // Czas wykonania w sekundach: 1 min, 2 min, 3 min
                int[] optimalSolutions = {1776, 2755, 2465};    // Znane optymalne wartości
                String[] files = {"ftv47.atsp", "ftv170.atsp", "rbg403.atsp"};

                for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
                    String file = files[fileIndex];
                    int optimalSolution = optimalSolutions[fileIndex];
                    int executionTime = executionTimes[fileIndex];

                    System.out.println("Plik: " + file + "(" + optimalSolution + ")");
                    TSPProblem problem = TSPProblem.loadFromFile(file);

                    double totalRelativeError = 0.0;
                    long totalExecutionTimeNs = 0;
                    long totalExecutionTimeMs = 0;

                    List<Integer> bestOverallPath = null;
                    int bestOverallDistance = Integer.MAX_VALUE;

                    // Uruchomienie algorytmu 10 razy
                    for (int run = 1; run <= 10; run++) {
                        long startTimeNano = System.nanoTime();
                        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(problem, initialTemperature, coolingRate, executionTime, initialSolutionMethod, coolingMethod);

                        // Użycie solve() do znalezienia najlepszego rozwiązania
                        List<Integer> solution = simulatedAnnealing.solve(optimalSolution);
                        long elapsedTimeNano = System.nanoTime() - startTimeNano;
                        long elapsedTimeMs = elapsedTimeNano / 1_000_000;

                        int bestDistance = calculateTotalDistance(solution, problem);
                        double relativeError = calculateRelativeError(bestDistance, optimalSolution);

                        // Aktualizacja najlepszego ogólnego rozwiązania
                        if (bestDistance < bestOverallDistance) {
                            bestOverallDistance = bestDistance;
                            bestOverallPath = new ArrayList<>(solution);
                        }

                        // Dodaj wyniki do sumowania dla średnich wartości
                        totalRelativeError += relativeError;
                        totalExecutionTimeNs += elapsedTimeNano;
                        totalExecutionTimeMs += elapsedTimeMs;

                        System.out.printf("Iteracja %d: Najlepsza odleglosc = %d, Blad wzgledny = %.2f%%, Czas wykonania = %d ns (%d ms), Najlepsza sciezka = %s%n",
                                run, bestDistance, relativeError, elapsedTimeNano, elapsedTimeMs, solution);

                        csvWriter.writeRecord(file, run, bestDistance, relativeError, elapsedTimeNano, elapsedTimeMs, "-");
                    }

                    // Obliczanie średnich wartości
                    double averageRelativeError = totalRelativeError / 10;
                    double averageExecutionTimeNs = (double) totalExecutionTimeNs / 10;
                    double averageExecutionTimeMs = (double) totalExecutionTimeMs / 10;

                    // Zapis średnich do pliku CSV
                    csvWriter.writeAverageRecord(file, averageRelativeError, averageExecutionTimeNs, averageExecutionTimeMs);
                    csvWriter.writeRecord(file, -1, bestOverallDistance, -1, -1, -1, bestOverallPath.toString());

                    System.out.printf("Plik %s: Sredni blad wzgledny = %.2f%%, Sredni czas wykonania = %.2f ns (%.2f ms)%n",
                            file, averageRelativeError, averageExecutionTimeNs, averageExecutionTimeMs);
                }
            }

        } catch (IOException e) {
            System.err.println("Błąd podczas wczytywania konfiguracji lub danych: " + e.getMessage());
        } finally {
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException e) {
                    System.err.println("Błąd podczas zamykania pliku CSV: " + e.getMessage());
                }
            }
            scanner.close();
        }
    }

    private static int calculateTotalDistance(List<Integer> solution, TSPProblem problem) {
        int distance = 0;
        for (int i = 0; i < solution.size() - 1; i++) {
            distance += problem.getDistance(solution.get(i), solution.get(i + 1));
        }
        distance += problem.getDistance(solution.get(solution.size() - 1), solution.get(0));
        return distance;
    }

    private static double calculateRelativeError(int foundDistance, int optimalDistance) {
        return ((double) (foundDistance - optimalDistance) / optimalDistance) * 100;
    }
}
