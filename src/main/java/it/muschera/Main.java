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
        exec.doProportion();
        exec.evaluateBuggyness();
        out.println("Buggyness delle classi calcolata correttamente");
        exec.writeFiles();
        out.println("All done");

    }
}