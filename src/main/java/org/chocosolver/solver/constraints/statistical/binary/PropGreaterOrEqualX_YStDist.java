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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.syat.statistics.KolmogorovSmirnovTestTwoSamples;
import org.syat.statistics.TTest;
import org.syat.statistics.TTestTwoSamples;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;

/**
 * X >= Y
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public final class PropGreaterOrEqualX_YStDist extends Propagator<IntVar> {

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
    public PropGreaterOrEqualX_YStDist(IntVar[] var1, IntVar[] var2, double confidence) {
        super(mergeArrays(var1, var2), PropagatorPriority.BINARY, true);
        if(var1.length == 1 || var2.length == 1)
        	throw new SolverException("Cannot propagate statistically on single observations");
        this.x = var1;
        this.y = var2;
        this.confidence = confidence;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
    	if (vIdx == 0) {
            return IntEventType.INSTANTIATE.getMask() + IntEventType.DECUPP.getMask();
        } else {
            return IntEventType.INSTANTIATE.getMask() + IntEventType.INCLOW.getMask();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
    	propagateTwoSampleKSTestDist(0);
    }
    
    private void propagateTwoSampleKSTestDist(int evtmask) throws ContradictionException {
    	for(int i = 0; i < this.x.length; i++){
        	double[] samplesX = new double[this.x.length];
        	IntVar pivotVar = this.x[i];
        	int k = 0;
        	samplesX[k++] = pivotVar.getLB();
        	for(int j = 0; j < this.x.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesX[k++] = this.x[j].getUB();
        	}
        	EmpiricalDist empX = new EmpiricalDist(samplesX);
        	
        	double[] samplesY = new double[this.y.length];
        	for(int j = 0; j < this.y.length; j++){
        		samplesY[j] = this.y[j].getLB();
        	}
        	EmpiricalDist empY = new EmpiricalDist(samplesY);
        	
			KolmogorovSmirnovTestTwoSamples ksTest = new KolmogorovSmirnovTestTwoSamples(empX, empY, this.confidence);
			while(!ksTest.testE1GeqE2()){
				pivotVar.updateLowerBound(pivotVar.getLB()+1, aCause);
				samplesX[0] = pivotVar.getLB();
				empX = new EmpiricalDist(samplesX);
				ksTest = new KolmogorovSmirnovTestTwoSamples(empX, empY, this.confidence);
			}
        }
    	
    	for(int i = 0; i < this.y.length; i++){
        	double[] samplesY = new double[this.y.length];
        	IntVar pivotVar = this.y[i];
        	int k = 0;
        	samplesY[k++] = pivotVar.getUB();
        	for(int j = 0; j < this.y.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesY[k++] = this.y[j].getLB();
        	}
        	EmpiricalDist empY = new EmpiricalDist(samplesY);
        	
        	double[] samplesX = new double[this.x.length];
        	for(int j = 0; j < this.x.length; j++){
        		samplesX[j] = this.x[j].getUB();
        	}
        	EmpiricalDist empX = new EmpiricalDist(samplesX);
        	
        	KolmogorovSmirnovTestTwoSamples ksTest = new KolmogorovSmirnovTestTwoSamples(empX, empY, this.confidence);
			while(!ksTest.testE1GeqE2()){
				pivotVar.updateUpperBound(pivotVar.getUB()-1, aCause);
				samplesY[0] = pivotVar.getUB();
				empY = new EmpiricalDist(samplesY);
				ksTest = new KolmogorovSmirnovTestTwoSamples(empX, empY, this.confidence);
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
