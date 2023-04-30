package it.muschera.filescreators;

import it.muschera.model.JavaClass;

import java.io.FileWriter;
import java.util.List;

import static java.lang.System.*;

public class CsvWriter {

    private CsvWriter(){}

    public static void writeCsv(String projName, CsvEnumType type, List<JavaClass> javaClassesList){
        String fileNameType = (type == CsvEnumType.TRAINING) ? "training" : "testing";
        String csvNameStr = projName + "-" + fileNameType;

        /*
         * Nel CSV di Bookkeeper non risulteranno esserci file relativi ad alcune versioni. Analizzando i commit in quelle versioni
         * si vede che riguardano soltanto file .html, .js, .xml ecc... e pertanto non sono stati considerati nella costruzione del
         * dataset, dato che vogliamo considerare solamente la buggyness dei file .java
         */

        try (
                FileWriter fileWriter = new FileWriter(csvNameStr + ".csv")

        ) {

            //Name of CSV for output
            fileWriter.append("JAVA_CLASS,RELEASE,LOC,NR,NAUTH,LOC_ADDED,MAX_LOC_ADDED,AVG_LOC_ADDED,CHURN,MAX_CHURN,AVG_CHURN,IS_BUGGY");
            fileWriter.append("\n");
            for (JavaClass javaClass : javaClassesList) {

                int lastSlashIndex = javaClass.getName().lastIndexOf("/");
                String name = javaClass.getName().substring(lastSlashIndex + 1);

                fileWriter.append(name);
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getRelease().getId()));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getLinesOfCode()));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getNr()));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getnAuth()));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getLocAdded()));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getMaxLocAdded()));
                fileWriter.append(",");
                fileWriter.append(Double.toString(javaClass.getAvgLocAdded()));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getChurn()));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(javaClass.getMaxChurn()));
                fileWriter.append(",");
                fileWriter.append(Double.toString(javaClass.getAvgChurn()));
                fileWriter.append(",");
                fileWriter.append("?"); //TODO
                fileWriter.append("\n");


            }
            fileWriter.flush();

        } catch (Exception e) {
            out.println("Error in csv writer");
            e.printStackTrace();
        }
    }


}
