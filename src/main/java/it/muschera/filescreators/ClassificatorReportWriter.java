package it.muschera.filescreators;

import it.muschera.weka.EvaluationSet;
import it.muschera.weka.WekaResultEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

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
            fileWriter.append("Project,Iteration,Classifier,FeatureSelection,Sampling,CostSensitive,Precision,Recall,AUC,Kappa,TP,FN,FP,TN\n");


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
                fileWriter.append(",");
                fileWriter.append(Double.toString(result.getTp()));
                fileWriter.append(",");
                fileWriter.append(Double.toString(result.getFn()));
                fileWriter.append(",");
                fileWriter.append(Double.toString(result.getFp()));
                fileWriter.append(",");
                fileWriter.append(Double.toString(result.getTn()));
                fileWriter.append("\n");


            }


        } catch (IOException e) {
            err.println("Impossible creare .csv report");
            e.printStackTrace();


        }


    }

    public static void avgReportWriter() { //TODO Alcune metriche a volte escono NaN, su una media non va bene imho
        String fileName = "ClassificationReport-Weka-AVG.csv";

        try (
                FileWriter fileWriter = new FileWriter(fileName)
        ) {
            fileWriter.append("Project,Classifier,FeatureSelection,Sampling,CostSensitive,Precision,Recall,FScore,AUC,Kappa,TP,FN,FP,TN\n");




            for (EvalBundle result : AvgWekaDataHolder.getInstance().getPrintableList()) {
                fileWriter.append(result.getProjName()).append(",");

                fileWriter.append(result.getClassifier());
                fileWriter.append(",");
                fileWriter.append(Boolean.toString(result.isFs()));
                fileWriter.append(",");
                if (result.isBalancing())
                    fileWriter.append(result.getType().toString());
                else
                    fileWriter.append("None");
                fileWriter.append(",");
                fileWriter.append(Boolean.toString(result.isCostSens()));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getPrecision()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getRecall()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getF1()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getAuc())).replace(",", "."));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getKappa())).replace(",", "."));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getTp()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getFn()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getFp()).replace(",", ".")));
                fileWriter.append(",");
                fileWriter.append((new DecimalFormat("#.##").format(result.getTn()).replace(",", ".")));
                fileWriter.append("\n");


            }


        } catch (IOException e) {
            err.println("Impossible creare .csv report");
            e.printStackTrace();


        }

    }


}
