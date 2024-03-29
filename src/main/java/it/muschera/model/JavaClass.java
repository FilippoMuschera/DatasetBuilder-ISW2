package it.muschera.model;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.System.*;

public class JavaClass {
    /*
     * Questa classe rappresenta una classe Java del progetto di cui si stanno calcolando le features. Oltre al nome,
     * al suo contenuto e alla release a cui appartiene, avrà un attributo per ogni metrica che viene calcolata su di
     * essa (es. LOC, NAuth, ecc...).
     * L'idea è quella di avere un istanza di questo tipo per ogni classe di ogni release del progetto da analizzare,
     * calcolarne tutte le features, e poi "trasporre" ognuna di queste istanze in una riga del file .csv/.arff da usare
     * poi su WEKA
     */

    private String name;
    private String content;
    private Release release;
    private boolean isBuggyRealistic;
    private boolean isBuggyPrecise;
    private List<RevCommit> commitsInvolved = new ArrayList<>();

    private List<Integer> addedLinesList = new ArrayList<>();
    private List<Integer> deletedLinesList = new ArrayList<>();

    private RevCommit firstAppearance = null;
    private final List<String> processedTickets = new ArrayList<>(); //Tengo conto dei ticket per cui ho già aggiornato nfix


    private int linesOfCode = 0;
    private int linesOfCodeTouched = 0; //somma delle righe di codice aggiunte/rimosse all'interno della release
    private int nr = 0; //numero di commit che hanno toccato questa classe
    private int nAuth = 0; //numero di autori diversi che hanno toccato questa classe
    private int locAdded = 0; //numero di linee di codice aggiunte tramite tutti i commit della release (non dall'inizio del progetto)
    private int maxLocAdded = 0; //numero MASSIMO di righe di codice aggiunte in un singolo commit all'interno della release
    private double avgLocAdded = 0.0; //media delle righe di codice aggiunte ad ogni commit all'interno della release
    private int churn = 0; //misura di quante LOC il codice è cambiato rispetto alla release precedente, controllando ogni commit della release
    private int maxChurn = 0; //valore massimo di churn che si incontra all'interno della release
    private double avgChurn = 0.0; //churn medio all'intero della release
    private int handledExceptions = 0; //numero di eccezioni che la classe si trova a dover gestire all'interno del suo codie

    /*
     * NFix è il numero di bug fixati per questa classe. Viene incrementato in due momenti diversi. Quando si chiama il metodo ComputeFeatures.ComputeMetricNFix, che
     * guarda il numero di big fixati durante la release corrente (FV = OV). Poi quando si va a calcolare (in modo realistico) la buggyness della classe, si aumenta
     * di un NFix se la classe è identificata come buggy, perchè vuol dire che aveva un bug che è durato per più versioni, e quindi non era stato conteggiato in
     * ComputeMetricNFix, che va a cercare solo nei ticket con FV = OV.
     */
    private int nFix = 0;
    private int cyclComplexity = 1; //Complessità ciclomatica (numero di cammini indipendenti) della classe. Più è alta più è difficile da testare
    //e di conseguenza potrebbe essere più incline ad avere bug al suo interno


    public JavaClass(String name, String content, Release release) {
        this.name = name;
        this.content = content;
        this.release = release;


    }

    public void doLinesAddedAndDeleted() { //verrà chiamato nel momento in cui sarà necessario che le liste siano riempite
        try {
            computeAddedAndDeletedLinesList(this);
        } catch (IOException e) {
            err.println("Impossibile calcolare added/deleted lines per la classe");
            Logger logger = Logger.getLogger(JavaClass.class.getName());
            logger.info(e.getMessage());
        }
    }

    public void computeAddedAndDeletedLinesList(JavaClass javaClass) throws IOException {


        for (RevCommit comm : javaClass.getCommitsInvolved()) {
            try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

                RevCommit parentComm = comm.getParent(0); //commit precedente

                diffFormatter.setRepository(this.release.getRepository());
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

                List<DiffEntry> diffs = diffFormatter.scan(parentComm.getTree(), comm.getTree());
                for (DiffEntry entry : diffs) { //ogni DiffEntry rappresenta un cambiamento alla classe
                    if (entry.getNewPath().equals(javaClass.getName())) {
                        javaClass.getAddedLinesList().add(getAddedLines(diffFormatter, entry));
                        javaClass.getDeletedLinesList().add(getDeletedLines(diffFormatter, entry));

                    }

                }

            } catch (ArrayIndexOutOfBoundsException e) {
                //se il commit non ha un parent non posso calcolare le linee aggiunte/rimosse
                //di conseguenza la lista rimarrà vuota e le metriche relative rimarranno
                //inizializzate a zero, come da inizializzazione della classe

            }

        }


    }

    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int addedLines = 0;
        /*
         * Dal javadoc di Edit:
         * A modified region detected between two versions of roughly the same content.
           An edit covers the modified region only. It does not cover a common region.
           Regions should be specified using 0 based notation, so add 1 to the start and end marks for line numbers in a file.
           An edit where beginA == endA && beginB < endB is an insert edit, that is sequence B inserted the elements in region [beginB, endB) at beginA.
           An edit where beginA < endA && beginB == endB is a delete edit, that is sequence B has removed the elements between [beginA, endA).
           An edit where beginA < endA && beginB < endB is a replace edit, that is sequence B has replaced the range of elements between [beginA, endA) with those found in [beginB, endB).


         */


        for (Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            addedLines += edit.getLengthA();

        }

        return addedLines;
    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int deletedLines = 0;
        for (Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getLengthB();

        }
        return deletedLines;

    }

    //getter & setter

    public int getnFix() {
        return nFix;
    }

    public void setnFix(int nFix, String key) {
        if (!this.processedTickets.contains(key)) {
            this.nFix = nFix;
            this.processedTickets.add(key);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public void setLinesOfCode(int linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

    public List<RevCommit> getCommitsInvolved() {
        return commitsInvolved;
    }

    public void setCommitsInvolved(List<RevCommit> commitsInvolved) {
        this.commitsInvolved = commitsInvolved;
    }

    public int getLinesOfCodeTouched() {
        return linesOfCodeTouched;
    }

    public void setLinesOfCodeTouched(int linesOfCodeTouched) {
        this.linesOfCodeTouched = linesOfCodeTouched;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public int getnAuth() {
        return nAuth;
    }

    public void setnAuth(int nAuth) {
        this.nAuth = nAuth;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public void setMaxLocAdded(int maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(double avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public void setMaxChurn(int maxChurn) {
        this.maxChurn = maxChurn;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public void setAvgChurn(double avgChurn) {
        this.avgChurn = avgChurn;
    }


    public List<Integer> getAddedLinesList() {
        return addedLinesList;
    }

    public void setAddedLinesList(List<Integer> addedLinesList) {
        this.addedLinesList = addedLinesList;
    }

    public List<Integer> getDeletedLinesList() {
        return deletedLinesList;
    }

    public void setDeletedLinesList(List<Integer> deletedLinesList) {
        this.deletedLinesList = deletedLinesList;
    }

    public void setBuggyRealistic(boolean isBuggyRealistic) {
        this.isBuggyRealistic = isBuggyRealistic;
    }

    public String isBuggyRealisticString() {
        if (this.isBuggyRealistic)
            return "yes";
        else
            return "no";
    }

    public void setBuggyPrecise(boolean isBuggyPrecise) {
        this.isBuggyPrecise = isBuggyPrecise;
    }

    public String isBuggyPreciseString() {
        if (this.isBuggyPrecise)
            return "yes";
        else
            return "no";
    }

    public int getHandledExceptions() {
        return handledExceptions;
    }

    public void setHandledExceptions(int handledExceptions) {
        this.handledExceptions = handledExceptions;
    }

    public int getCyclComplexity() {
        return cyclComplexity;
    }

    public void setCyclComplexity(int cyclComplexity) {
        this.cyclComplexity = cyclComplexity;
    }

    public RevCommit getFirstAppearance() {
        return firstAppearance;
    }

    public void setFirstAppearance(RevCommit firstAppearance) {
        this.firstAppearance = firstAppearance;
    }
}

