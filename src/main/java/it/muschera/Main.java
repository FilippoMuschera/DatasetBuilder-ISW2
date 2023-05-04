package it.muschera;

import it.muschera.entities.BookkeeperEntity;
import it.muschera.entities.OpenJPAEntity;
import it.muschera.execution.Executor;

import static java.lang.System.*;

public class Main {
    public static void main(String[] args) {
        execProject("bookkeeper");
        execProject("openjpa");


    }


    private static void execProject(String projName) {
        if (projName.equals("bookkeeper")) {
            BookkeeperEntity b = BookkeeperEntity.getInstance();
            b.checkInit();
        }
        else if (projName.equals("openjpa")) {

            OpenJPAEntity jpa = OpenJPAEntity.getInstance();
            jpa.checkInit();
        }
        else
        {
            err.println("Progetto Sconosciuto");
            return;
        }

        out.println("Progetto caricato correttamente");
        Executor exec = new Executor(projName);
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