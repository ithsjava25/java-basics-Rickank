package com.example;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equals("--help"))) {
            help();
            return;

        }
        System.out.println("Elprisoptimeraren");
    }

    //Expected CLI arguments
    private static void help() {
        System.out.println("Commands:\n" +
                "--zone SE1|SE2|SE3|SE4 (required)\n" +
                "--date YYYY-MM-DD\n" +
                "--sorted\n" +
                "--help");
    }
}