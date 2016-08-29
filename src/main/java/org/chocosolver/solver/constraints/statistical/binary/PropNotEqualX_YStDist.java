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
import org.chocosolver.util.tools.ArrayUtils;
import org.syat.statistics.KolmogorovSmirnovTestTwoSamples;
import org.syat.statistics.TTest;
import org.syat.statistics.TTestTwoSamples;
import org.chocosolver.util.iterators.DisposableValueIterator;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;


/**
 * X >= Y
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public final class PropNotEqualX_YStDist extends Propagator<IntVar> {

    final IntVar[] x;
    final IntVar[] y;
    final double confidence;

    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }
    
    @SuppressWarnings({"unchecked"})
    public PropNotEqualX_YStDist(IntVar[] var1, IntVar[] var2, double confidence) {
        super(mergeArrays(var1, var2), PropagatorPriority.BINARY, true);
        if(var1.length == 1 || var2.length == 1)
        	throw new SolverException("Cannot propagate statistically on single observations");
        this.x = var1;
        this.y = var2;
        this.confidence = confidence;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
    	return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
    	propagateTwoSampleKSTestDist(0);
    }
    
    /**
     * Double check validity if samples are not single observations
     * @param evtmask
     * @throws ContradictionException
     */
    private void propagateTwoSampleKSTestDist(int evtmask) throws ContradictionException {
    	for(int i = 0; i < this.x.length; i++){
    		double[] samplesXLB = new double[x.length];
        	int k = 1;
        	for(int j = 0; j < x.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesXLB[k++] = x[j].getLB();
        	}
        	double[] samplesXUB = new double[x.length];
        	k = 1;
        	for(int j = 0; j < x.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesXUB[k++] = x[j].getUB();
        	}
        	double[] samplesYLB = new double[y.length];
        	for(int j = 0; j < y.length; j++){
        		samplesYLB[j] = y[j].getLB();
        	}
        	double[] samplesYUB = new double[y.length];
        	for(int j = 0; j < y.length; j++){
        		samplesYUB[j] = y[j].getUB();
        	}
        	
        	IntVar pivotVar = x[i];
        	DisposableValueIterator iterator = pivotVar.getValueIterator(true);
        	while(iterator.hasNext()){
        		int value = iterator.next();
        		samplesXLB[0] = samplesXUB[0] = value;
        		EmpiricalDist empXLB = new EmpiricalDist(samplesXLB);
        		EmpiricalDist empYUB = new EmpiricalDist(samplesYUB);
        		KolmogorovSmirnovTestTwoSamples ksTestXLBYUB = new KolmogorovSmirnovTestTwoSamples(empXLB, empYUB, this.confidence);
    			EmpiricalDist empXUB = new EmpiricalDist(samplesXUB);
    			EmpiricalDist empYLB = new EmpiricalDist(samplesYLB);
    			KolmogorovSmirnovTestTwoSamples ksTestXUBYLB = new KolmogorovSmirnovTestTwoSamples(empYLB, empXUB, this.confidence);
    			
    			if(ksTestXLBYUB.testE2GeqE1() && ksTestXUBYLB.testE2GeqE1()){
    				pivotVar.removeValue(value, aCause);
    			}	
        	}
        }
    	
    	for(int i = 0; i < this.y.length; i++){
    		double[] samplesYLB = new double[y.length];
        	int k = 1;
        	for(int j = 0; j < y.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesYLB[k++] = y[j].getLB();
        	}
        	double[] samplesYUB = new double[y.length];
        	k = 1;
        	for(int j = 0; j < y.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesYUB[k++] = y[j].getUB();
        	}
        	double[] samplesXLB = new double[x.length];
        	for(int j = 0; j < x.length; j++){
        		samplesYLB[j] = x[j].getLB();
        	}
        	double[] samplesXUB = new double[x.length];
        	for(int j = 0; j < x.length; j++){
        		samplesYUB[j] = x[j].getUB();
        	}
        	
        	IntVar pivotVar = y[i];
        	DisposableValueIterator iterator = pivotVar.getValueIterator(true);
        	while(iterator.hasNext()){
        		int value = iterator.next();
        		samplesYLB[0] = samplesYUB[0] = value;
        		EmpiricalDist empXLB = new EmpiricalDist(samplesXLB);
        		EmpiricalDist empYUB = new EmpiricalDist(samplesYUB);
        		KolmogorovSmirnovTestTwoSamples ksTestXLBYUB = new KolmogorovSmirnovTestTwoSamples(empXLB, empYUB, this.confidence);
    			EmpiricalDist empXUB = new EmpiricalDist(samplesXUB);
    			EmpiricalDist empYLB = new EmpiricalDist(samplesYLB);
    			KolmogorovSmirnovTestTwoSamples ksTestXUBYLB = new KolmogorovSmirnovTestTwoSamples(empYLB, empXUB, this.confidence);
    			
    			if(ksTestXLBYUB.testE2GeqE1() && ksTestXUBYLB.testE2GeqE1()){
    				pivotVar.removeValue(value, aCause);
    			}	
        	}
        }
    }
    
    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }


    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        bf.append("prop(").append(vars[0].getName()).append(".GEQ.").append(vars[1].getName()).append(")");
        return bf.toString();
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
