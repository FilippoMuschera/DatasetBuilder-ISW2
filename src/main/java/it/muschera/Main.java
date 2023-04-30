package it.muschera;

import it.muschera.entities.BookkeeperEntity;
import it.muschera.execution.Executor;

import static java.lang.System.*;

public class Main {
    public static void main(String[] args) {
        BookkeeperEntity b = BookkeeperEntity.getInstance();
        b.checkInit();
        out.println("Progetto caricato correttamente");
        Executor exec = new Executor("bookkeeper");
        exec.buildDataset();
        out.println("Dataset costruito correttamente");
        exec.getTickets();
        exec.getConsistentTickets();
        exec.evaluateBuggyness(); //Al momento non fa nulla actually
        out.println("Buggyness delle classi calcolata correttamente");
        exec.writeCsv();
        out.println("All done");

    }
}