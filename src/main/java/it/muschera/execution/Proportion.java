package it.muschera.execution;

import it.muschera.model.JiraTicket;

import java.util.ArrayList;
import java.util.List;

public class Proportion {

    private Proportion() {/*non istanziabile*/}
    public static Double computeProportionValue(List<JiraTicket> ticketList) {

        List<Double> prop = new ArrayList<>();

        for (JiraTicket ticket : ticketList){

            double iv = ticket.getInjectedVersion().getIndex();
            double ov = ticket.getOpeningVersion().getIndex();
            double fv = ticket.getFixVersion().getIndex();


            //P = (FV-IV)/(FV-OV)
            Double p = (fv - iv)/(fv - ov);

            prop.add(p);

        }

        double finalP = 0.0;
        for (double p : prop)
            finalP += p;
        finalP = finalP/prop.size();

        return finalP;

    }

}
