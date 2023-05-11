package it.muschera.weka;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class EvaluationParams {

    private Instances testing;
    private Instances training;
    private RandomForest randomForest;
    private NaiveBayes naiveBayes;
    private IBk iBk1;
    private IBk iBkN;
    private boolean fs;
    private boolean balancing;

    public BalancingType getBalancingType() {
        return balancingType;
    }

    public void setBalancingType(BalancingType balancingType) {
        this.balancingType = balancingType;
    }

    private BalancingType balancingType;
    private boolean costSens;

    public Instances getTesting() {
        return testing;
    }

    public void setTesting(Instances testing) {
        this.testing = testing;
    }

    public Instances getTraining() {
        return training;
    }

    public void setTraining(Instances training) {
        this.training = training;
    }

    public RandomForest getRandomForest() {
        return randomForest;
    }

    public void setRandomForest(RandomForest randomForest) {
        this.randomForest = randomForest;
    }

    public NaiveBayes getNaiveBayes() {
        return naiveBayes;
    }

    public void setNaiveBayes(NaiveBayes naiveBayes) {
        this.naiveBayes = naiveBayes;
    }

    public IBk getiBk1() {
        return iBk1;
    }

    public void setiBk1(IBk iBk1) {
        this.iBk1 = iBk1;
    }

    public IBk getiBkN() {
        return iBkN;
    }

    public void setiBkN(IBk iBkN) {
        this.iBkN = iBkN;
    }

    public boolean isFs() {
        return fs;
    }

    public void setFs(boolean fs) {
        this.fs = fs;
    }

    public boolean isBalancing() {
        return balancing;
    }

    public void setBalancing(boolean balancing) {
        this.balancing = balancing;
    }

    public boolean isCostSens() {
        return costSens;
    }

    public void setCostSens(boolean costSens) {
        this.costSens = costSens;
    }
}



