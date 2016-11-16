package org.chocosolver.samples.statistical.rng;

/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.slf4j.LoggerFactory;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.NormalDist;
import org.chocosolver.util.ESat;

/**
 * @author Gregy4
 */
public class RandomNumberGeneration extends AbstractProblem {

	public static int REPLICATIONS = 10;
	public static int SAMPLES = 10;
	public static int LAMBDA = 50;
	
	IntVar[][] intervalsH;
	IntVar[][] intervalsV;
	IntVar[] intervals;

	@Override
	public void createSolver(){
		solver = new Solver("schedule");
	}

	@Override
	public void buildModel(){
		intervalsH = new IntVar[SAMPLES][REPLICATIONS];
		intervalsV = new IntVar[REPLICATIONS][SAMPLES];
		intervals = new IntVar[SAMPLES*REPLICATIONS];
		for(int i = 0; i < SAMPLES; i++){
			for(int j = 0; j < REPLICATIONS; j++){
				intervalsH[i][j] = VariableFactory.bounded("Observation " + i + " " + j , 0, 1000, solver);
				intervalsV[j][i] = intervalsH[i][j];
				intervals[i*REPLICATIONS+j] = intervalsH[i][j];
			}
			Distribution dist = new NormalDist(LAMBDA,Math.sqrt(LAMBDA));
			if(i > 0) solver.post(IntConstraintFactorySt.arithmSt(intervalsH[i], dist, "=", 0.6));
		}
		Distribution dist = new NormalDist(LAMBDA,Math.sqrt(LAMBDA));
		solver.post(IntConstraintFactorySt.arithmSt(intervals, dist, "=", 0.6));
		for(int j = 0; j < REPLICATIONS; j++){
			if(j > 0) solver.post(IntConstraintFactorySt.arithmSt(intervalsV[j], dist, "=", 0.6));
		}
	}

	@Override
	public void configureSearch(){
		IntVar[] intervalsArray = new IntVar[SAMPLES*REPLICATIONS];
		for(int i = 0; i < SAMPLES; i++){
			System.arraycopy(intervalsH[i], 0, intervalsArray,i*(REPLICATIONS), intervalsH[i].length);
		}
		
		solver.set(IntStrategyFactory.impact(intervalsArray,2211));
	}

	@Override
	public void solve(){
		solver.findSolution();
		//solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,maxEnd);
	}

	@Override
	public void prettyOut(){
		if (solver.isFeasible() == ESat.TRUE) {
			StringBuilder schedule = new StringBuilder();
			
			StringBuilder st = new StringBuilder();
			for(int i = 0; i < SAMPLES; i++){
				for(int j = 0; j < REPLICATIONS; j++){
					st.append(intervalsH[i][j].getValue()+"\t");
				}
				st.append("\n");
			}
	        LoggerFactory.getLogger("bench").info(st.toString());
	        LoggerFactory.getLogger("bench").info("\n");
	        LoggerFactory.getLogger("bench").info(schedule.toString());
		}
	}

	public static void main(String[] args){
	   @SuppressWarnings("unused")
		String[] str={"-log","SEARCH"};
		new RandomNumberGeneration().execute();
	}
}