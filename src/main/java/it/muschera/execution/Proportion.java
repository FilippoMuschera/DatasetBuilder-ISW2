package it.muschera.execution;

import it.muschera.inforetriver.JiraInfoRetriever;
import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import it.muschera.util.ProjNameEnum;
import it.muschera.util.TicketUtil;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Proportion {

    private static double proportionColdStart = -1.0;

    private Proportion() {/*non istanziabile*/}

    public static Double computeProportionValue(List<JiraTicket> ticketList) {

        List<Double> prop = new ArrayList<>();

        for (JiraTicket ticket : ticketList) {

            double iv = ticket.getInjectedVersion().getIndex();
            double ov = ticket.getOpeningVersion().getIndex();
            double fv = ticket.getFixVersion().getIndex();


            //P = (FV-IV)/(FV-OV)
            Double p = (fv - iv) / (fv - ov);

            prop.add(p);

        }

        double finalP = 0.0;
        for (double p : prop)
            finalP += p;
        finalP = finalP / prop.size();

        return finalP;

    }

    public static double coldStart() {

        if (Proportion.proportionColdStart != -1.0) {
            //Se ho già calcolato il proportion cold start non lo ricalcolo e prendo direttamente il suo valore
            return Proportion.proportionColdStart;
        }

        List<Double> proportions = new ArrayList<>();

        JiraInfoRetriever jiraInfoRetriever = new JiraInfoRetriever();

        for (ProjNameEnum proj : ProjNameEnum.values()) {
            List<Release> coldStartRelease;
            List<JiraTicket> coldStartTickets;
            List<JiraTicket> coldStartConsistentTickets = new ArrayList<>();

            try {
                coldStartRelease = jiraInfoRetriever.getJiraVersions(proj.toString(), false);
                coldStartTickets = jiraInfoRetriever.getAllJiraTickets(coldStartRelease, proj.toString());
                for (JiraTicket ticket : coldStartTickets) {

                    if (TicketUtil.isConsistent(ticket)) {
                        //setta la IV, e controlla che non ci siano buchi nelle AV (se ci sono li riempe, non scarta il ticket)
                        JiraTicket goodTicket = TicketUtil.makeTicketAccurate(ticket, coldStartRelease);
                        coldStartConsistentTickets.add(goodTicket);
                    }
                }

                System.out.println("++++++++++++++DEBUG: (Proj, ConsistentTickets) = (" + proj + ", " + coldStartConsistentTickets.size() + ")");
                proportions.add(Proportion.computeProportionValue(coldStartConsistentTickets));


            } catch (IOException | GitAPIException | ParseException e) {
                e.printStackTrace();
            }


        }


        Collections.sort(proportions);

        Proportion.proportionColdStart = (proportions.get(proportions.size() / 2) + proportions.get((proportions.size() / 2) - 1)) / 2;
        System.out.println("++++++++++++++DEBUG: Proportion cold start = " + proportionColdStart);

        return Proportion.proportionColdStart;

    }


}
