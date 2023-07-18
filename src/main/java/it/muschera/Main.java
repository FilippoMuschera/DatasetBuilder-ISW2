package it.muschera;

import it.muschera.entities.BookkeeperEntity;
import it.muschera.entities.OpenJPAEntity;
import it.muschera.execution.ExecutorV2;
import it.muschera.filescreators.ClassificatorReportWriter;

import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.System.*;

public class Main {
    public static void main(String[] args) throws IOException {

        long startTime = System.nanoTime();


        final String BK = "bookkeeper";
        final String JPA = "openjpa";

        ExecutorV2.cleanEnvironment();
        execProject(BK);
        ClassificatorReportWriter.avgReportWriter(BK); //per bookkeeper
        ClassificatorReportWriter.cleanAvg();
        execProject(JPA);
        ClassificatorReportWriter.writeReport();
        ClassificatorReportWriter.avgReportWriter(JPA);

        long endTime = System.nanoTime();

        // Calcolo del tempo di esecuzione in secondi
        double executionTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;

        // Calcolo del tempo di esecuzione in minuti
        double executionTimeInMinutes = executionTimeInSeconds / 60.0;

        out.println("Il programma è stato eseguito in " + executionTimeInSeconds + " secondi (" + executionTimeInMinutes + " minuti).");


    }


    private static void execProject(String projName) {
        if (projName.equals("bookkeeper")) {
            BookkeeperEntity b = BookkeeperEntity.getInstance();
            b.checkInit();
        } else if (projName.equals("openjpa")) {

            OpenJPAEntity jpa = OpenJPAEntity.getInstance();
            jpa.checkInit();
        } else {
            err.println("Progetto Sconosciuto");
            return;
        }

        out.println("Progetto caricato correttamente");
        ExecutorV2 exec = new ExecutorV2(projName);
        try {
            exec.runV2();
        } catch (Exception e) {
            err.println("Non è stato possibile eseguire il programma");
            Logger logger = Logger.getLogger(Main.class.getName());
            logger.info(e.getMessage());
        }
        out.println("All done");
    }
}