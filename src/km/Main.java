package km;

import km.algorithms.SimulatedAnnealing;
import km.data.ConfigLoader;
import km.data.CSVWriter;
import km.model.TSPProblem;
import km.ui.Display;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CSVWriter csvWriter = null;
        Scanner scanner = new Scanner(System.in);

        try {
//            // Pytanie o ścieżkę do pliku konfiguracyjnego
//            System.out.println("Podaj ścieżkę do pliku konfiguracyjnego: ");
//            String configFilePath = scanner.nextLine();

            // Wczytanie pliku konfiguracyjnego
            ConfigLoader configLoader = new ConfigLoader("pea3_config.txt");

            // Pobieranie konfiguracji
            String inputFilePath = configLoader.getProperty("inputData");
            long stopTime = configLoader.getIntProperty("stopTime");
            double coolingRate = configLoader.getDoubleProperty("coolingRate");
            double initialTemperature = configLoader.getDoubleProperty("initialTemperature");
            String outputFilePath = configLoader.getProperty("outputFile");

            // Inicjalizacja CSVWriter
            csvWriter = new CSVWriter();
            csvWriter.setFilePath(outputFilePath);

            // Wczytanie danych z pliku
            TSPProblem problem = TSPProblem.loadFromFile(inputFilePath);

            // Wykonanie algorytmu Symulated Annealing
            SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(problem, initialTemperature, coolingRate, stopTime);

            long startTime = System.nanoTime();
            List<Integer> solution = simulatedAnnealing.solve();
            long elapsedTime = System.nanoTime() - startTime;

            // Wyświetlenie wyników
            int totalDistance = calculateTotalDistance(solution, problem);
            Display.displayRoute(solution);
            Display.displayDistance(totalDistance);
            Display.displayExecutionTime(elapsedTime);

            // Zapis wyników do pliku CSV
            csvWriter.writeRecord(problem.getCitiesCount(), problem.getDistanceMatrix(), "Simulated Annealing", elapsedTime, elapsedTime / 1_000_000);

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
}
