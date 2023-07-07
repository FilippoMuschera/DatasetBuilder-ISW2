package it.muschera.execution;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.TryStmt;
import it.muschera.model.JavaClass;
import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import it.muschera.util.CyclomaticComplexityCalculator;
import it.muschera.util.JavaClassFinder;
import it.muschera.util.ReleaseFinder;
import it.muschera.util.TicketUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.text.DecimalFormat;
import java.util.*;

public class ComputeFeatures {

    private ComputeFeatures() {
    } //solo metodi statici

    public static void computeFeatures(List<JavaClass> javaClassList) {

        for (JavaClass javaClass : javaClassList) {
            ComputeFeatures.computeLOC(javaClass);
            ComputeFeatures.computeNR(javaClass);
            ComputeFeatures.computeNAuth(javaClass);
            ComputeFeatures.computeChurnAndLocTouched(javaClass);
            ComputeFeatures.computeHandledExceptions(javaClass);
            ComputeFeatures.computeCyclComplexity(javaClass);
        }


    }


    private static void computeCyclComplexity(JavaClass javaClass) {
        javaClass.setCyclComplexity(CyclomaticComplexityCalculator.calculate(javaClass));
    }


    private static void computeHandledExceptions(JavaClass javaClass) {

        JavaParser jp = new JavaParser();
        ParseResult<CompilationUnit> parseResult = jp.parse(javaClass.getContent());


        int numHandledExceptions = 0;


        for (MethodDeclaration method : parseResult.getResult().orElseThrow().findAll(MethodDeclaration.class)) {
            for (TryStmt tryStmt : method.findAll(TryStmt.class)) {
                numHandledExceptions += tryStmt.getCatchClauses().size();
            }
        }


        javaClass.setHandledExceptions(numHandledExceptions);

    }

    private static void computeLOC(JavaClass javaClass) {
        String[] lines = javaClass.getContent().split("\r\n|\r|\n");
        javaClass.setLinesOfCode(lines.length);
    }

    private static void computeNR(JavaClass javaClass) {
        /* Siccome il numero di commitsInvolved è calcolato su tutti i commits DELLA RELEASE, la metrica NR è calcolata sulla
         * singola release, e non su tutta la storia della repository.
         */
        javaClass.setNr(javaClass.getCommitsInvolved().size()); //il numero di commit che hanno toccato quella classe
    }

    private static void computeNAuth(JavaClass javaClass) {
        List<String> classAuthors = new ArrayList<>();

        for (RevCommit commit : javaClass.getCommitsInvolved()) {
            if (!classAuthors.contains(commit.getAuthorIdent().getName())) {
                classAuthors.add(commit.getAuthorIdent().getName());
            }

        }
        javaClass.setnAuth(classAuthors.size());

    }

    private static void computeChurnAndLocTouched(JavaClass javaClass) {
        int sumLOC = 0;
        int maxLOC = 0;
        double avgLOC = 0;
        int churn = 0;
        int maxChurn = 0;
        double avgChurn = 0;
        javaClass.doLinesAddedAndDeleted();

        for (int i = 0; i < javaClass.getAddedLinesList().size(); i++) {

            int currentLOC = javaClass.getAddedLinesList().get(i);
            int currentDiff = Math.abs(javaClass.getAddedLinesList().get(i) - javaClass.getDeletedLinesList().get(i));

            sumLOC = sumLOC + currentLOC;
            churn = churn + currentDiff;

            if (currentLOC > maxLOC) {
                maxLOC = currentLOC;
            }
            if (currentDiff > maxChurn) {
                maxChurn = currentDiff;
            }

        }

        //If a class has 0 revisions, its AvgLocAdded and AvgChurn are 0 (see initialization above).
        if (!javaClass.getAddedLinesList().isEmpty()) {
            avgLOC = 1.0 * sumLOC / javaClass.getAddedLinesList().size();
            DecimalFormat df = new DecimalFormat("#.##");
            avgLOC = Double.parseDouble((df.format(avgLOC)).replace(",", "."));
        }
        if (!javaClass.getAddedLinesList().isEmpty()) {
            avgChurn = 1.0 * churn / javaClass.getAddedLinesList().size();
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

    public static void computeMetricNfix(List<JiraTicket> allTickets, List<JavaClass> javaClassList, List<Release> releaseList, int iteration) {

        List<JiraTicket> admissibleTickets = getTicketsForNFix(allTickets, iteration);


        for (JiraTicket ticket : admissibleTickets) {
            List<RevCommit> associatedCommits = TicketUtil.getRealisticCommitsOfTickets(ticket, releaseList, iteration);

            for (RevCommit commit : associatedCommits) {

                Release release = ReleaseFinder.findByCommit(commit, releaseList);

                List<String> classesTouchedByCommit = JavaClassFinder.getModifiedClasses(commit, release.getRepository());

                for (String className : classesTouchedByCommit) {

                    for (JavaClass javaClass : javaClassList) {
                        if (className.equals(javaClass.getName()))
                            javaClass.setnFix(javaClass.getnFix() + 1, ticket.getKey());
                    }

                }


            }

        }

    }

    private static List<JiraTicket> getTicketsForNFix(List<JiraTicket> allTickets, int iteration) {
        List<JiraTicket> admissibleTickets = new ArrayList<>();
        for (JiraTicket ticket : allTickets) {
            if (ticket.getFixVersion().getIndex() == (iteration - 1) && ticket.getOpeningVersion().getIndex() == ticket.getFixVersion().getIndex())
                admissibleTickets.add(ticket);
        }

        return admissibleTickets;
    }
}
