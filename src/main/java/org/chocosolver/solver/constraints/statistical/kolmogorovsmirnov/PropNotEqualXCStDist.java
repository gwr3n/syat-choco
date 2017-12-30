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

package org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;

import org.chocosolver.util.iterators.DisposableValueIterator;
import org.syat.statistics.KolmogorovSmirnovTest;

@SuppressWarnings("serial")
class PropNotEqualXCStDist extends Propagator<IntVar> {

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
    				pivotVar.removeValue(value, this);
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
}


