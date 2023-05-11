package it.muschera.weka;

public class WekaResultEntity {


    private String projName;
    private int walkForwardIterationIndex;
    private String classifier;
    private boolean featureSelection;
    private boolean sampling;
    private boolean costSensitive;
    private double precision;
    private double recall;
    private double auc;
    private double kappa;
    private BalancingType balancingType;


    public String getProjName() {
        return projName;
    }

    public int getWalkForwardIterationIndex() {
        return walkForwardIterationIndex;
    }

    public String getClassifier() {
        return classifier;
    }

    public boolean isFeatureSelection() {
        return featureSelection;
    }

    public boolean isSampling() {
        return sampling;
    }

    public boolean isCostSensitive() {
        return costSensitive;
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

    public BalancingType getBalancingType() {
        return balancingType;
    }


    public WekaResultEntity(String projName, int walkForwardIterationIndex, String classifier, boolean featureSelection, boolean sampling, boolean costSensitive, double precision, double recall, double auc, double kappa, BalancingType balancingType) {
        this.projName = projName;
        this.walkForwardIterationIndex = walkForwardIterationIndex;
        this.classifier = classifier;
        this.featureSelection = featureSelection;
        this.sampling = sampling;
        this.costSensitive = costSensitive;
        this.precision = precision;
        this.recall = recall;
        this.auc = auc;
        this.kappa = kappa;
        this.balancingType = balancingType;
    }




}
