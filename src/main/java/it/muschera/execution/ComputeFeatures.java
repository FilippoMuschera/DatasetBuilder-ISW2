package it.muschera.execution;

import it.muschera.model.JavaClass;
import org.eclipse.jgit.revwalk.RevCommit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ComputeFeatures {

    private ComputeFeatures() {} //solo metodi statici

    public static void computeFeatures(List<JavaClass> javaClassList) {

        for (JavaClass javaClass : javaClassList){
            ComputeFeatures.computeLOC(javaClass);
            ComputeFeatures.computeNR(javaClass);
            ComputeFeatures.computeNAuth(javaClass);
            ComputeFeatures.computeChurnAndLocTouched(javaClass);
        }


    }

    private static void computeLOC(JavaClass javaClass) {
        String[] lines = javaClass.getContent().split("\r\n|\r|\n");
        javaClass.setLinesOfCode(lines.length);
    }

    private static  void computeNR(JavaClass javaClass){
        /* Siccome il numero di commitsInvolved è calcolato su tutti i commits DELLA RELEASE, la metrica NR è calcolata sulla
         * singola release, e non su tutta la storia della repository.
         */
        javaClass.setNr(javaClass.getCommitsInvolved().size()); //il numero di commit che hanno toccato quella classe
    }

    private static void computeNAuth(JavaClass javaClass){
        List<String> classAuthors = new ArrayList<>();

        for(RevCommit commit : javaClass.getCommitsInvolved()) {
            if(!classAuthors.contains(commit.getAuthorIdent().getName())) {
                classAuthors.add(commit.getAuthorIdent().getName());
            }

        }
        javaClass.setnAuth(classAuthors.size());

    }

    private static void computeChurnAndLocTouched(JavaClass javaClass){
        int sumLOC = 0;
        int maxLOC = 0;
        double avgLOC = 0;
        int churn = 0;
        int maxChurn = 0;
        double avgChurn = 0;
        javaClass.doLinesAddedAndDeleted();

        for(int i=0; i<javaClass.getAddedLinesList().size(); i++) {

            int currentLOC = javaClass.getAddedLinesList().get(i);
            int currentDiff = Math.abs(javaClass.getAddedLinesList().get(i) - javaClass.getDeletedLinesList().get(i));

            sumLOC = sumLOC + currentLOC;
            churn = churn + currentDiff;

            if(currentLOC > maxLOC) {
                maxLOC = currentLOC;
            }
            if(currentDiff > maxChurn) {
                maxChurn = currentDiff;
            }

        }

        //If a class has 0 revisions, its AvgLocAdded and AvgChurn are 0 (see initialization above).
        if(!javaClass.getAddedLinesList().isEmpty()) {
            avgLOC = 1.0*sumLOC/javaClass.getAddedLinesList().size();
            DecimalFormat df = new DecimalFormat("#.##");
            avgLOC = Double.parseDouble((df.format(avgLOC)).replace(",", "."));
        }
        if(!javaClass.getAddedLinesList().isEmpty()) {
            avgChurn = 1.0*churn/javaClass.getAddedLinesList().size();
            DecimalFormat df = new DecimalFormat("#.##");
            avgChurn = Double.parseDouble((df.format(avgChurn)).replace(",", "."));
        }

        javaClass.setLocAdded(sumLOC);
        javaClass.setMaxLocAdded(maxLOC);
        javaClass.setAvgLocAdded(avgLOC);
        javaClass.setChurn(churn);
        javaClass.setMaxChurn(maxChurn);
        javaClass.setAvgChurn(avgChurn);
    }

}
