package it.muschera.filescreators;

import it.muschera.weka.BalancingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Double.NaN;

public class EvalBundle {

    private final String classifier;
    private final String projName;
    private final double precision;
    private final double recall;
    private final double auc;
    private final double kappa;
    private final double tp;
    private final double tn;
    private final double fp;

    private final double f1;


    private final double fn;

    private final boolean fs;
    private final boolean balancing;
    private final boolean costSens;
    private final BalancingType type;

    public EvalBundle(List<String> names, List<Double> values, List<Boolean> booleans, BalancingType type) {
        values = this.checkForNaN(values);
        this.projName = names.get(0);
        this.classifier = names.get(1);
        this.precision = values.get(0);
        this.recall = values.get(1);
        this.auc = values.get(2);
        this.kappa = values.get(3);
        this.tp = values.get(4);
        this.tn = values.get(5);
        this.fp = values.get(6);
        this.fn = values.get(7);
        this.f1 = values.get(8);
        this.fs = booleans.get(0);
        this.balancing = booleans.get(1);
        this.costSens = booleans.get(2);
        this.type = type;

    }

    private List<Double> checkForNaN(List<Double> values) {
        List<Double> adjustedList = new ArrayList<>();
        for (double d : values) {
            if (Double.isNaN(d))
                adjustedList.add(0.0);
            else
                adjustedList.add(d);

        }

        return adjustedList;
    }

    public boolean isFs() {
        return fs;
    }

    public boolean isBalancing() {
        return balancing;
    }

    public boolean isCostSens() {
        return costSens;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getProjName() {
        return projName;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getAuc() {
        return auc;
    }

    public double getKappa() {
        return kappa;
    }

    public double getTp() {
        return tp;
    }

    public double getTn() {
        return tn;
    }

    public double getFp() {
        return fp;
    }

    public double getFn() {
        return fn;
    }

    public List<Boolean> getBools() {
        return Arrays.asList(fs, balancing, costSens);
    }

    public BalancingType getType() {
        return this.type;
    }

    public double getF1() {
        return this.f1;
    }
}
