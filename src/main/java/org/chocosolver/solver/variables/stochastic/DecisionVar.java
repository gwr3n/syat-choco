package org.chocosolver.solver.variables.stochastic;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.iterators.DisposableValueIterator;

public class DecisionVar {
	public DomainVal[] values;
    private int[] intValues;
    
    public DecisionVar(int[] sortedDomain){
    	Arrays.sort(sortedDomain);
        this.values = new DomainVal[sortedDomain.length];
        this.intValues = new int[sortedDomain.length];
        for(int x = 0; x < sortedDomain.length; x++){
            this.intValues[x] = sortedDomain[x];
            this.values[x] = new DomainVal(sortedDomain[x]);
        } 
    }

    public DecisionVar(int LB, int UB){
        this.values = new DomainVal[UB-LB+1];
        this.intValues = new int[UB-LB+1];
        for(int x = 0; x < UB-LB+1; x++){
            this.intValues[x] = LB + x;
            this.values[x] = new DomainVal(LB + x);
        }

    }

    public DecisionVar(IntVar lvar){
        this.values = new DomainVal[lvar.getDomainSize()];
        this.intValues = new int[lvar.getDomainSize()];
        DisposableValueIterator iter = lvar.getValueIterator(true);
        int x = 0;
        while(iter.hasNext()){
            this.intValues[x] = iter.next();
            this.values[x] = new DomainVal(this.intValues[x]);
            x++;
        }
    }

    public IntVar getIntDomainVar(String name, Solver solver){
        return VariableFactory.enumerated(name, intValues, solver);
    }
}
