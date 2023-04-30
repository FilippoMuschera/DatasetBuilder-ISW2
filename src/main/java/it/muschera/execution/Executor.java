package it.muschera.execution;

import it.muschera.filescreators.CsvEnumType;
import it.muschera.filescreators.CsvWriter;
import it.muschera.inforetriver.GitInfoRetriever;
import it.muschera.inforetriver.JiraInfoRetriever;
import it.muschera.model.JavaClass;
import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import it.muschera.model.ReleaseCommits;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

public class Executor {

    private final String projName;
    private JiraInfoRetriever jiraInfoRetriever;
    private List<Release> releaseList;
    private List<JavaClass> javaClassList;
    private List<JiraTicket> allTickets;

    private List<JiraTicket> consistentTickets;



    public Executor(String projName){
        this.projName = projName;
    }

    public void buildDataset() {

        this.jiraInfoRetriever = new JiraInfoRetriever();
        try {
            this.releaseList = jiraInfoRetriever.getJiraVersions(this.projName.toUpperCase(), true);
        } catch (IOException | ParseException | GitAPIException e) {
            err.println("Errore nella lettura delle versioni da Jira");
            e.printStackTrace();
        }


        this.javaClassList = new ArrayList<>();

        for (Release release : releaseList) {
            ReleaseCommits releaseCommits = release.getReleaseCommits();
            Map<String, String> javaClasses = releaseCommits.getJavaClasses();

            for (Map.Entry<String, String> singleClass : javaClasses.entrySet()) {
                JavaClass javaClass = new JavaClass(singleClass.getKey(), singleClass.getValue(), release);
                GitInfoRetriever gitInfoRetriever = new GitInfoRetriever(release.getRepository());
                gitInfoRetriever.computeInvolvedCommits(javaClass); //metodo lento ma facile, per ora ok, se ho tempo si può migliorare
                this.javaClassList.add(javaClass);
            }

        }

        ComputeFeatures.computeFeatures(this.javaClassList);


    }

    public void evaluateBuggyness(){
        //TODO
    }


    public void writeCsv() {
        CsvWriter.writeCsv(projName, CsvEnumType.TRAINING, javaClassList);
    }

    public void getTickets() {

        try {
            this.allTickets = this.jiraInfoRetriever.getAllJiraTickets(this.releaseList, this.projName);
        } catch (IOException | ParseException e) {
            err.println("Errore nella raccolta dei Ticket di jira per " + this.projName);
            e.printStackTrace();
        }

        //Debug purposes
        for (JiraTicket t : allTickets) {
            out.println(t.getKey());
        }
        out.println("TICKET LIST SIZE =  " + allTickets.size());

    }


    public void getConsistentTickets() {
        for (JiraTicket ticket : this.allTickets) {
            if ()
        }
    }
}
