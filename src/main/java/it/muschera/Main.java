package it.muschera;

import it.muschera.entities.BookkeeperEntity;
import it.muschera.entities.OpenJPAEntity;
import it.muschera.execution.ExecutorV2;
import it.muschera.filescreators.ClassificatorReportWriter;

import java.io.IOException;

import static java.lang.System.*;

public class Main {
    public static void main(String[] args) throws IOException {
        ExecutorV2.cleanEnvironment();
        execProject("bookkeeper");
        execProject("openjpa");
        ClassificatorReportWriter.writeReport();

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
            e.printStackTrace();
        }
        out.println("All done");
    }
}