package org.chocosolver.solver.constraints.statistical.unary;

import gnu.trove.map.hash.THashMap;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.syat.statistics.TTest;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;

/**
 * X <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
@SuppressWarnings("serial")
public class PropLessOrEqualXCStMean extends Propagator<IntVar> {

    private final int constant;
    private final double confidence;

    public PropLessOrEqualXCStMean(IntVar[] var, int cste, double confidence) {
        super(var, PropagatorPriority.UNARY, true);
        this.constant = cste;
        this.confidence = confidence;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // with views such as abs(...), the prop can be not entailed after initial propagation
        /*if (vars[0].updateUpperBound(constant, aCause) || vars[0].getUB() <= constant) {
            this.setPassive();
        }*/
        
        for(int i = 0; i < vars.length; i++){
        	double[] samples = new double[vars.length];
        	IntVar pivotVar = vars[i];
        	int k = 0;
        	samples[k++] = pivotVar.getUB();
        	for(int j = 0; j < vars.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samples[k++] = vars[j].getLB();
        	}
        	EmpiricalDist emp = new EmpiricalDist(samples);
			TTest tTest = new TTest(emp, this.constant, this.confidence);
			while(!tTest.testTMGeqE1(this.confidence)){
				pivotVar.updateUpperBound(pivotVar.getUB()-1, aCause);
				samples[0] = pivotVar.getUB();
				emp = new EmpiricalDist(samples);
				tTest = new TTest(emp, this.constant, this.confidence);
			}
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
        /*if (vars[0].getUB() <= constant) {
            return ESat.TRUE;
        }
        if (vars[0].getLB() > constant) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;*/
    	return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].getName() + " <= " + constant;
    }

    @Override
    public void explain(ExplanationEngine xengine, Deduction d, Explanation e) {
        //e.add(xengine.getPropagatorActivation(this));
        throw new SolverException("Constraint duplication not implemented");
    }
    
    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        throw new SolverException("Constraint duplication not implemented");
    }
}

