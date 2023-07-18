package it.muschera.filescreators;


import it.muschera.weka.EvaluationSet;
import it.muschera.weka.WekaResultEntity;


import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import static java.lang.System.*;

public class ClassificatorReportWriter {


    private ClassificatorReportWriter() {
        //solo metodi statici
    }

    public static void writeReport() {

        String fileName = "ClassificationReport-Weka.csv";

        try (
                FileWriter fileWriter = new FileWriter(fileName)
        ) {
            fileWriter.append("Project;Iteration;Classifier;FeatureSelection;Sampling;CostSensitive;Precision;Recall;FScore;AUC;Kappa;TP;FN;FP;TN\n");


            for (WekaResultEntity result : EvaluationSet.getInstance().getEvaluationSetList()) {
                fileWriter.append(result.getProjName()).append(";");
                fileWriter.append(Integer.toString(result.getWalkForwardIterationIndex()));
                fileWriter.append(";");
                fileWriter.append(result.getClassifier());
                fileWriter.append(";");
                fileWriter.append(Boolean.toString(result.isFeatureSelection()));
                fileWriter.append(";");
                if (result.isSampling())
                    fileWriter.append(result.getBalancingType().toString());
                else
                    fileWriter.append("None");
                fileWriter.append(";");
                fileWriter.append(Boolean.toString(result.isCostSensitive()));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getPrecision())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getRecall())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getFscore())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getAuc())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getKappa())));
                fileWriter.append(";");
                fileWriter.append(Double.toString(result.getTp()));
                fileWriter.append(";");
                fileWriter.append(Double.toString(result.getFn()));
                fileWriter.append(";");
                fileWriter.append(Double.toString(result.getFp()));
                fileWriter.append(";");
                fileWriter.append(Double.toString(result.getTn()));
                fileWriter.append("\n");


            }


        } catch (IOException e) {
            err.println("Impossible creare .csv report");
            Logger logger = Logger.getLogger(ClassificatorReportWriter.class.getName());
            logger.info(e.getMessage());


        }


    }

    public static void avgReportWriter(String projName) {
        String fileName = "ClassificationReport-Weka-AVG-" + projName +".csv";

        try (
                FileWriter fileWriter = new FileWriter(fileName)
        ) {
            fileWriter.append("Project;Classifier;FeatureSelection;Sampling;CostSensitive;Precision;Recall;FScore;AUC;Kappa;TP;FN;FP;TN\n");


            for (EvalBundle result : AvgWekaDataHolder.getInstance().getPrintableList()) {
                fileWriter.append(result.getProjName()).append(";");

                fileWriter.append(result.getClassifier());
                fileWriter.append(";");
                fileWriter.append(Boolean.toString(result.isFs()));
                fileWriter.append(";");
                if (result.isBalancing())
                    fileWriter.append(result.getType().toString());
                else
                    fileWriter.append("None");
                fileWriter.append(";");
                fileWriter.append(Boolean.toString(result.isCostSens()));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getPrecision())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getRecall())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getF1())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getAuc())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getKappa())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getTp())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getFn())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getFp())));
                fileWriter.append(";");
                fileWriter.append((new DecimalFormat("#.##").format(result.getTn())));
                fileWriter.append("\n");


            }


        } catch (IOException e) {
            err.println("Impossible creare .csv report");
            Logger logger = Logger.getLogger(ClassificatorReportWriter.class.getName());
            logger.info(e.getMessage());


        }

    }


    public static void cleanAvg() {
        AvgWekaDataHolder.cleanInstance(); //ripulisce le medie dai valori del precedente progetto
    }
}
