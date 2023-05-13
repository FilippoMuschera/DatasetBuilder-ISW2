package it.muschera.weka;

public class WekaResultEntity {


    private final String projName;
    private final int walkForwardIterationIndex;
    private final String classifier;
    private final boolean featureSelection;
    private final boolean sampling;
    private final boolean costSensitive;
    private final double precision;
    private final double recall;
    private final double auc;
    private final double kappa;
    private double tp;

    private double tn;
    private double fp;
    private double fn;


    private final BalancingType balancingType;


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

    public double getTp() {
        return tp;
    }

    public void setTp(double tp) {
        this.tp = tp;
    }

    public double getTn() {
        return tn;
    }

    public void setTn(double tn) {
        this.tn = tn;
    }

    public double getFp() {
        return fp;
    }

    public void setFp(double fp) {
        this.fp = fp;
    }

    public double getFn() {
        return fn;
    }

    public void setFn(double fn) {
        this.fn = fn;
    }
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


}
