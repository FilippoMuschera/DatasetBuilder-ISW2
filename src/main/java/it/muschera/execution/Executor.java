package it.muschera.execution;

import it.muschera.filescreators.CsvEnumType;
import it.muschera.filescreators.CsvWriter;
import it.muschera.inforetriver.GitInfoRetriever;
import it.muschera.inforetriver.JiraInfoRetriever;
import it.muschera.model.JavaClass;
import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import it.muschera.model.ReleaseCommits;
import it.muschera.util.JavaClassFinder;
import it.muschera.util.ReleaseFinder;
import it.muschera.util.TicketUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

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
    private List<JiraTicket> fixedTickets;

    private double p;



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
        /*
         * Questo metodo (e i metodi che chiama) hanno l'obiettivo di calcolare, per ogni classe java già costruita, presente in
         * javaClassList, l'attributo "isBuggy", per vedere se quella classe, a quella release, era buggy oppure no.
         *
         *Per farlo dobbiamo prendere la lista di tutti i ticket (anche quelli senza AV), e inserire manualmente le AV e la IV, usando
         * il metodo di Proportion, dove P è già stato calcolato usando i ticket che avevano a disposizione la OV, la FV e le AV.
         *
         * Una volta resi tutti i ticket utilizzabili, iteriamo su di essi, per andare ad analizzare a quali commit si riferiscono,
         * e quali classi sono state toccate da quei commit, andando poi ad aggiornare la loro buggyness.
         */

        //Qui sappiamo che P è stato già inizializzato, perché questa funzione nel flusso di esecuzione è chiamata dopo "doProportion"

        List<JiraTicket> brokenTickets = this.allTickets;
        this.fixedTickets = new ArrayList<>();
        this.fixedTickets.addAll(this.consistentTickets); //Questi ticket sono già consistent, li aggiungo subito
        brokenTickets.removeAll(this.consistentTickets); //Qui ho solo i ticket da "aggiustare"

        for (JiraTicket brokenTicket : brokenTickets) {
            JiraTicket fixedTicket = TicketUtil.repairTicketWithProportion(brokenTicket, this.p, this.releaseList);
            this.fixedTickets.add(fixedTicket);
        }

        /*
         * Ora che ho la lista "riparata" di tutti i ticket, itero su di essi. L'idea è quella, per ogni ticket, di vedere
         * i commit a lui associati, vedere quali classi vanno a toccare,e marcare le classi modificate come buggy per tutte
         * le Release comprese tra la IV e la FV
         */

        for (JiraTicket ticket : this.fixedTickets) {
            List<RevCommit> associatedCommits = TicketUtil.getCommitsOfTicket(ticket, this.releaseList);

            for (RevCommit commit : associatedCommits) {

                Release release = ReleaseFinder.findByCommit(commit, this.releaseList);

                List<String> classesTouchedByCommit = JavaClassFinder.getModifiedClasses(commit, release.getRepository());

                for (String className : classesTouchedByCommit) {

                    JavaClassFinder.markAsBuggy(this.javaClassList, className, ticket, release);

                }


            }

        }


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

        //temporary, for debug
        out.println("TICKET LIST SIZE =  " + allTickets.size());

    }


    public void getConsistentTickets() {

        this.consistentTickets = new ArrayList<>();

        for (JiraTicket ticket : this.allTickets) {
            if (TicketUtil.isConsistent(ticket)){
                //setta la IV, e controlla che non ci siano buchi nelle AV (se ci sono li riempe, non scarta il ticket)
                JiraTicket goodTicket = TicketUtil.makeTicketAccurate(ticket, this.releaseList);
                this.consistentTickets.add(goodTicket);
            }
        }

        //temporary, For debug
        out.println("TOTAL NUMBER OF CONSISTENT TICKET IS " + this.consistentTickets.size());

    }

    public void doProportion(){
        if (this.consistentTickets.size() >= 5) {
            this.p = Proportion.computeProportionValue(this.consistentTickets);
        }
    }
}
