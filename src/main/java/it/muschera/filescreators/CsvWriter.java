package it.muschera.filescreators;


import it.muschera.model.JavaClass;

import java.io.FileWriter;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.*;

public class CsvWriter {

    private CsvWriter() {
    }

    public static void writeCsv(String projName, EnumFileType type, List<JavaClass> javaClassesList, int iter) {
        String fileNameType = (type == EnumFileType.TRAINING) ? "training" : "testing";
        String csvNameStr = projName + "-" + fileNameType + "-" + iter;

        /*
         * Nel CSV di Bookkeeper non risulteranno esserci file relativi ad alcune versioni. Analizzando i commit in quelle versioni
         * si vede che riguardano soltanto file .html, .js, .xml ecc... e pertanto non sono stati considerati nella costruzione del
         * dataset, dato che vogliamo considerare solamente la buggyness dei file .java
         */

        try (
                FileWriter fileWriter = new FileWriter(csvNameStr + ".csv")

        ) {


            fileWriter.append("JAVA_CLASS,RELEASE,LOC,NR,NAUTH,LOC_ADDED,MAX_LOC_ADDED,AVG_LOC_ADDED,CHURN,MAX_CHURN,AVG_CHURN,HND_EXCEPT,NFIX,CYCL_COMPLEX,IS_BUGGY");
            fileWriter.append("\n");
            for (JavaClass javaClass : ArffWriter.getSplit(javaClassesList, type, iter)) {

                int lastSlashIndex = javaClass.getName().lastIndexOf("/");
                String name = javaClass.getName().substring(lastSlashIndex + 1);

                fileWriter.append(name);
                fileWriter.append(",");
                ArffWriter.writeFile(fileWriter, javaClass, type);


            }
            fileWriter.flush();

        } catch (Exception e) {
            out.println("Error in csv writer");
            Logger logger = Logger.getLogger(CsvWriter.class.getName());
            logger.info(e.getMessage());
        }
    }


}
