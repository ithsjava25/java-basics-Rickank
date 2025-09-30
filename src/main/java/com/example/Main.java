package com.example;

import com.example.api.ElpriserAPI;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        try {
            // sätt svensk locale för datum och text
            Locale.setDefault(new Locale("sv", "SE"));

            ElpriserAPI elpriserAPI = new ElpriserAPI();
            ElpriserAPI.Prisklass zon = null;

            LocalDate date = LocalDate.now();

            if (args.length == 0) {
                System.out.println("Usage: ");
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

                // validera zoner
                if (args[0].equals("--zone")) {
                    if (!Pattern.matches("^SE[1-4]$", args[1])) {
                        System.out.println("Invalid zone");
                        return;
                    }
                    zon = ElpriserAPI.Prisklass.valueOf(args[1]);
                    boolean validArgs = true;

                    // hantera ytterligare argument
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
                            default -> {
                                System.out.println("Invalid argument " + args[i]);
                                help();
                                validArgs = false;
                            }
                        }
                    }

                    if (validArgs) {
                        System.out.println("Zone: " + zon + ", Date: " + date);
                        // TODO: prishämtning
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

    private static void help() {
        System.out.println("Commands:\n" +
                "--zone SE1|SE2|SE3|SE4 (required)\n" +
                "--date YYYY-MM-DD\n" +
                "--sorted\n" +
                "--charging 2h|4h|8h\n" +
                "--help");
    }
}