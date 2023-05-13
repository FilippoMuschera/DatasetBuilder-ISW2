package it.muschera.filescreators;

import it.muschera.weka.BalancingType;
import it.muschera.weka.EvaluationParams;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AvgWekaDataHolder {

    private static AvgWekaDataHolder instance = null;

    public List<EvalBundle> getSimpleNB() {
        return simpleNB;
    }

    public void setSimpleNB(List<EvalBundle> simpleNB) {
        this.simpleNB = simpleNB;
    }

    public List<EvalBundle> getSimpleIbk() {
        return simpleIbk;
    }

    public void setSimpleIbk(List<EvalBundle> simpleIbk) {
        this.simpleIbk = simpleIbk;
    }

    public List<EvalBundle> getSimpleRF() {
        return simpleRF;
    }

    public void setSimpleRF(List<EvalBundle> simpleRF) {
        this.simpleRF = simpleRF;
    }

    public List<EvalBundle> getSimpleMLP() {
        return simpleMLP;
    }

    public void setSimpleMLP(List<EvalBundle> simpleMLP) {
        this.simpleMLP = simpleMLP;
    }

    public List<EvalBundle> getFeatureSelNB() {
        return featureSelNB;
    }

    public void setFeatureSelNB(List<EvalBundle> featureSelNB) {
        this.featureSelNB = featureSelNB;
    }

    public List<EvalBundle> getFeatureSelIbk() {
        return featureSelIbk;
    }

    public void setFeatureSelIbk(List<EvalBundle> featureSelIbk) {
        this.featureSelIbk = featureSelIbk;
    }

    public List<EvalBundle> getFeatureSelRF() {
        return featureSelRF;
    }

    public void setFeatureSelRF(List<EvalBundle> featureSelRF) {
        this.featureSelRF = featureSelRF;
    }

    public List<EvalBundle> getFeatureSelMLP() {
        return featureSelMLP;
    }

    public void setFeatureSelMLP(List<EvalBundle> featureSelMLP) {
        this.featureSelMLP = featureSelMLP;
    }

    public List<EvalBundle> getFeatureSelUnderNB() {
        return featureSelUnderNB;
    }

    public void setFeatureSelUnderNB(List<EvalBundle> featureSelUnderNB) {
        this.featureSelUnderNB = featureSelUnderNB;
    }

    public List<EvalBundle> getFeatureSelUnderIbk() {
        return featureSelUnderIbk;
    }

    public void setFeatureSelUnderIbk(List<EvalBundle> featureSelUnderIbk) {
        this.featureSelUnderIbk = featureSelUnderIbk;
    }

    public List<EvalBundle> getFeatureSelUnderRF() {
        return featureSelUnderRF;
    }

    public void setFeatureSelUnderRF(List<EvalBundle> featureSelUnderRF) {
        this.featureSelUnderRF = featureSelUnderRF;
    }

    public List<EvalBundle> getFeatureSelUnderMLP() {
        return featureSelUnderMLP;
    }

    public void setFeatureSelUnderMLP(List<EvalBundle> featureSelUnderMLP) {
        this.featureSelUnderMLP = featureSelUnderMLP;
    }

    public List<EvalBundle> getFeatureSelOverNB() {
        return featureSelOverNB;
    }

    public void setFeatureSelOverNB(List<EvalBundle> featureSelOverNB) {
        this.featureSelOverNB = featureSelOverNB;
    }

    public List<EvalBundle> getFeatureSelOverIbk() {
        return featureSelOverIbk;
    }

    public void setFeatureSelOverIbk(List<EvalBundle> featureSelOverIbk) {
        this.featureSelOverIbk = featureSelOverIbk;
    }

    public List<EvalBundle> getFeatureSelOverRF() {
        return featureSelOverRF;
    }

    public void setFeatureSelOverRF(List<EvalBundle> featureSelOverRF) {
        this.featureSelOverRF = featureSelOverRF;
    }

    public List<EvalBundle> getFeatureSelOverMLP() {
        return featureSelOverMLP;
    }

    public void setFeatureSelOverMLP(List<EvalBundle> featureSelOverMLP) {
        this.featureSelOverMLP = featureSelOverMLP;
    }

    public List<EvalBundle> getFeatureSelSmoteNB() {
        return featureSelSmoteNB;
    }

    public void setFeatureSelSmoteNB(List<EvalBundle> featureSelSmoteNB) {
        this.featureSelSmoteNB = featureSelSmoteNB;
    }

    public List<EvalBundle> getFeatureSelSmoteIbk() {
        return featureSelSmoteIbk;
    }

    public void setFeatureSelSmoteIbk(List<EvalBundle> featureSelSmoteIbk) {
        this.featureSelSmoteIbk = featureSelSmoteIbk;
    }

    public List<EvalBundle> getFeatureSelSmoteRF() {
        return featureSelSmoteRF;
    }

    public void setFeatureSelSmoteRF(List<EvalBundle> featureSelSmoteRF) {
        this.featureSelSmoteRF = featureSelSmoteRF;
    }

    public List<EvalBundle> getFeatureSelSmoteMLP() {
        return featureSelSmoteMLP;
    }

    public void setFeatureSelSmoteMLP(List<EvalBundle> featureSelSmoteMLP) {
        this.featureSelSmoteMLP = featureSelSmoteMLP;
    }

    public List<EvalBundle> getSimpleUnderNB() {
        return simpleUnderNB;
    }

    public void setSimpleUnderNB(List<EvalBundle> simpleUnderNB) {
        this.simpleUnderNB = simpleUnderNB;
    }

    public List<EvalBundle> getSimpleUnderIbk() {
        return simpleUnderIbk;
    }

    public void setSimpleUnderIbk(List<EvalBundle> simpleUnderIbk) {
        this.simpleUnderIbk = simpleUnderIbk;
    }

    public List<EvalBundle> getSimpleUnderRF() {
        return simpleUnderRF;
    }

    public void setSimpleUnderRF(List<EvalBundle> simpleUnderRF) {
        this.simpleUnderRF = simpleUnderRF;
    }

    public List<EvalBundle> getSimpleUnderMLP() {
        return simpleUnderMLP;
    }

    public void setSimpleUnderMLP(List<EvalBundle> simpleUnderMLP) {
        this.simpleUnderMLP = simpleUnderMLP;
    }

    public List<EvalBundle> getCostSensNB() {
        return costSensNB;
    }

    public void setCostSensNB(List<EvalBundle> costSensNB) {
        this.costSensNB = costSensNB;
    }

    public List<EvalBundle> getCostSensIbk() {
        return costSensIbk;
    }

    public void setCostSensIbk(List<EvalBundle> costSensIbk) {
        this.costSensIbk = costSensIbk;
    }

    public List<EvalBundle> getCostSensRF() {
        return costSensRF;
    }

    public void setCostSensRF(List<EvalBundle> costSensRF) {
        this.costSensRF = costSensRF;
    }

    public List<EvalBundle> getCostSensMLP() {
        return costSensMLP;
    }

    public void setCostSensMLP(List<EvalBundle> costSensMLP) {
        this.costSensMLP = costSensMLP;
    }

    private List<EvalBundle> simpleNB = new ArrayList<>();

    private List<EvalBundle> simpleIbk = new ArrayList<>();
    private List<EvalBundle> simpleRF = new ArrayList<>();
    private List<EvalBundle> simpleMLP = new ArrayList<>();
    private List<EvalBundle> featureSelNB = new ArrayList<>();
    private List<EvalBundle> featureSelIbk = new ArrayList<>();
    private List<EvalBundle> featureSelRF = new ArrayList<>();
    private List<EvalBundle> featureSelMLP = new ArrayList<>();
    private List<EvalBundle> featureSelUnderNB = new ArrayList<>();
    private List<EvalBundle> featureSelUnderIbk = new ArrayList<>();
    private List<EvalBundle> featureSelUnderRF = new ArrayList<>();
    private List<EvalBundle> featureSelUnderMLP = new ArrayList<>();
    private List<EvalBundle> featureSelOverNB = new ArrayList<>();
    private List<EvalBundle> featureSelOverIbk = new ArrayList<>();
    private List<EvalBundle> featureSelOverRF = new ArrayList<>();
    private List<EvalBundle> featureSelOverMLP = new ArrayList<>();
    private List<EvalBundle> featureSelSmoteNB = new ArrayList<>();
    private List<EvalBundle> featureSelSmoteIbk = new ArrayList<>();
    private List<EvalBundle> featureSelSmoteRF = new ArrayList<>();
    private List<EvalBundle> featureSelSmoteMLP = new ArrayList<>();
    private List<EvalBundle> simpleUnderNB = new ArrayList<>();
    private List<EvalBundle> simpleUnderIbk = new ArrayList<>();
    private List<EvalBundle> simpleUnderRF = new ArrayList<>();
    private List<EvalBundle> simpleUnderMLP = new ArrayList<>();
    private List<EvalBundle> costSensNB = new ArrayList<>();
    private List<EvalBundle> costSensIbk = new ArrayList<>();
    private List<EvalBundle> costSensRF = new ArrayList<>();
    private List<EvalBundle> costSensMLP = new ArrayList<>();





    private AvgWekaDataHolder(){
        //Singleton
    }

    public static AvgWekaDataHolder getInstance() {
        if (instance == null)
            instance = new AvgWekaDataHolder();
        return instance;
    }

    public void fillAvgLists(String projName, String classifier, Evaluation evaluation, List<EvalBundle> correctList, List<Boolean> booleans, BalancingType type) {
        correctList.add(new EvalBundle(
                Arrays.asList(projName, classifier),
                Arrays.asList(evaluation.precision(0),
                evaluation.recall(0),
                evaluation.areaUnderROC(0),
                evaluation.kappa(),
                evaluation.numTruePositives(0),
                evaluation.numTrueNegatives(0),
                evaluation.numFalsePositives(0),
                evaluation.numFalseNegatives(0),
                evaluation.fMeasure(0)),
                booleans,
                type
        ));
        
    }

    public EvalBundle computeAvg(List<EvalBundle> list){
        double precision = 0;
        double recall = 0;
        double auc = 0;
        double kappa = 0;
        double tp = 0;
        double tn = 0;
        double fp = 0;
        double fn = 0;
        double f1 = 0;

        for (EvalBundle bundle : list) {
            precision += bundle.getPrecision();
            recall += bundle.getRecall();
            auc += bundle.getAuc();
            kappa += bundle.getKappa();
            tp += bundle.getTp();
            tn += bundle.getTn();
            fp += bundle.getFp();
            fn += bundle.getFn();
            f1 += bundle.getF1();
        }
        int size = list.size();
        return new EvalBundle(
                Arrays.asList(list.get(0).getProjName(),
                list.get(0).getClassifier()),
                Arrays.asList(precision/size,
                recall/size,
                auc/size,
                kappa/size,
                tp/size,
                tn/size,
                fp/size,
                fn/size,
                f1/size),
                list.get(0).getBools(),
                list.get(0).getType()

        );
    }

    public List<EvalBundle> getPrintableList() {
        List<EvalBundle>  returnList = new ArrayList<>();


        returnList.add(this.computeAvg(simpleNB));
        returnList.add(this.computeAvg(simpleIbk));
        returnList.add(this.computeAvg(simpleRF));
        returnList.add(this.computeAvg(simpleMLP));
        returnList.add(this.computeAvg(featureSelNB));
        returnList.add(this.computeAvg(featureSelIbk));
        returnList.add(this.computeAvg(featureSelRF));
        returnList.add(this.computeAvg(featureSelMLP));
        returnList.add(this.computeAvg(featureSelUnderNB));
        returnList.add(this.computeAvg(featureSelUnderIbk));
        returnList.add(this.computeAvg(featureSelUnderRF));
        returnList.add(this.computeAvg(featureSelUnderMLP));
        returnList.add(this.computeAvg(featureSelOverNB));
        returnList.add(this.computeAvg(featureSelOverIbk));
        returnList.add(this.computeAvg(featureSelOverRF));
        returnList.add(this.computeAvg(featureSelOverMLP));
        returnList.add(this.computeAvg(featureSelSmoteNB));
        returnList.add(this.computeAvg(featureSelSmoteIbk));
        returnList.add(this.computeAvg(featureSelSmoteRF));
        returnList.add(this.computeAvg(featureSelSmoteMLP));
        returnList.add(this.computeAvg(simpleUnderNB));
        returnList.add(this.computeAvg(simpleUnderIbk));
        returnList.add(this.computeAvg(simpleUnderRF));
        returnList.add(this.computeAvg(simpleUnderMLP));
        returnList.add(this.computeAvg(costSensNB));
        returnList.add(this.computeAvg(costSensIbk));
        returnList.add(this.computeAvg(costSensRF));
        returnList.add(this.computeAvg(costSensMLP));

        return returnList;


    }








}
