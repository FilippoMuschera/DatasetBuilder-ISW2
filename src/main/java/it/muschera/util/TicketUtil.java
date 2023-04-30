package it.muschera.util;

import it.muschera.model.JiraTicket;
import it.muschera.model.Release;

import java.util.List;

public class TicketUtil {

    public static boolean isConsistent(JiraTicket ticket) {

        boolean isConsistent = false;

        if (ticket.getAffectedVersions() == null || ticket.getAffectedVersions().isEmpty()){
            //Se il ticket non ha affected versions, non ci serve per il calcolo del proportion e lo scartiamo
            return false;
        }

        if (ticket.getOpeningVersion().getIndex() > ticket.getFixVersion().getIndex()) //ticket banalmente errato
            return false;

        for (Release affectedVersion : ticket.getAffectedVersions()) {
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
}
