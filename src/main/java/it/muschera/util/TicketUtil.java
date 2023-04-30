package it.muschera.util;

import it.muschera.model.JiraTicket;
import it.muschera.model.Release;

public class TicketUtil {

    public static boolean isConsistent(JiraTicket ticket) {

        boolean isConsistent = false;

        if (ticket.getAffectedVersions() == null || ticket.getAffectedVersions().isEmpty()){
            //Se il ticket non ha affected versions, non ci serve per il calcolo del proportion e lo scartiamo
            return false;
        }

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

}
