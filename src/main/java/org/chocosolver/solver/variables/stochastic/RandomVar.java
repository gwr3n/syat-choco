package org.chocosolver.solver.variables.stochastic;

public class RandomVar {
    double[] probabilities;
    int[] values;

    public RandomVar(int[] values, double[] probabilities){
        this.probabilities = probabilities;
        this.values = values;
    }

    public double getProbability(int i){
        return this.probabilities[i];
    }

    public double getValue(int i){
        return this.values[i];
    }

}
