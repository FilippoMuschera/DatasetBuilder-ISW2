package it.muschera.filescreators;

import it.muschera.model.JavaClass;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.*;

public class ArffWriter {

    private ArffWriter() {
        //solo metodi statici
    }

    public static void writeArff(String projName, EnumFileType type, List<JavaClass> javaClassesList, int iter) {
        String fileNameType = (type == EnumFileType.TRAINING) ? "training" : "testing";
        String arffFileName = projName + "-" + fileNameType + "-" + iter;


        /*
         * Nel CSV di Bookkeeper non risulteranno esserci file relativi ad alcune versioni. Analizzando i commit in quelle versioni
         * si vede che riguardano soltanto file .html, .js, .xml ecc... e pertanto non sono stati considerati nella costruzione del
         * dataset, dato che vogliamo considerare solamente la buggyness dei file .java
         */

        try (
                FileWriter fileWriter = new FileWriter(arffFileName + ".arff")

        ) {

            fileWriter.append("@relation ").append(projName).append("-").append(fileNameType);


            fileWriter.write("\n@attribute LOC numeric\n");
            fileWriter.write("@attribute NR numeric\n");
            fileWriter.write("@attribute N_AUTH numeric\n");
            fileWriter.write("@attribute LOC_ADDED numeric\n");
            fileWriter.write("@attribute MAX_LOC_ADDED numeric\n");
            fileWriter.write("@attribute AVG_LOC_ADDED numeric\n");
            fileWriter.write("@attribute CHURN numeric\n");
            fileWriter.write("@attribute MAX_CHURN numeric\n");
            fileWriter.write("@attribute AVG_CHURN numeric\n");
            fileWriter.write("@attribute HND_EXCEPT numeric\n");
            fileWriter.write("@attribute NFIX numeric\n");
            fileWriter.write("@attribute CYCL_COMPLEX numeric\n");
            fileWriter.write("@attribute IS_BUGGY {'yes', 'no'}\n");
            fileWriter.write("@data\n");
            for (JavaClass javaClass : ArffWriter.getSplit(javaClassesList, type, iter)) {


                writeFile(fileWriter, javaClass, type);


            }
            fileWriter.flush();

        } catch (Exception e) {
            out.println("Error in arff writer");
            Logger logger = Logger.getLogger(ArffWriter.class.getName());
            logger.info(e.getMessage());
        }
    }

    public static void writeFile(FileWriter fileWriter, JavaClass javaClass, EnumFileType type) throws IOException {

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
        fileWriter.append(Integer.toString(javaClass.getHandledExceptions()));
        fileWriter.append(",");
        fileWriter.append(Long.toString(javaClass.getnFix()));
        fileWriter.append(",");
        fileWriter.append(Integer.toString(javaClass.getCyclComplexity()));
        fileWriter.append(",");
        if (type.equals(EnumFileType.TRAINING))
            fileWriter.append(javaClass.isBuggyRealisticString());
        else if (type.equals(EnumFileType.TESTING)) {
            fileWriter.append(javaClass.isBuggyPreciseString());
        }
        fileWriter.append("\n");
    }

    public static List<JavaClass> getSplit(List<JavaClass> javaClassesList, EnumFileType type, int iter) {

        List<JavaClass> returnList = new ArrayList<>();
        if (type == EnumFileType.TRAINING) {
            for (JavaClass javaClass : javaClassesList) {
                if (javaClass.getRelease().getIndex() < iter) {
                    returnList.add(javaClass);
                }
            }
        } else if (type == EnumFileType.TESTING) {
            for (JavaClass javaClass : javaClassesList) {
                if (javaClass.getRelease().getIndex() == iter) {
                    returnList.add(javaClass);
                }
            }
        }

        return returnList;


    }

}
