package org.chocosolver.solver.variables.stochastic;

import java.util.ArrayList;

public class Stage {
    ArrayList<DecisionVar> decisionVariables = new ArrayList<DecisionVar>();
    ArrayList<RandomVar> randomVariables = new ArrayList<RandomVar>();

    public void addDecisionVar(DecisionVar var){
        this.decisionVariables.add(var);
    }

    public void addRandomVar(RandomVar var){
        this.randomVariables.add(var);
    }
}
