/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.solver.constraints.statistical.sum;

import java.util.Arrays;

import gnu.trove.map.hash.THashMap;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.syat.statistics.KolmogorovSmirnovTest;
import org.syat.statistics.KolmogorovSmirnovTestTwoSamples;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;


/**
 * A propagator for SUM(x_i) = b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p/>
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 use I in filterOn{G,L}eg
 * @since 18/03/11
 */
public class PropScalarEqSt extends Propagator<IntVar> {

    final int[][] samples; // list of coefficients
    final int[] b; // bound to respect
    final double confidence;
    int sumLB, sumUB; // sum of lower bounds, and sum of upper bounds


    protected static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 1) {
            return PropagatorPriority.UNARY;
        } else if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }

    public PropScalarEqSt(IntVar[] variables, int[][] samples, int[] b, double confidence) {
        super(variables, computePriority(variables.length), false);
        this.samples = samples;
        this.b = b;
        this.confidence = confidence;
    }

    protected void prepare() {
        
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter(true, 2);
    }

    protected void filter(boolean startWithLeq, int nbRules) throws ContradictionException {
        prepare();
        boolean run = false;
        int nbR = 0;
        do {
            if (startWithLeq) {
                run = filterOnLeq();
            } else {
                run = filterOnGeq();
            }
            startWithLeq ^= true;
            nbR++;
        } while (run || nbR < nbRules);
    }


    @SuppressWarnings({"NullableProblems"})
    boolean filterOnLeq() throws ContradictionException {
        boolean anychange = false;
        double[] pivotUB = new double[samples.length];
        double[] rhs = new double[samples.length];
        for(int i = 0; i < this.b.length; i++) rhs[i] = this.b[i];
        // positive coefficients first
        for (int i = 0; i < this.vars.length; i++) {
            IntVar pivotVar = vars[i];        	
            KolmogorovSmirnovTestTwoSamples ksTest = null;
			do{
				int value = pivotVar.getUB();
				for(int k = 0; k < this.b.length; k++) rhs[k] = this.b[k];
	            for(int j = 0; j < this.samples.length; j++) {
	            	pivotUB[j] = value*this.samples[j][i];
	            	
	            	for (int k = 0; k < this.vars.length; k++) {
	                	if(k == i) continue;
	                	pivotUB[j] += vars[k].getLB()*this.samples[j][k];
	                }
	            }
	            EmpiricalDist empPivot = new EmpiricalDist(pivotUB);
	            EmpiricalDist empRHS = new EmpiricalDist(rhs);
	            ksTest = new KolmogorovSmirnovTestTwoSamples(empPivot, empRHS, this.confidence);
	            if(!ksTest.testE2GeqE1()) 
	            	anychange = pivotVar.updateUpperBound(pivotVar.getUB()-1, aCause);
			}while(!ksTest.testE2GeqE1());
        }
        // then negative ones
        /*for (; i < l; i++) {
            if (I[i] - (b - sumLB) > 0) {
                lb = vars[i].getUB() * c[i];
                ub = lb + I[i];
                if (vars[i].updateLowerBound(divCeil(-(b - sumLB + lb), -c[i]), aCause)) {
                    int nub = vars[i].getLB() * c[i];
                    sumUB -= ub - nub;
                    I[i] = nub - lb;
                    anychange = true;
                }
            }
        }*/
        return anychange;
    }

    @SuppressWarnings({"NullableProblems"})
    boolean filterOnGeq() throws ContradictionException {
    	boolean anychange = false;
        double[] pivotLB = new double[samples.length];
        double[] rhs = new double[samples.length];
        for(int i = 0; i < this.b.length; i++) rhs[i] = this.b[i];
        // positive coefficients first
        for (int i = 0; i < this.vars.length; i++) {
            IntVar pivotVar = vars[i];      
            for(int k = 0; k < this.b.length; k++) rhs[k] = this.b[k];
            KolmogorovSmirnovTestTwoSamples ksTest = null;
			do{
				int value = pivotVar.getLB();
	            for(int j = 0; j < this.samples.length; j++) {
	            	pivotLB[j] = value*this.samples[j][i];
	            	
	            	for (int k = 0; k < this.vars.length; k++) {
	                	if(k == i) continue;
	                	pivotLB[j] += vars[k].getUB()*this.samples[j][k];
	                }
	            }
	            EmpiricalDist empPivot = new EmpiricalDist(pivotLB);
	            EmpiricalDist empRHS = new EmpiricalDist(rhs);
	            ksTest = new KolmogorovSmirnovTestTwoSamples(empPivot, empRHS, this.confidence);
	            if(!ksTest.testE1GeqE2())
	            	anychange = pivotVar.updateLowerBound(pivotVar.getLB()+1, aCause);
			}while(!ksTest.testE1GeqE2());
        }
        // then negative ones
        /*for (; i < l; i++) {
            if (I[i] > -(b - sumUB)) {
                ub = vars[i].getLB() * c[i];
                lb = ub - I[i];
                if (vars[i].updateUpperBound(divFloor(-(b - sumUB + ub), -c[i]), aCause)) {
                    int nlb = vars[i].getUB() * c[i];
                    sumLB += nlb - lb;
                    I[i] = ub - nlb;
                    anychange = true;
                }
            }
        }*/
        return anychange;
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        filter(true, 2);
//        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
    	return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask();
    }

    @Override
    public final ESat isEntailed() {
    	return ESat.UNDEFINED;
    }

    protected ESat compare(int sumLB, int sumUB) {
    	return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "not implemented";
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
