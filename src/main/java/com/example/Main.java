package com.example;

import com.example.api.ElpriserAPI;

public class Main {
    public static void main(String[] args) {
        ElpriserAPI elpriserAPI = new ElpriserAPI();

        elpriserAPI.getPriser("2025-09-24", ElpriserAPI.Prisklass.SE2);

        System.out.print();
    }
}
