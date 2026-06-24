package org.example;

/**
 * Point d'entrée principal de l'application Password CLI.
 * Délègue toute l'interaction à la classe CLI.
 *
 * @author Milola
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
            CLI cli = new CLI();
            cli.start();

    }

}