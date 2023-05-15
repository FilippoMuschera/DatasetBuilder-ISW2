package it.muschera.weka;

public class WekaResultEntity {


    private final String projName;
    private final int walkForwardIterationIndex;
    private final String classifier;
    private final boolean featureSelection;
    private final boolean sampling;
    private final boolean costSensitive;
    private final BalancingType balancingType;
    private double precision;
    private double recall;
    private double fscore;
    private double auc;
    private double kappa;
    private double tp;
    private double tn;
    private double fp;
    private double fn;


    public WekaResultEntity(String projName, int walkForwardIterationIndex, String classifier, boolean featureSelection, boolean sampling, boolean costSensitive, BalancingType balancingType) {
        this.projName = projName;
        this.walkForwardIterationIndex = walkForwardIterationIndex;
        this.classifier = classifier;
        this.featureSelection = featureSelection;
        this.sampling = sampling;
        this.costSensitive = costSensitive;
        this.balancingType = balancingType;
    }

    public double getTp() {
        return tp;
    }

    public void setTp(double tp) {
        this.tp = tp;
    }

    public double getFscore() {
        return fscore;
    }

    public void setFscore(double fscore) {
        this.fscore = fscore;
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

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getAuc() {
        return auc;
    }

    public void setAuc(double auc) {
        this.auc = auc;
    }

    public double getKappa() {
        return kappa;
    }

    public void setKappa(double kappa) {
        this.kappa = kappa;
    }

    public BalancingType getBalancingType() {
        return balancingType;
    }
}
