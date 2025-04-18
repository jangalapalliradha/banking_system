package com.gic.service;


import java.util.Scanner;

public interface BankingService {

     void processTransaction(Scanner Scanner);
     void applyInterstRates(Scanner scanner);
     void printStatement(Scanner scanner);

     void loadTrasactions();


}
