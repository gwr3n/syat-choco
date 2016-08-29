package org.chocosolver.solver.constraints.statistical.binary;

import gnu.trove.map.hash.THashMap;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.VariableState;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.syat.statistics.KolmogorovSmirnovTest;
import org.syat.statistics.TTest;
import org.chocosolver.solver.variables.statistical.distributions.DistributionVar;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;

/**
 * X <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class PropLessOrEqualX_DStDist extends Propagator<IntVar> {

    private final DistributionVar dist;
    private final double confidence;

    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }
    
    public PropLessOrEqualX_DStDist(IntVar[] var, DistributionVar dist, double confidence) {
        super(mergeArrays(var,dist.getVarParatemers()), PropagatorPriority.BINARY, true);
        if(!(dist instanceof ContinuousDistribution)) 
			throw new SolverException("Theoretical distribution should not be discrete");
        if(dist.getNumberOfVarParameters() > 1)
        	throw new SolverException("This propagator only supports distribution with a single parameter");
        this.dist = dist;
        this.confidence = confidence;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask();
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
        	this.dist.setParameters(new double[]{this.dist.getVarParatemers()[0].getUB()});
			KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest(emp, this.dist, this.confidence);
			while(!ksTest.testD1GeqE1()){
				pivotVar.updateUpperBound(pivotVar.getUB()-1, aCause);
				samples[0] = pivotVar.getUB();
				emp = new EmpiricalDist(samples);
				ksTest = new KolmogorovSmirnovTest(emp, this.dist, this.confidence);
			}
        }
        
        double[] samples = new double[vars.length];
    	for(int j = 0; j < vars.length; j++){
    		samples[j] = vars[j].getLB();
    	}
    	EmpiricalDist emp = new EmpiricalDist(samples);
    	IntVar pivotVar = dist.getVarParatemers()[0];
    	this.dist.setParameters(new double[]{this.dist.getVarParatemers()[0].getLB()});
    	KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest(emp, this.dist, this.confidence);
    	while(!ksTest.testD1GeqE1()){
    		pivotVar.updateLowerBound(pivotVar.getLB()+1, aCause);
    		this.dist.setParameters(new double[]{this.dist.getVarParatemers()[0].getLB()});
    		ksTest = new KolmogorovSmirnovTest(emp, this.dist, this.confidence);
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
    	throw new SolverException("Constraint duplication not implemented");
    }
    
    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        throw new SolverException("Constraint duplication not implemented");
    }
}

