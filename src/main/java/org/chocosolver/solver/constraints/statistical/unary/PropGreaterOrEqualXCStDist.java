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
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.syat.statistics.KolmogorovSmirnovTest;
import org.syat.statistics.TTest;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;

/**
 * X <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class PropGreaterOrEqualXCStDist extends Propagator<IntVar> {

    private final Distribution dist;
    private final double confidence;

    public PropGreaterOrEqualXCStDist(IntVar[] var, Distribution dist, double confidence) {
        super(var, PropagatorPriority.UNARY, true);
        if(!(dist instanceof ContinuousDistribution)) 
			throw new SolverException("Theoretical distribution should not be discrete");
        this.dist = dist;
        this.confidence = confidence;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // with views such as abs(...), the prop can be not entailed after initial propagation
        /*if (vars[0].updateUpperBound(constant, aCause) || vars[0].getUB() <= constant) {
            this.setPassive();
        }*/
        
    	//int counter = 0;
        for(int i = 0; i < vars.length; i++){
        	double[] samples = new double[vars.length];
        	IntVar pivotVar = vars[i];
        	int k = 0;
        	samples[k++] = pivotVar.getLB();
        	for(int j = 0; j < vars.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samples[k++] = vars[j].getUB();
        	}
        	EmpiricalDist emp = new EmpiricalDist(samples);
			KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest(emp, this.dist, this.confidence);
			while(!ksTest.testE1GeqD1()){
				pivotVar.updateLowerBound(pivotVar.getLB()+1, aCause);
				//counter++;
				samples[0] = pivotVar.getLB();
				emp = new EmpiricalDist(samples);
				ksTest = new KolmogorovSmirnovTest(emp, this.dist, this.confidence);
			}
        }
        //if(counter > 0)
        	//LoggerFactory.getLogger("bench").info("Pruned (GE): "+counter);
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
        return vars[0].getName() + " <= " + dist.toString();
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
