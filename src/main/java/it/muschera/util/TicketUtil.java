package it.muschera.util;

import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class TicketUtil {

    private TicketUtil() {
        //solo metodi statici
    }

    public static boolean isConsistent(JiraTicket ticket) {

        boolean isConsistent = false;

        if (ticket.getAffectedVersions() == null || ticket.getAffectedVersions().isEmpty()){
            //Se il ticket non ha affected versions, non ci serve per il calcolo del proportion e lo scartiamo
            return false;
        }

        if (ticket.getOpeningVersion().getIndex() > ticket.getFixVersion().getIndex()) //ticket banalmente errato
            return false;

        for (Release affectedVersion : ticket.getAffectedVersions()) { //AV = 2,3,4,5
            if (affectedVersion.getIndex() >= ticket.getFixVersion().getIndex()){
                //Se la fix version risulta anche affected, il ticket non è consistente (Deve essere sempre AV < FV)
                //Altrimenti, se la fix version è anche affected, non è realmente "fixed".
                return false;
            }

            if (affectedVersion.getIndex() == ticket.getOpeningVersion().getIndex())
                isConsistent = true; //L'opening version deve essere contenuta tra le affected version per essere consistente
        }

        /*
         * Quindi, consideriamo un ticket consistente se:
         * 1) Ha delle affected version
         * 2) la fix version non è anche compresa tra le affected versions
         * 3) La opening version è compresa tra le affected version
         */

        return isConsistent;

    }

    public static JiraTicket makeTicketAccurate(JiraTicket ticket, List<Release> releaseList) {
        JiraTicket newTicket = new JiraTicket(ticket.getKey(), ticket.getOpeningVersion(), ticket.getFixVersion());
        //Per settare la IV prendo la prima delle AV. So che c'è perchè ho già fatto il controllo quando ho chiamato
        //TicketUtil.isConsistent(thisTicket) su questo stesso ticket
        newTicket.setInjectedVersion(ticket.getAffectedVersions().get(0));
        for (Release release : releaseList) {
            if (release.getIndex() >= newTicket.getInjectedVersion().getIndex() && release.getIndex() < newTicket.getFixVersion().getIndex()) {
                //Se IV <= actualVersion < FV
                newTicket.getAffectedVersions().add(release);
            }
        }

        return newTicket;
    }

    public static JiraTicket repairTicketWithProportion(JiraTicket brokenTicket, double p, List<Release> releaseList) {

        //Su questi ticket voglio chiamare makeTicketAccurate, ma quel metodo, per settare la IV, guarda la prima
        //delle AV, che sui brokenTicket non è presente. Perciò la aggiungo (tramite proportion) e poi passo il ticket
        //a makeTicketAccurate, in modo che aggiunga la IV e calcoli tutte le AV correttamente

        List<Release> affectedVersions = new ArrayList<>();
        int ov = brokenTicket.getOpeningVersion().getIndex();
        int fv = brokenTicket.getFixVersion().getIndex();
        int iv = (int) (fv - (fv - ov) * p); //TODO ARROTONDARE?

        iv = Math.max(1, iv); //Se con proportion la IV viene <1 la impostiamo a 1 per ovvi motivi

        for (Release release : releaseList) {
            if (release.getIndex() == iv) {
                affectedVersions.add(release);
                break;
            }
        }

        brokenTicket.setAffectedVersions(affectedVersions);
        /* Attualmente con openjpa qui da qualche problema, perchè a quanto pare alcuni ticket si ritorvano con
        * una affectedVersion list vuota (potrebbe essere un edge case in cui l'unica affacted version che ci sarebbe
        * è una di quelle che scartiamo perchè non ci sono commit associati).
        * Dovrebbe essere sufficiente scartare questi ticket perchè alla fine è come se avessere IV = FV */
        //TODO OPENJPA-2896 HA OV=FV=36=3.2.1 COME HA FATTO AD ARRIVARE FINO QUI LOL
        //SUI BROKEN TICKET NON è PRESENTE UN CONTROLLO DI CONSISTENZE, ECCO IL PROBLEMA

        return TicketUtil.makeTicketAccurate(brokenTicket, releaseList);


    }

    public static List<RevCommit> getCommitsOfTicket(JiraTicket ticket, List<Release> releaseList) {

        List<RevCommit> commitList = new ArrayList<>();
        List<RevCommit> commitsOfTicket = new ArrayList<>();
        for (Release rel : releaseList){
            commitList.addAll(rel.getReleaseCommits().getCommits());
        }

        //ora ho una lista completa di tutti i possibili commit

        for (RevCommit commit : commitList){
            String commitMessage = commit.getFullMessage();

            /*
             * Le condizioni dell'if sono fatte in questo modo perchè il problema è che se io ho il ticket "BOOKKEEPER-110" e usassi
             * semplicemente commitMessage.contains(ticket.getKey()) entrerei nell'if anche se il commit message facesse riferimento
             * al ticket "BOOKKEEPER-1101", perchè il contains sarebbe vero, ma in realtà il ticket a cui si fa riferimento nel commit
             * message non è quello che sto cercando. Per questo scriviamo le if-condition in questo modo.
             */
            if (commitMessage.contains(ticket.getKey() + ":") || commitMessage.contains(ticket.getKey() + "]") || commitMessage.contains(ticket.getKey() + " ") && (!commitsOfTicket.contains(commit))) {
                    commitsOfTicket.add(commit);


            }
        }

        return commitsOfTicket;
    }

    public static boolean isBrokenButConsistent(JiraTicket brokenTicket) {
        if (brokenTicket.getOpeningVersion().getIndex() > brokenTicket.getFixVersion().getIndex()) //ticket banalmente errato
            return false;
        if (brokenTicket.getOpeningVersion().getIndex() == brokenTicket.getFixVersion().getIndex())
            return false;
        return true;
    }
}
