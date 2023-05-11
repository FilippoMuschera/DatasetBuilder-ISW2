package it.muschera.filescreators;

import it.muschera.weka.EvaluationSet;
import it.muschera.weka.WekaResultEntity;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import static java.lang.System.*;

public class ClassificatorReportWriter {


    private ClassificatorReportWriter(){
        //solo metodi statici
    }

    public static void writeReport(String projName) {

        String fileName = "ClassificationReport - " + projName.toUpperCase() + ".csv";

        try (
                FileWriter fileWriter = new FileWriter(fileName);
                )

        {
            fileWriter.append("Project,Iteration,Classifier,FeatureSelection,Sampling,CostSensitive,Precision,Recall,AUC,Kappa\n");


            for (WekaResultEntity result : EvaluationSet.getInstance().getEvaluationSetList()) {
                fileWriter.append(result.getProjName()).append(",");
                fileWriter.append(Integer.toString(result.getWalkForwardIterationIndex()));
                fileWriter.append(",");
                fileWriter.append(result.getClassifier());
                fileWriter.append(",");
                fileWriter.append(Boolean.toString(result.isFeatureSelection()));
                fileWriter.append(",");
                if (result.isSampling())
                    fileWriter.append(result.getBalancingType().toString());
                else
                    fileWriter.append("None");
                fileWriter.append(",");
                fileWriter.append(Boolean.toString(result.isCostSensitive()));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getPrecision()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getRecall()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getAuc())).replace(",", "."));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getKappa())).replace(",", "."));
                fileWriter.append("\n");

            }



        } catch (IOException e) {
            err.println("Impossible creare .csv report");
            e.printStackTrace();


        }


    }


}
