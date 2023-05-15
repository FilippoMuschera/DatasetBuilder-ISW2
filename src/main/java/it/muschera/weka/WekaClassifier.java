package it.muschera.weka;

import it.muschera.filescreators.AvgWekaDataHolder;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WekaClassifier {

    private static final String RANDOM_FOREST = "Random Forest";
    private static final String NAIVE_BAYES = "Naive Bayes";


    private static final String IBK1 = "IBk (k=1)";
    private static final String MLP = "MultiLayer Perceptron";

    private final int iteration;

    private final String projName;


    public WekaClassifier(String projName, int i) {
        this.projName = projName;
        this.iteration = i;
        Logger.getLogger("com.github.fommil").setLevel(Level.OFF);

    }

    public void computeWekaMetrics(String trainingSet, String testingSet) throws Exception {

        AvgWekaDataHolder avgWekaDataHolder = AvgWekaDataHolder.getInstance();

        //FEATURE SELECTION: NO, BALANCING: NO, COST SENSITIVE: NO
        Instances training = DataSource.read(trainingSet);
        Instances testing = DataSource.read(testingSet);
        RandomForest randomForest = new RandomForest();
        NaiveBayes naiveBayes = new NaiveBayes();
        IBk iBk1 = new IBk();
        iBk1.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));
        MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
        multilayerPerceptron.setMomentum(0.3);
        multilayerPerceptron.setLearningRate(0.3);
        multilayerPerceptron.setHiddenLayers("3");
        multilayerPerceptron.setTrainingTime(500);


        training.setClassIndex(training.numAttributes() - 1);
        testing.setClassIndex(testing.numAttributes() - 1);

        EvaluationParams evaluationParams = new EvaluationParams();
        evaluationParams.setTraining(training);
        evaluationParams.setTesting(testing);
        evaluationParams.setNaiveBayes(naiveBayes);
        evaluationParams.setRandomForest(randomForest);
        evaluationParams.setiBk1(iBk1);
        evaluationParams.setMultilayerPerceptron(multilayerPerceptron);
        evaluationParams.setFs(false);
        evaluationParams.setBalancing(false);
        evaluationParams.setCostSens(false);
        evaluationParams.setBalancingType(BalancingType.NONE);

        List<Evaluation> evaluationList = doEval(evaluationParams);
        avgWekaDataHolder.fillAvgLists(projName, RANDOM_FOREST, evaluationList.get(0), avgWekaDataHolder.getSimpleRF(), Arrays.asList(false, false, false), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, NAIVE_BAYES, evaluationList.get(1), avgWekaDataHolder.getSimpleNB(), Arrays.asList(false, false, false), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, IBK1, evaluationList.get(2), avgWekaDataHolder.getSimpleIbk(), Arrays.asList(false, false, false), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, MLP, evaluationList.get(3), avgWekaDataHolder.getSimpleMLP(), Arrays.asList(false, false, false), BalancingType.NONE);


        //FEATURE SELECTION: NO, BALANCING: SI (UNDERSAMPLE), COST SENSITIVE: NO

        evaluationParams.setTraining(underSampleDataset(training));
        evaluationParams.setBalancing(true);
        evaluationParams.setBalancingType(BalancingType.UNDERSAMPLING);

        evaluationList = doEval(evaluationParams);
        avgWekaDataHolder.fillAvgLists(projName, RANDOM_FOREST, evaluationList.get(0), avgWekaDataHolder.getSimpleUnderRF(), Arrays.asList(false, true, false), BalancingType.UNDERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, NAIVE_BAYES, evaluationList.get(1), avgWekaDataHolder.getSimpleUnderNB(), Arrays.asList(false, true, false), BalancingType.UNDERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, IBK1, evaluationList.get(2), avgWekaDataHolder.getSimpleUnderIbk(), Arrays.asList(false, true, false), BalancingType.UNDERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, MLP, evaluationList.get(3), avgWekaDataHolder.getSimpleUnderMLP(), Arrays.asList(false, true, false), BalancingType.UNDERSAMPLING);

        //FEATURE SELECTION: SI, BALANCING: NO, COST SENSITIVE: NO
        List<Instances> featureSelectedDataSet = this.featureSelection(training, testing);
        Instances trainingFeatureSelected = featureSelectedDataSet.get(0);
        Instances testingFeatureSelected = featureSelectedDataSet.get(1);
        trainingFeatureSelected.setClassIndex(trainingFeatureSelected.numAttributes() - 1);
        testingFeatureSelected.setClassIndex(testingFeatureSelected.numAttributes() - 1);


        evaluationParams.setTraining(trainingFeatureSelected);
        evaluationParams.setTesting(testingFeatureSelected);
        evaluationParams.setFs(true);
        evaluationParams.setBalancing(false);
        evaluationParams.setBalancingType(BalancingType.NONE);

        evaluationList = doEval(evaluationParams);
        avgWekaDataHolder.fillAvgLists(projName, RANDOM_FOREST, evaluationList.get(0), avgWekaDataHolder.getFeatureSelRF(), Arrays.asList(true, false, false), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, NAIVE_BAYES, evaluationList.get(1), avgWekaDataHolder.getFeatureSelNB(), Arrays.asList(true, false, false), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, IBK1, evaluationList.get(2), avgWekaDataHolder.getFeatureSelIbk(), Arrays.asList(true, false, false), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, MLP, evaluationList.get(3), avgWekaDataHolder.getFeatureSelMLP(), Arrays.asList(true, false, false), BalancingType.NONE);

        //FEATURE SLECTION: SI, BALANCING: SI (SMOTE), COST SENSITIVE: NO
        Instances smotedTrainingSet = smoteDataset(trainingFeatureSelected);
        evaluationParams.setTraining(smotedTrainingSet);
        evaluationParams.setBalancing(true);
        evaluationParams.setBalancingType(BalancingType.SMOTE);

        evaluationList = doEval(evaluationParams);
        avgWekaDataHolder.fillAvgLists(projName, RANDOM_FOREST, evaluationList.get(0), avgWekaDataHolder.getFeatureSelSmoteRF(), Arrays.asList(true, true, false), BalancingType.SMOTE);
        avgWekaDataHolder.fillAvgLists(projName, NAIVE_BAYES, evaluationList.get(1), avgWekaDataHolder.getFeatureSelSmoteNB(), Arrays.asList(true, true, false), BalancingType.SMOTE);
        avgWekaDataHolder.fillAvgLists(projName, IBK1, evaluationList.get(2), avgWekaDataHolder.getFeatureSelSmoteIbk(), Arrays.asList(true, true, false), BalancingType.SMOTE);
        avgWekaDataHolder.fillAvgLists(projName, MLP, evaluationList.get(3), avgWekaDataHolder.getFeatureSelSmoteMLP(), Arrays.asList(true, true, false), BalancingType.SMOTE);

        //FEATURE SLECTION: SI, BALANCING: SI (UNDERSAMPLING), COST SENSITIVE: NO
        evaluationParams.setTraining(underSampleDataset(trainingFeatureSelected));
        evaluationParams.setFs(true);
        evaluationParams.setBalancingType(BalancingType.UNDERSAMPLING);

        evaluationList = doEval(evaluationParams);
        avgWekaDataHolder.fillAvgLists(projName, RANDOM_FOREST, evaluationList.get(0), avgWekaDataHolder.getFeatureSelUnderRF(), Arrays.asList(true, true, false), BalancingType.UNDERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, NAIVE_BAYES, evaluationList.get(1), avgWekaDataHolder.getFeatureSelUnderNB(), Arrays.asList(true, true, false), BalancingType.UNDERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, IBK1, evaluationList.get(2), avgWekaDataHolder.getFeatureSelUnderIbk(), Arrays.asList(true, true, false), BalancingType.UNDERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, MLP, evaluationList.get(3), avgWekaDataHolder.getFeatureSelUnderMLP(), Arrays.asList(true, true, false), BalancingType.UNDERSAMPLING);

        //FEATURE SLECTION: SI, BALANCING: SI (OVERSAMPLING), COST SENSITIVE: NO
        evaluationParams.setTesting(testingFeatureSelected);
        evaluationParams.setTraining(overSampleDataset(trainingFeatureSelected));
        evaluationParams.setFs(true);
        evaluationParams.setBalancingType(BalancingType.OVERSAMPLING);

        evaluationList = doEval(evaluationParams);
        avgWekaDataHolder.fillAvgLists(projName, RANDOM_FOREST, evaluationList.get(0), avgWekaDataHolder.getFeatureSelOverRF(), Arrays.asList(true, true, false), BalancingType.OVERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, NAIVE_BAYES, evaluationList.get(1), avgWekaDataHolder.getFeatureSelOverNB(), Arrays.asList(true, true, false), BalancingType.OVERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, IBK1, evaluationList.get(2), avgWekaDataHolder.getFeatureSelOverIbk(), Arrays.asList(true, true, false), BalancingType.OVERSAMPLING);
        avgWekaDataHolder.fillAvgLists(projName, MLP, evaluationList.get(3), avgWekaDataHolder.getFeatureSelOverMLP(), Arrays.asList(true, true, false), BalancingType.OVERSAMPLING);


        //FEATURE SLECTION: SI, BALANCING: NO, COST SENSITIVE: SI
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, 1.0);
        costMatrix.setCell(0, 1, 15.0);//CFN = 15.0*CFP
        costMatrix.setCell(1, 1, 0.0);

        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        costSensitiveClassifier.setCostMatrix(costMatrix);
        costSensitiveClassifier.setMinimizeExpectedCost(true);

        List<Evaluation> evaluationListCS = doEvalCostSensitive(costSensitiveClassifier, trainingFeatureSelected, testingFeatureSelected);
        avgWekaDataHolder.fillAvgLists(projName, NAIVE_BAYES, evaluationListCS.get(0), avgWekaDataHolder.getCostSensNB(), Arrays.asList(true, false, true), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, RANDOM_FOREST, evaluationListCS.get(1), avgWekaDataHolder.getCostSensRF(), Arrays.asList(true, false, true), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, IBK1, evaluationListCS.get(2), avgWekaDataHolder.getCostSensIbk(), Arrays.asList(true, false, true), BalancingType.NONE);
        avgWekaDataHolder.fillAvgLists(projName, MLP, evaluationListCS.get(3), avgWekaDataHolder.getCostSensMLP(), Arrays.asList(true, false, true), BalancingType.NONE);


    }

    private Instances overSampleDataset(Instances training) throws Exception {
        training.setClassIndex(training.numAttributes() - 1);
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

        search.setSearchBackwards(true);
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

        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
        SMOTE smote = new SMOTE();
        int minority = trainingSet.attributeStats(trainingSet.classIndex()).nominalCounts[0];
        int majority = trainingSet.attributeStats(trainingSet.classIndex()).nominalCounts[1];
        double percentage = ((1.0) * majority - minority) / minority;
        smote.setPercentage(percentage);
        smote.setInputFormat(trainingSet);
        smote.setClassValue("0");
        return Filter.useFilter(trainingSet, smote);

    }

    private Instances underSampleDataset(Instances trainingSet) throws Exception {
        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
        SpreadSubsample spreadSubsample = new SpreadSubsample();
        spreadSubsample.setDistributionSpread(1.0);
        spreadSubsample.setInputFormat(trainingSet);
        return Filter.useFilter(trainingSet, spreadSubsample);
    }

    private List<Evaluation> doEvalCostSensitive(CostSensitiveClassifier costSensitiveClassifier, Instances trainingSet, Instances testingSet) throws Exception {

        List<Evaluation> evaluationList = new ArrayList<>();

        //CS per NaiveBayes
        costSensitiveClassifier.setClassifier(new NaiveBayes());
        costSensitiveClassifier.buildClassifier(trainingSet);
        Evaluation evaluation = new Evaluation(trainingSet, costSensitiveClassifier.getCostMatrix());
        evaluation.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation, NAIVE_BAYES, true, false, true, BalancingType.NONE);
        evaluationList.add(evaluation);

        //CS per RandomForest
        Evaluation evaluation2 = new Evaluation(trainingSet, costSensitiveClassifier.getCostMatrix());

        costSensitiveClassifier.setClassifier(new RandomForest());
        costSensitiveClassifier.buildClassifier(trainingSet);
        evaluation2.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation2, RANDOM_FOREST, true, false, true, BalancingType.NONE);
        evaluationList.add(evaluation2);


        //CS per IBk (k=1)
        Evaluation evaluation3 = new Evaluation(trainingSet, costSensitiveClassifier.getCostMatrix());

        IBk ibk1 = new IBk();
        ibk1.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));
        costSensitiveClassifier.setClassifier(ibk1);
        costSensitiveClassifier.buildClassifier(trainingSet);
        evaluation3.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation3, IBK1, true, false, true, BalancingType.NONE);
        evaluationList.add(evaluation3);


        //CS per MLP
        Evaluation evaluation4 = new Evaluation(trainingSet, costSensitiveClassifier.getCostMatrix());

        MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
        multilayerPerceptron.setMomentum(0.3);
        multilayerPerceptron.setLearningRate(0.3);
        multilayerPerceptron.setHiddenLayers("3");
        multilayerPerceptron.setTrainingTime(500);
        costSensitiveClassifier.setClassifier(multilayerPerceptron);
        costSensitiveClassifier.buildClassifier(trainingSet);
        evaluation4.evaluateModel(costSensitiveClassifier, testingSet);
        addResultToSet(evaluation4, MLP, true, false, true, BalancingType.NONE);
        evaluationList.add(evaluation4);

        return evaluationList;


    }

    public List<Evaluation> doEval(EvaluationParams params) throws Exception {

        List<Evaluation> evaluationList = new ArrayList<>();

        RandomForest randomForest = params.getRandomForest();
        NaiveBayes naiveBayes = params.getNaiveBayes();
        IBk iBk1 = params.getiBk1();
        MultilayerPerceptron multilayerPerceptron = params.getMultilayerPerceptron();
        boolean fs = params.isFs();
        boolean samp = params.isBalancing();
        boolean costSens = params.isCostSens();
        Instances training = params.getTraining();
        Instances testing = params.getTesting();

        Evaluation eval = new Evaluation(training);
        BalancingType balancingType = params.getBalancingType();

        randomForest.buildClassifier(training);
        eval.evaluateModel(randomForest, testing);
        addResultToSet(eval, RANDOM_FOREST, fs, samp, costSens, balancingType);
        evaluationList.add(eval);


        Evaluation eval2 = new Evaluation(training);

        naiveBayes.buildClassifier(training);
        eval2.evaluateModel(naiveBayes, testing);
        addResultToSet(eval2, NAIVE_BAYES, fs, samp, costSens, balancingType);
        evaluationList.add(eval2);

        Evaluation eval3 = new Evaluation(training);

        iBk1.buildClassifier(training);
        eval3.evaluateModel(iBk1, testing);
        addResultToSet(eval3, IBK1, fs, samp, costSens, balancingType);
        evaluationList.add(eval3);

        Evaluation eval4 = new Evaluation(training);

        multilayerPerceptron.buildClassifier(training);
        eval4.evaluateModel(multilayerPerceptron, testing);
        addResultToSet(eval4, MLP, fs, samp, costSens, balancingType);
        evaluationList.add(eval4);

        return evaluationList;


    }

    public void addResultToSet(Evaluation eval, String classifier, boolean fs, boolean samp, boolean costSens, BalancingType balancingType) {
        EvaluationSet evaluationSet = EvaluationSet.getInstance();
        WekaResultEntity resultEntity = new WekaResultEntity(
                this.projName,
                this.iteration,
                classifier,
                fs,
                samp,
                costSens,
                balancingType
        );
        resultEntity.setTp(eval.numTruePositives(0));
        resultEntity.setTn(eval.numTrueNegatives(0));
        resultEntity.setFp(eval.numFalsePositives(0));
        resultEntity.setFn(eval.numFalseNegatives(0));
        resultEntity.setFscore(eval.fMeasure(0));
        resultEntity.setPrecision(eval.precision(0));
        resultEntity.setRecall(eval.recall(0));
        resultEntity.setAuc(eval.areaUnderROC(0));
        resultEntity.setKappa(eval.kappa());

        evaluationSet.getEvaluationSetList().add(resultEntity);
    }


}
