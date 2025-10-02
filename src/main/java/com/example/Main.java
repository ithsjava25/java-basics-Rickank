package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        // sätt svensk locale för datum och text
        try {
            Locale.setDefault(new Locale("sv", "SE"));

            ElpriserAPI elpriserAPI = new ElpriserAPI();
            ElpriserAPI.Prisklass zon;

            LocalDate date = LocalDate.now();

            if (args.length == 0) {
                System.out.println("Usage: ");
                help();
            } else if (args.length == 1 && args[0].equals("--help")) {
                help();
            } else {
                if (args.length < 2) {
                    System.out.println("Zone required");
                    return;
                }

                // validera zoner och laddningstider
                if (args[0].equals("--zone")) {
                    if (!Pattern.matches("^SE[1-4]$", args[1])) {
                        System.out.println("Invalid zone");
                        return;
                    }
                    zon = ElpriserAPI.Prisklass.valueOf(args[1]);
                    boolean validArgs = true;

                    for (int i = 2; i < args.length; i++) {
                        switch (args[i]) {
                            case "--date" -> {
                                if (i + 1 < args.length) {
                                    date = checkDate(args[++i]);
                                } else {
                                    System.out.println("Missing value for --date");
                                    validArgs = false;
                                }
                            }
                            case "--sorted" -> {
                            }
                            case "--charging" -> {
                                if (i + 1 < args.length) {
                                    String duration = args[++i];
                                    if (!duration.equals("2h") && !duration.equals("4h") && !duration.equals("8h")) {
                                        System.out.println("Invalid charging duration: " + duration);
                                        validArgs = false;
                                    }
                                } else {
                                    System.out.println("Missing value for --charging");
                                    validArgs = false;
                                }
                            }
                            case "2h", "4h", "8h" -> {
                            }
                            default -> {
                                System.out.println("Invalid argument " + args[i]);
                                help();
                                validArgs = false;
                            }
                        }
                    }

                    if (validArgs) {
                        System.out.println("Zone: " + zon + ", Date: " + date);
                        LocalDate tomorrow = date.plusDays(1);
                        var pricesToday = elpriserAPI.getPriser(date, zon);
                        var pricesTomorrow = elpriserAPI.getPriser(tomorrow, zon);

                        var allPricesSet = new java.util.LinkedHashSet<>(pricesToday);
                        allPricesSet.addAll(pricesTomorrow);
                        var allPrices = new java.util.ArrayList<>(allPricesSet);

                        if (allPrices.isEmpty()) {
                            System.out.println("Inga priser tillgängliga");
                            return;
                        }

                        calculateAndDisplayStats(allPrices);
                        System.out.println("Alla priser:");
                        displayPrices(allPrices, args);

                        // kolla om användaren fråga om charging optimization
                        for (int i = 0; i < args.length; i++) {
                            if (args[i].equals("--charging") && i + 1 < args.length) {
                                String duration = args[i + 1];
                                int hours = Integer.parseInt(duration.replace("h", ""));
                                findBestChargingWindow(allPrices, hours);
                            }
                        }
                    }
                } else {
                    System.out.println("Zone required");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // kollar datum, om ogiltlig printar dagens datum
    private static LocalDate checkDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            System.out.println("Invalid date");
            return LocalDate.now();
        }
    }

    private static void calculateAndDisplayStats(java.util.List<ElpriserAPI.Elpris> prices) {
        double mean = prices.stream().mapToDouble(ElpriserAPI.Elpris::sekPerKWh).average().orElse(0);
        ElpriserAPI.Elpris min = prices.stream().min(java.util.Comparator.comparingDouble(ElpriserAPI.Elpris::sekPerKWh)).get();
        ElpriserAPI.Elpris max = prices.stream().max(java.util.Comparator.comparingDouble(ElpriserAPI.Elpris::sekPerKWh)).get();

        System.out.printf("Medelpris: %.2f öre%n", mean * 100);
        System.out.printf("Lägsta pris: %.2f öre (%02d-%02d)%n", min.sekPerKWh() * 100, min.timeStart().getHour(), min.timeStart().getHour() + 1);
        System.out.printf("Högsta pris: %.2f öre (%02d-%02d)%n", max.sekPerKWh() * 100, max.timeStart().getHour(), max.timeStart().getHour() + 1);
    }

    private static void displayPrices(java.util.List<ElpriserAPI.Elpris> prices, String[] args) {
        boolean sorted = java.util.Arrays.asList(args).contains("--sorted");

        var priceList = sorted ?
                prices.stream()
                        .sorted(java.util.Comparator.comparingDouble(ElpriserAPI.Elpris::sekPerKWh))
                        .toList() :
                prices;

        for (var price : priceList) {
            String time = String.format("%02d-%02d",
                    price.timeStart().getHour(),
                    (price.timeStart().getHour() + 1) % 24);
            double ore = price.sekPerKWh() * 100;
            System.out.printf("%s %.2f öre%n", time, ore);
        }
    }

    // ny metod för charging optimization
    private static void findBestChargingWindow(
            java.util.List<ElpriserAPI.Elpris> prices,
            int hours
    ) {
        if (prices.size() < hours) {
            System.out.println("Inte tillräckligt med priser för att beräkna laddning");
            return;
        }

        double bestAvg = Double.MAX_VALUE;
        int bestIndex = -1;

        // sliding window
        for (int i = 0; i <= prices.size() - hours; i++) {
            double sum = 0;
            for (int j = 0; j < hours; j++) {
                sum += prices.get(i + j).sekPerKWh();
            }
            double avg = sum / hours;
            if (avg < bestAvg) {
                bestAvg = avg;
                bestIndex = i;
            }
        }

        if (bestIndex >= 0) {
            var start = prices.get(bestIndex).timeStart();
            var end = prices.get(bestIndex + hours - 1).timeStart().plusHours(1);

            var dfs = new java.text.DecimalFormatSymbols(new Locale("sv", "SE"));
            var df = new java.text.DecimalFormat("0.00", dfs);

            String meanStr = df.format(bestAvg * 100);

            String timeRange = String.format("%02d-%02d",
                    start.getHour(),
                    end.getHour());

            System.out.printf("Påbörja laddning kl %s (Medelpris: %s öre)%n",
                    timeRange, meanStr);
        }
    }

    private static void help() {
        System.out.println("Commands:\n" +
                "--zone SE1|SE2|SE3|SE4 (required)\n" +
                "--date YYYY-MM-DD\n" +
                "--sorted\n" +
                "--charging 2h|4h|8h\n" +
                "--help");
    }
}
// TODO: ändra språk 