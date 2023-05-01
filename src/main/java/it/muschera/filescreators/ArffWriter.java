package it.muschera.filescreators;

import it.muschera.model.JavaClass;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static java.lang.System.*;

public class ArffWriter {

    private ArffWriter(){
        //solo metodi statici
    }
    public static void writeArff(String projName, EnumFileType type, List<JavaClass> javaClassesList){
        String fileNameType = (type == EnumFileType.TRAINING) ? "training" : "testing";
        String csvNameStr = projName + "-" + fileNameType;


        /*
         * Nel CSV di Bookkeeper non risulteranno esserci file relativi ad alcune versioni. Analizzando i commit in quelle versioni
         * si vede che riguardano soltanto file .html, .js, .xml ecc... e pertanto non sono stati considerati nella costruzione del
         * dataset, dato che vogliamo considerare solamente la buggyness dei file .java
         */

        try (
                FileWriter fileWriter = new FileWriter(csvNameStr + ".arff")

        ) {

            //Name of ARFF dataset
            fileWriter.append("@relation ").append(projName).append("-").append(fileNameType);

            fileWriter.write("\n@attribute RELEASE numeric\n");
            fileWriter.write("@attribute LOC numeric\n");
            fileWriter.write("@attribute NR numeric\n");
            fileWriter.write("@attribute N_AUTH numeric\n");
            fileWriter.write("@attribute LOC_ADDED numeric\n");
            fileWriter.write("@attribute MAX_LOC_ADDED numeric\n");
            fileWriter.write("@attribute AVG_LOC_ADDED numeric\n");
            fileWriter.write("@attribute CHURN numeric\n");
            fileWriter.write("@attribute MAX_CHURN numeric\n");
            fileWriter.write("@attribute AVG_CHURN numeric\n");
            fileWriter.write("@attribute IS_BUGGY {'yes', 'no'}\n");
            fileWriter.write("@data\n");
            for (JavaClass javaClass : ArffWriter.getSplit(javaClassesList, type)) {


                writeFile(fileWriter, javaClass);


            }
            fileWriter.flush();

        } catch (Exception e) {
            out.println("Error in arff writer");
            e.printStackTrace();
        }
    }

    public static void writeFile(FileWriter fileWriter, JavaClass javaClass) throws IOException {
        fileWriter.append(Integer.toString(javaClass.getRelease().getIndex()));
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
        fileWriter.append(javaClass.isBuggyString());
        fileWriter.append("\n");
    }

    public static List<JavaClass> getSplit(List<JavaClass> javaClassesList, EnumFileType type) {
        int split = (int) (javaClassesList.size() * 0.66);
        List<JavaClass> splitListForFile;

        if (type.equals(EnumFileType.TRAINING)) {

            splitListForFile = javaClassesList.subList(0, split);
        }
        else {
            splitListForFile = javaClassesList.subList(split, javaClassesList.size());

        }

        return splitListForFile;
    }

}
