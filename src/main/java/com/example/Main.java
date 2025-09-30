package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


public class Main {

    public static void main(String[] args) {
        try {
            ElpriserAPI elpriserAPI = new ElpriserAPI();
            ElpriserAPI.Prisklass zon = null;

            // defaultvärden

            LocalDate date = LocalDate.now();
            int window = 24;
            boolean sorted = false;

            if (args.length == 0) {
                System.out.println("Usage:");
                help();
                return;

            } else if (args.length == 1 && args[0].equals("--help")) {
                help();
                return;

            } else {
                if (args.length < 2) {
                    System.out.println("Zone required");
                    return;
                }

                if (args[0].equals("--zone")) {
                    if (!Pattern.matches("^SE[1-4]$", args[1])) {
                        System.out.println("Invalid zone");
                        return;
                    }
                    zon = ElpriserAPI.Prisklass.valueOf(args[1]);

                    // hämtar priserna från API

                    List<ElpriserAPI.Elpris> pricesForDate = priceOnDate(elpriserAPI, zon, date);

                    if (pricesForDate == null || pricesForDate.isEmpty()) {
                        System.out.println("No data available for " + date);
                        return;
                    } else {
                        System.out.println("Fetched " + pricesForDate.size() + " prices for " + date);

                        // lägga till kod för  --date, --charging, --sorted
                    }

                } else {
                    System.out.println("Zone required");
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<ElpriserAPI.Elpris> priceOnDate(ElpriserAPI elpriserAPI, ElpriserAPI.Prisklass zon, LocalDate date) {
        List<ElpriserAPI.Elpris> res = elpriserAPI.getPriser(date, zon);
        return res == null ? Collections.emptyList() : res;
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
