package org.chocosolver.solver.variables.stochastic;

import java.util.ArrayList;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;


public class ScenarioTree {

    public static void main(String args[]){
        ScenarioTree tree = new ScenarioTree();
        Stage stage1 = new Stage();
        stage1.addDecisionVar(new DecisionVar(0, 10));
        //stage1.addDecisionVar(new DecisionVar(0, 5));
        int[] values = {1,2,3};
        double[] prob = {0.5,0.3,0.2};
        stage1.addRandomVar(new RandomVar(values,prob));
        //int[] values1 = {3,4};
        //double[] prob1 = {0.6,0.4};
        //stage1.addRandomVar(new RandomVar(values1,prob1));
        tree.addStage(stage1);
        Stage stage2 = new Stage();
        stage2.addDecisionVar(new DecisionVar(0, 7));
        int[] values2 = {4,5,6};
        double[] prob2 = {0.1,0.2,0.7};
        stage2.addRandomVar(new RandomVar(values2,prob2));
        int[] values3 = {6,7};
        double[] prob3 = {0.4,0.6};
        stage2.addRandomVar(new RandomVar(values3,prob3));
        tree.addStage(stage2);
        Solver solver = new Solver("TTest");
        tree.generateTables();
        tree.generateDecisionVariables(solver);
        for(int x = 0; x < tree.scenarioValues.length; x++){
            for(int y = 0; y < tree.scenarioValues[x].length; y++){
                System.out.print(tree.scenarioValues[x][y]+" ");
            }
            System.out.println("     " + tree.scenarioProbabilities[x]);
        }

        for(int x = 0; x < tree.scenarioIntDecVars.length; x++){
            for(int y = 0; y < tree.scenarioIntDecVars[x].length; y++){
                System.out.print(tree.scenarioIntDecVars[x][y].getName()+" ");
            }
            System.out.println();
        }

        IntVar[]  dva = tree.getDecisionVarArray();
        for(int x = 0; x < dva.length; x++){
            System.out.print(dva[x]+" ");
        }
        System.out.println();
    }

    ArrayList<Stage> stages = new ArrayList<Stage>();

    public double[] scenarioProbabilities; //OK
    public int[][] scenarioValues; //OK
    public double[][] scenarioProbabilityValues; //OK
    public IntVar[][] scenarioIntDecVars; //OK
    public static IntVar[][] sScenarioIntDecVars; //OK
    ArrayList<IntVar> scenarioIntDecVarsArray = new ArrayList<IntVar>();

    public void addStage(Stage stage){
        this.stages.add(stage);
    }

    public void generateTables(){
        this.scenarioProbabilities = new double[getScenarioTableRows()];
        this.scenarioProbabilityValues = new double[getScenarioTableRows()][getScenarioTableCols()];
        this.scenarioValues = new int[getScenarioTableRows()][getScenarioTableCols()];
        fillScenarioValuesTableRows();
        fillProbabilityValuesTableRows();
        convertProbabilities();
    }

    public IntVar[] getDecisionVarArray(){
        scenarioIntDecVarsArray.trimToSize();
        IntVar[] array = new IntVar[scenarioIntDecVarsArray.size()];
        return scenarioIntDecVarsArray.toArray(array);
    }

    public void generateDecisionVariables(Solver solver){
        this.scenarioIntDecVars = new IntVar[getScenarioTableRows()][getDecisionVarTableCols()];
        fillScenarioIntDecVarsTableRows(solver);
        ScenarioTree.sScenarioIntDecVars = this.scenarioIntDecVars;
    }

    public void fillScenarioIntDecVarsTableRows(Solver solver){
        @SuppressWarnings("unchecked")
      ArrayList<Stage> stagesBuffer = (ArrayList<Stage>)this.stages.clone();
        fillIntDecVarsScenario(stagesBuffer, 0, solver);
    }

    public void fillIntDecVarsScenario(ArrayList<Stage> stages, int colOffset, Solver solver){
        int uniqueID = 0;
        int numberOfScenarios = 1;
        while (stages.size() > 0) {
            Stage curStage = stages.remove(0);
            for (int x = 0; x < curStage.decisionVariables.size(); x++) {
                int module = this.scenarioIntDecVars.length / numberOfScenarios;
                IntVar curDecisionVar = curStage.decisionVariables.get(x).getIntDomainVar("" + uniqueID++, solver);
                scenarioIntDecVarsArray.add(curDecisionVar);
                for (int y = 0; y < this.scenarioIntDecVars.length; y++) {
                    this.scenarioIntDecVars[y][colOffset] = curDecisionVar;
                    if (y + 1 >= module && y + 1 < this.scenarioIntDecVars.length && (y + 1) % module == 0) {
                        curDecisionVar = curStage.decisionVariables.get(x).getIntDomainVar("" + uniqueID++, solver);
                        scenarioIntDecVarsArray.add(curDecisionVar);
                    }
                }
                colOffset++;
            }
            for (int i = 0; i < curStage.randomVariables.size(); i++) {
                numberOfScenarios *= curStage.randomVariables.get(i).values.length;
            }
        }
    }

    public void convertProbabilities(){
        for(int x = 0; x < this.scenarioProbabilityValues.length; x++){
            this.scenarioProbabilities[x] = 1;
            for(int y = 0; y < this.scenarioProbabilityValues[x].length; y++){
                this.scenarioProbabilities[x] *= scenarioProbabilityValues[x][y];
            }
        }
    }

    public void fillProbabilityValuesTableRows(){
        @SuppressWarnings("unchecked")
      ArrayList<Stage> stagesBuffer = (ArrayList<Stage>)this.stages.clone();
        fillStageScenarioProbabilities(stagesBuffer, 0);
    }

    public void fillStageScenarioProbabilities(ArrayList<Stage> stages, int colOffset){
        int numberOfScenarios = 1;
        while(stages.size() > 0){
            Stage curStage = stages.remove(0);
            for (int x = 0; x < curStage.randomVariables.size(); x++) {
                numberOfScenarios *= curStage.randomVariables.get(x).values.length;
                int module = this.scenarioProbabilities.length / numberOfScenarios;
                int counter = 0;
                for (int y = 0; y < this.scenarioProbabilities.length; y++) {
                    this.scenarioProbabilityValues[y][colOffset] = curStage.randomVariables.get(x).probabilities[counter];
                    if (y + 1 >= module && (y + 1) % module == 0) {
                        counter++;
                    }
                    if (counter >=
                        curStage.randomVariables.get(x).values.length) {
                        counter = 0;
                    }
                }
                colOffset++;
            }
        }
    }

    public void fillScenarioValuesTableRows(){
        @SuppressWarnings("unchecked")
      ArrayList<Stage> stagesBuffer = (ArrayList<Stage>)this.stages.clone();
        fillStageScenario(stagesBuffer, 0);
    }

    public void fillStageScenario(ArrayList<Stage> stages, int colOffset){
        int numberOfScenarios = 1;
        while(stages.size() > 0){
            Stage curStage = stages.remove(0);
            for (int x = 0; x < curStage.randomVariables.size(); x++) {
                numberOfScenarios *= curStage.randomVariables.get(x).values.length;
                int module = this.scenarioValues.length / numberOfScenarios;
                int counter = 0;
                for (int y = 0; y < this.scenarioValues.length; y++) {
                    this.scenarioValues[y][colOffset] = curStage.randomVariables.get(x).values[counter];
                    if (y + 1 >= module && (y + 1) % module == 0) {
                        counter++;
                    }
                    if (counter >=
                        curStage.randomVariables.get(x).values.length) {
                        counter = 0;
                    }
                }
                colOffset++;
            }
        }
    }

    public int getScenarioTableRows(){
        @SuppressWarnings("unchecked")
      ArrayList<Stage> stagesBuffer = (ArrayList<Stage>)this.stages.clone();
        return handleStageScenario(stagesBuffer);
    }

    public int handleStageScenario(ArrayList<Stage> stages){
        Stage curStage = stages.remove(0);
        int numberOfScenarios = 1;
        for(int x = 0; x < curStage.randomVariables.size(); x++){
            numberOfScenarios *= curStage.randomVariables.get(x).probabilities.length;
        }
        if(stages.size() == 0)
            return numberOfScenarios;
        else
            return numberOfScenarios*handleStageScenario(stages);
    }

    public int getScenarioTableCols(){
        int randVars = 0;
        for(int x = 0; x < this.stages.size(); x++){
            randVars += this.stages.get(x).randomVariables.size();
        }
        return randVars;
    }

    public int getDecisionVarTableCols(){
        int decVars = 0;
        for(int x = 0; x < this.stages.size(); x++){
            decVars += this.stages.get(x).decisionVariables.size();
        }
        return decVars;
    }
}
