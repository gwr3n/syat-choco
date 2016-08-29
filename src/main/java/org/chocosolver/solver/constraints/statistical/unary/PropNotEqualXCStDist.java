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

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;

import org.chocosolver.util.iterators.DisposableValueIterator;
import org.syat.statistics.KolmogorovSmirnovTest;

/**
 * X <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class PropNotEqualXCStDist extends Propagator<IntVar> {

	private final Distribution dist;
    private final double confidence;

    public PropNotEqualXCStDist(IntVar[] var, Distribution dist, double confidence) {
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
        
        for(int i = 0; i < vars.length; i++){
        	double[] samplesLB = new double[vars.length];
        	int k = 1;
        	for(int j = 0; j < vars.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesLB[k++] = vars[j].getLB();
        	}
        	double[] samplesUB = new double[vars.length];
        	k = 1;
        	for(int j = 0; j < vars.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesUB[k++] = vars[j].getUB();
        	}
        	
        	IntVar pivotVar = vars[i];
        	DisposableValueIterator iterator = pivotVar.getValueIterator(true);
        	while(iterator.hasNext()){
        		int value = iterator.next();
        		samplesLB[0] = samplesUB[0] = value;
        		EmpiricalDist empLB = new EmpiricalDist(samplesLB);
        		KolmogorovSmirnovTest ksTestLB = new KolmogorovSmirnovTest(empLB, this.dist, this.confidence);
    			EmpiricalDist empUB = new EmpiricalDist(samplesUB);
    			KolmogorovSmirnovTest ksTestUB = new KolmogorovSmirnovTest(empUB, this.dist, this.confidence);
    			
    			if(ksTestLB.testD1GeqE1() && ksTestUB.testE1GeqD1()){
    				pivotVar.removeValue(value, aCause);
    			}
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


