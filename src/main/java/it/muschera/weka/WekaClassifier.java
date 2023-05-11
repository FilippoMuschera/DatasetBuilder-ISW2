package it.muschera.weka;

import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;


public class WekaClassifier {

    private static final String RANDOM_FOREST = "Random Forest";
    private static final String NAIVE_BAYES = "Naive Bayes";

    private static final int NEIGHBOURS = 5;
    private static final String IBK1 = "IBk (k=1)";
    private static final String IBKN = "IBk (k=" + NEIGHBOURS + ")";

    private final int iteration;

    private final String projName;


    public WekaClassifier(String projName, int i) {
        this.projName = projName;
        this.iteration = i;
    }

    public void computeWekaMetrics(String trainingSet, String testingSet) throws Exception {

        //FEATURE SELECTION: NO, BALANCING: NO, COST SENSITIVE: NO
        Instances training = DataSource.read(trainingSet);
        Instances testing = DataSource.read(testingSet);
        RandomForest randomForest = new RandomForest();
        NaiveBayes naiveBayes = new NaiveBayes();
        IBk iBk1 = new IBk();
        iBk1.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));
        IBk iBkN = new IBk(NEIGHBOURS);
        iBkN.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));

        training.setClassIndex(training.numAttributes() - 1);
        testing.setClassIndex(testing.numAttributes() - 1);

        EvaluationParams evaluationParams = new EvaluationParams();
        evaluationParams.setTraining(training);
        evaluationParams.setTesting(testing);
        evaluationParams.setNaiveBayes(naiveBayes);
        evaluationParams.setRandomForest(randomForest);
        evaluationParams.setiBk1(iBk1);
        evaluationParams.setiBkN(iBkN);
        evaluationParams.setFs(false);
        evaluationParams.setBalancing(false);
        evaluationParams.setCostSens(false);
        evaluationParams.setBalancingType(BalancingType.NONE);

        doEval(evaluationParams);

        //FEATURE SELECTION: SI, BALANCING: NO, COST SENSITIVE: NO
        List<Instances> featureSelectedDataSet = this.featureSelection(training, testing);
        Instances trainingFeatureSelected = featureSelectedDataSet.get(0);
        Instances testingFeatureSelected = featureSelectedDataSet.get(1);
        trainingFeatureSelected.setClassIndex(trainingFeatureSelected.numAttributes() - 1);
        testingFeatureSelected.setClassIndex(testingFeatureSelected.numAttributes() - 1);


        evaluationParams.setTraining(trainingFeatureSelected);
        evaluationParams.setTesting(testingFeatureSelected);
        evaluationParams.setFs(true);

        doEval(evaluationParams);

        //FEATURE SLECTION: SI, BALANCING: SI (SMOTE), COST SENSITIVE: NO
        Instances smotedTrainingSet = smoteDataset(trainingFeatureSelected);
        evaluationParams.setTraining(smotedTrainingSet);
        evaluationParams.setBalancing(true);
        evaluationParams.setBalancingType(BalancingType.SMOTE);

        doEval(evaluationParams);

        //FEATURE SLECTION: SI, BALANCING: SI (UNDERSAMPLING), COST SENSITIVE: NO
        evaluationParams.setTraining(underSampleDataset(trainingFeatureSelected));
        evaluationParams.setBalancingType(BalancingType.UNDERSAMPLING);


        doEval(evaluationParams);

        //FEATURE SLECTION: SI, BALANCING: SI (OVERSAMPLING), COST SENSITIVE: NO //TODO rimettere FS
        evaluationParams.setTesting(testing);
        evaluationParams.setTraining(overSampleDataset(training));
        evaluationParams.setFs(false);
        evaluationParams.setBalancingType(BalancingType.OVERSAMPLING);
        IBk ibk = new IBk();
        String[] options = new String[]{ "-K", "5", "-W", "0", "-A", "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\"" };
        ibk.setOptions(options);
        evaluationParams.setiBkN(ibk);


        doEval(evaluationParams);


        //FEATURE SLECTION: SI, BALANCING: NO, COST SENSITIVE: SI
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, 10.0); //CFN = 10.0*CFP
        costMatrix.setCell(0, 1, 1.0);
        costMatrix.setCell(1, 1, 0.0);

        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        costSensitiveClassifier.setCostMatrix(costMatrix);
        costSensitiveClassifier.setMinimizeExpectedCost(true);

        doEvalCostSensitive(costSensitiveClassifier, trainingFeatureSelected, testingFeatureSelected);





    }

    private Instances overSampleDataset(Instances training) throws Exception {
        training.setClassIndex(training.numAttributes() -1);
        Resample resample = new Resample();
        resample.setBiasToUniformClass(1.0);
        resample.setInputFormat(training);

        return Filter.useFilter(training, resample);
    }


    private List<Instances> featureSelection(Instances training, Instances testing) throws Exception {
        // setup filter
        AttributeSelection filter = new AttributeSelection();
        CfsSubsetEval eval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();

        search.setSearchBackwards(false);
        filter.setEvaluator(eval);
        filter.setSearch(search);
        filter.setInputFormat(training);

        Instances newTraining = Filter.useFilter(training, filter);
        Instances newTesting = Filter.useFilter(testing, filter);

        List<Instances> returnList = new ArrayList<>();
        returnList.add(newTraining);
        returnList.add(newTesting);

        return returnList;


    }

    private Instances smoteDataset(Instances trainingSet) throws Exception {

        trainingSet.setClassIndex(trainingSet.numAttributes() -1);
        SMOTE smote = new SMOTE();
        int minority = trainingSet.attributeStats(trainingSet.classIndex()).nominalCounts[0];
        int majority = trainingSet.attributeStats(trainingSet.classIndex()).nominalCounts[1];
        double percentage = ((1.0)*majority - minority)/minority;
        smote.setPercentage(percentage);
        smote.setInputFormat(trainingSet);
        smote.setClassValue("0");
        return Filter.useFilter(trainingSet, smote);

    }

    private Instances underSampleDataset(Instances trainingSet) throws Exception {
        trainingSet.setClassIndex(trainingSet.numAttributes() -1);
        SpreadSubsample spreadSubsample = new SpreadSubsample();
        spreadSubsample.setDistributionSpread(1.0);
        spreadSubsample.setInputFormat(trainingSet);
        return Filter.useFilter(trainingSet, spreadSubsample);
    }

    private void doEvalCostSensitive(CostSensitiveClassifier costSensitiveClassifier, Instances trainingSet, Instances testingSet) throws Exception {

        //CS per NaiveBayes
        costSensitiveClassifier.setClassifier(new NaiveBayes());
        costSensitiveClassifier.buildClassifier(trainingSet);
        Evaluation evaluation = new Evaluation(trainingSet, costSensitiveClassifier.getCostMatrix());
        evaluation.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation, NAIVE_BAYES, true, false, true, BalancingType.NONE);

        //CS per RandomForest
        costSensitiveClassifier.setClassifier(new RandomForest());
        costSensitiveClassifier.buildClassifier(trainingSet);
        evaluation.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation, RANDOM_FOREST, true, false, true, BalancingType.NONE);

        //CS per IBk (k=1)
        IBk ibk1 = new IBk();
        ibk1.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));
        costSensitiveClassifier.setClassifier(ibk1);
        costSensitiveClassifier.buildClassifier(trainingSet);
        evaluation.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation, IBK1, true, false, true, BalancingType.NONE);

        //CS per IBk (k=N)
        IBk ibkN = new IBk(NEIGHBOURS);
        ibkN.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));
        costSensitiveClassifier.setClassifier(ibkN);
        costSensitiveClassifier.buildClassifier(trainingSet);
        evaluation.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation, IBKN, true, false, true, BalancingType.NONE);



    }

    public void doEval(EvaluationParams params) throws Exception {

        RandomForest randomForest = params.getRandomForest();
        NaiveBayes naiveBayes = params.getNaiveBayes();
        IBk iBk1 = params.getiBk1();
        IBk iBkN = params.getiBkN();
        boolean fs = params.isFs();
        boolean samp = params. isBalancing();
        boolean costSens = params.isCostSens();
        Instances training = params.getTraining();
        Instances testing = params.getTesting();
        //bug?
        //Evaluation eval = new Evaluation(testing);
        //fix?
        Evaluation eval = new Evaluation(training);
        BalancingType balancingType = params.getBalancingType();

        randomForest.buildClassifier(training);
        eval.evaluateModel(randomForest, testing);
        addResultToSet(eval, RANDOM_FOREST, fs, samp, costSens, balancingType);

        naiveBayes.buildClassifier(training);
        eval.evaluateModel(naiveBayes, testing);
        addResultToSet(eval, NAIVE_BAYES, fs, samp, costSens, balancingType);

        iBk1.buildClassifier(training);
        eval.evaluateModel(iBk1, testing);
        addResultToSet(eval, IBK1, fs, samp, costSens, balancingType);

        iBkN.buildClassifier(training);
        eval.evaluateModel(iBkN, testing);
        addResultToSet(eval, IBKN, fs, samp, costSens, balancingType);










    }

    public void addResultToSet(Evaluation eval, String classifier, boolean fs, boolean samp, boolean costSens, BalancingType balancingType) {
        EvaluationSet evaluationSet = EvaluationSet.getInstance();
        evaluationSet.getEvaluationSetList().add(
                new WekaResultEntity(
                        this.projName,
                        this.iteration,
                        classifier,
                        fs,
                        samp,
                        costSens,
                        eval.precision(0),
                        eval.recall(0),
                        eval.areaUnderROC(0),
                        eval.kappa(),
                        balancingType
                )

        );
    }




}
