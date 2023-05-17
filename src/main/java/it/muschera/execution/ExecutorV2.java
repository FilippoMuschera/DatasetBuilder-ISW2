package it.muschera.execution;

import it.muschera.filescreators.ArffWriter;
import it.muschera.filescreators.CsvWriter;
import it.muschera.filescreators.EnumFileType;
import it.muschera.inforetriver.JiraInfoRetriever;
import it.muschera.model.JavaClass;
import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import it.muschera.model.ReleaseCommits;
import it.muschera.util.JavaClassFinder;
import it.muschera.util.ReleaseFinder;
import it.muschera.util.TicketUtil;
import it.muschera.weka.WekaClassifier;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.System.*;


public class ExecutorV2 {

    private static final String ARFF = ".arff";
    private final String projName;
    /*
     * Parto da iteration = 3 e non iteration = 2 perchè la prima run con i = 2 sarebbe "superflua", dal momento che
     * avrei solamente una Release che dovrei predire senza avere training, e pertanto non potrei fare altro che predire tutti
     * "no". Quindi la skippiamo anche per migliorare leggermente la velocità di esecuzione dell'interno progetto.
     */
    private int iteration = 3;
    private JiraInfoRetriever jiraInfoRetriever;
    private List<Release> releaseList;
    private List<JavaClass> allJavaClasses;
    private List<JiraTicket> allTickets;
    private List<JiraTicket> consistentTickets;
    private double p;


    public ExecutorV2(String projName) {
        this.projName = projName;
    }

    public static void cleanEnvironment() throws IOException {
        File directory = new File(".");
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".csv") || file.getName().endsWith(ARFF))
                Files.delete(Path.of(file.getPath()));
        }
    }

    /*
     * Questo metodo viene eseguito solo la prima volta, ed è scollegato dal walk forward, perchè non fa altro che costruire la lista di JavaClasses e calcolarne
     * le metriche (LOC, Churn, ecc...) che dipendono solo dalla classe stessa e dalla versione associata eventualmente. Quindi costruiamo questo array
     * una volta sola, e poi ad ogni iterazione ne andremo a usare una porzione. Ogni javaClass poi verrà identificata come buggy, ad ogni terazione sia in modo
     * REALISTICO per il training, che PERCISO per il testing.
     */
    private void buildDatasetFirstTime() throws IOException, ParseException, GitAPIException {
        this.jiraInfoRetriever = new JiraInfoRetriever();
        try {
            this.releaseList = jiraInfoRetriever.getJiraVersions(this.projName.toUpperCase());
            this.releaseList = ReleaseFinder.cleanReleaseList(this.releaseList); //elimina release senza commit
            this.releaseList = ReleaseFinder.refactorReleaseList(this.releaseList); //riordina le release (necessario se prima qualcuna è stata eliminata perchè vuota)
        } catch (IOException | ParseException | GitAPIException e) {
            err.println("Errore nella lettura delle versioni da Jira");
            e.printStackTrace();
        }

        out.println("Versioni di " + projName + " caricate correttamente");


        this.allJavaClasses = new ArrayList<>();


        for (Release release : releaseList) {

            ReleaseCommits releaseCommits = release.getReleaseCommits();
            Map<String, String> javaClasses = releaseCommits.getJavaClasses();

            for (Map.Entry<String, String> singleClass : javaClasses.entrySet()) {
                JavaClass javaClass = new JavaClass(singleClass.getKey(), singleClass.getValue(), release);
                this.allJavaClasses.add(javaClass);
            }


        }


        JavaClassFinder.computeCommitsOfClass(this.allJavaClasses, this.releaseList, this.releaseList.get(0).getRepository());
        out.println("JavaClassList creata correttamente");

        ComputeFeatures.computeFeatures(this.allJavaClasses);


    }

    private void getAllTickets() {

        try {

            this.allTickets = this.jiraInfoRetriever.getAllJiraTickets(this.releaseList, this.projName);
        } catch (IOException | ParseException e) {
            err.println("Errore nella raccolta dei Ticket di jira per " + this.projName);
            e.printStackTrace();
        }


    }

    private void getAllConsistentTickets() {

        this.consistentTickets = new ArrayList<>();

        for (JiraTicket ticket : this.allTickets) {

            if (TicketUtil.isConsistent(ticket)) {
                //setta la IV, e controlla che non ci siano buchi nelle AV (se ci sono li riempe, non scarta il ticket)
                JiraTicket goodTicket = TicketUtil.makeTicketAccurate(ticket, this.releaseList);
                this.consistentTickets.add(goodTicket);
            }
        }


    }

    private void doProportion(List<JiraTicket> ticketList) {
        out.println("Number of consistent tickets for iteration " + this.iteration + " is " + ticketList.size());
        if (ticketList.size() >= 5) {
            this.p = Proportion.computeProportionValue(this.consistentTickets);
        } else {
            this.p = Proportion.coldStart();
        }
    }

    private void evaluateBuggynessRealistically() {

        List<JiraTicket> fixedTicketsRealistic = new ArrayList<>();

        for (JiraTicket consistentTicket : this.consistentTickets) {
            if (consistentTicket.getFixVersion().getIndex() < this.iteration && !fixedTicketsRealistic.contains(consistentTicket)) {
                fixedTicketsRealistic.add(consistentTicket);
            }
        }
        for (JiraTicket ticket : this.allTickets) {
            if (ticket.getFixVersion().getIndex() < this.iteration && !fixedTicketsRealistic.contains(ticket) && TicketUtil.isBrokenButConsistent(ticket)) {
                JiraTicket fixedTicket = TicketUtil.repairTicketWithProportion(ticket, this.p, this.releaseList.subList(0, this.iteration + 1));
                fixedTicketsRealistic.add(fixedTicket);
            }
        }


        /*
         * Ora che ho la lista "riparata" di tutti i ticket, itero su di essi. L'idea è quella, per ogni ticket, di vedere
         * i commit a lui associati, vedere quali classi vanno a toccare, e marcare le classi modificate come buggy per tutte
         * le Release comprese tra la IV e la FV
         */

        for (JiraTicket ticket : fixedTicketsRealistic) {
            List<RevCommit> associatedCommits = TicketUtil.getRealisticCommitsOfTickets(ticket, this.releaseList, this.iteration);

            for (RevCommit commit : associatedCommits) {

                Release release = ReleaseFinder.findByCommit(commit, this.releaseList);

                List<String> classesTouchedByCommit = JavaClassFinder.getModifiedClasses(commit, release.getRepository());

                for (String className : classesTouchedByCommit) {

                    JavaClassFinder.markAsBuggy(this.allJavaClasses, className, ticket, EnumFileType.TRAINING);

                }


            }

        }


    }

    private void evaluateBuggynessPrecisely() {
        List<JiraTicket> fixedTickets;
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


        fixedTickets = new ArrayList<>(this.consistentTickets); //Questi ticket sono già consistent, li aggiungo subito
        List<JiraTicket> allTicketsList = new ArrayList<>(this.allTickets);
        allTicketsList.removeAll(this.consistentTickets); //Qui ho solo i ticket da "aggiustare"

        for (JiraTicket brokenTicket : allTicketsList) {

            if (TicketUtil.isBrokenButConsistent(brokenTicket)) {
                JiraTicket fixedTicket = TicketUtil.repairTicketWithProportion(brokenTicket, this.p, this.releaseList);
                fixedTickets.add(fixedTicket);
            }
        }

        /*
         * Ora che ho la lista "riparata" di tutti i ticket, itero su di essi. L'idea è quella, per ogni ticket, di vedere
         * i commit a lui associati, vedere quali classi vanno a toccare, e marcare le classi modificate come buggy per tutte
         * le Release comprese tra la IV e la FV
         */

        for (JiraTicket ticket : fixedTickets) {
            List<RevCommit> associatedCommits = TicketUtil.getAllCommitsOfTicket(ticket, this.releaseList);

            for (RevCommit commit : associatedCommits) {

                Release release = ReleaseFinder.findByCommit(commit, this.releaseList);

                List<String> classesTouchedByCommit = JavaClassFinder.getModifiedClasses(commit, release.getRepository());

                for (String className : classesTouchedByCommit) {

                    JavaClassFinder.markAsBuggy(this.allJavaClasses, className, ticket, EnumFileType.TESTING);

                }


            }

        }


    }

    private void writeFiles(boolean isCsv) {
        if (isCsv) {
            CsvWriter.writeCsv(projName, EnumFileType.TRAINING, this.allJavaClasses, this.iteration);
            CsvWriter.writeCsv(projName, EnumFileType.TESTING, this.allJavaClasses, this.iteration);
        }


        ArffWriter.writeArff(projName, EnumFileType.TRAINING, this.allJavaClasses, this.iteration);
        ArffWriter.writeArff(projName, EnumFileType.TESTING, this.allJavaClasses, this.iteration);


    }

    public void runV2() throws Exception {
        out.println("Starting... Preparing to build the initial dataset structures");
        this.buildDatasetFirstTime();
        this.getAllTickets();
        this.getAllConsistentTickets();
        out.println("...Initial Dataset structures built successfully\nStarting Walk Forward iterations");

        boolean isThereNextIteration = true;

        //Ora iniziamo il walkForward
        while (isThereNextIteration) {
            //prendiamo solo i ticket che esistevano al momento della nostra iterazione
            List<JiraTicket> truncatedTicketList = new ArrayList<>();
            for (JiraTicket ticket : this.consistentTickets) {
                if (ticket.getOpeningVersion().getIndex() <= this.iteration) {
                    truncatedTicketList.add(ticket);
                }
            }

            this.doProportion(truncatedTicketList); //cold start o increment, dipende dal numero di ticket disponibili
            out.println("Iteration [" + this.iteration + "] has proportion = " + this.p);
            this.evaluateBuggynessRealistically();
            this.evaluateBuggynessPrecisely();
            ComputeFeatures.computeMetricNfix(this.allTickets, this.allJavaClasses, this.releaseList, this.iteration); //lo faccio qui perchè mi servono i ticket, e va fatto iter per iter
            out.println("Buggyness (both real and precise) correctly evaluated for this iteration");
            this.writeFiles(true);
            out.println("Output files produced for this iteration");
            String trainingSet = projName + "-" + "training" + "-" + this.iteration + ARFF;
            String testingSet = projName + "-" + "testing" + "-" + this.iteration + ARFF;

            WekaClassifier weka = new WekaClassifier(this.projName, this.iteration);
            weka.computeWekaMetrics(trainingSet, testingSet);
            out.println("Weka ML ran. Starting next iteration if present");
            if (this.iteration == (this.releaseList.size()) / 2)
                isThereNextIteration = false;
            else
                this.iteration++;
            out.println("******************************");

        }

    }


}
