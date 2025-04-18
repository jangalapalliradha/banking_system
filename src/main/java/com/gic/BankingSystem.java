package com.gic;

import com.gic.service.BankingService;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


import java.util.Scanner;

@SpringBootApplication
@EnableScheduling
public class BankingSystem implements CommandLineRunner {

    private final BankingService bankingService;

    public BankingSystem(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    public static void main(String[] args) {
        SpringApplication.run(BankingSystem.class, args);
    }
    /**
     * @param args input
     */
    @Override
    public void run(String... args)  {

        Scanner scanner = new Scanner(System.in);
        String input;

        while(true) {
            promptWelcomeText();
            input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "T":
                    bankingService.processTransaction(scanner);
                    break;
                case "I":
                    bankingService.applyInterstRates(scanner);
                    break;
                case "P":
                    bankingService.printStatement(scanner);
                    break;
                case "Q":
                    return;
                default:
                    System.out.println("Please enter a valid input");
            }
        }
    }

    @PostConstruct
    public void initBalances() {
        bankingService.loadTrasactions();
    }

    private static void promptWelcomeText() {
        System.out.print("``` ");
        System.out.println("\nWelcome to AwesomeGIC Bank! What would you like to do?");
        System.out.println("[T] Input transactions");
        System.out.println("[I] Define interest rule");
        System.out.println("[P] Print statement");
        System.out.println("[Q] Quit");
        System.out.println("> ");
        System.out.print("``` ");
    }
}
