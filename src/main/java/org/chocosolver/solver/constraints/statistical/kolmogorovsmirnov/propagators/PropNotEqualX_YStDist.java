/*
 * syat-choco: a Choco extension for Declarative Statistics.
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.syat.statistics.KolmogorovSmirnovTestTwoSamples;
import org.chocosolver.util.iterators.DisposableValueIterator;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;

@SuppressWarnings("serial")
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
    				pivotVar.removeValue(value, this);
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
    				pivotVar.removeValue(value, this);
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
}
