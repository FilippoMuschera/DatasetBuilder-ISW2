package it.muschera.weka;

import java.util.ArrayList;
import java.util.List;

public class EvaluationSet {


    private static EvaluationSet entity = null;
    private final List<WekaResultEntity> evaluationSetList = new ArrayList<>();


    private EvaluationSet() {
        //Singleton
    }

    public static EvaluationSet getInstance() {
        if (entity == null)
            entity = new EvaluationSet();
        return entity;
    }

    public List<WekaResultEntity> getEvaluationSetList() {
        return evaluationSetList;
    }


}
