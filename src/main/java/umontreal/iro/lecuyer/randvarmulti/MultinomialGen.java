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

package umontreal.iro.lecuyer.randvarmulti;

import umontreal.iro.lecuyer.randvar.UniformGen;

/**
 * Multinomial random variates generator
 * 
 * @author Roberto Rossi
 *
 */

public class MultinomialGen extends RandomMultivariateGen {
	
	UniformGen rng;
	double p[];
	int N;
	
	/**
	 * Multinomial random variates generator constructor
	 * 
	 * @param rng generator of uniform random numbers
	 * @param p multinomial probabilities
	 * @param N multinomial trials
	 */
	public MultinomialGen(UniformGen rng, double p[], int N){
		this.rng = rng;
		this.p = p;
		this.N = N;
	}

	@Override
	public void nextPoint(double[] p) {
		// TODO Auto-generated method stub
		for(int j = 0; j < this.N; j++){
			double mass = this.rng.nextDouble();
			double cumulative = 0;
			for(int i = 0; i < this.p.length; i++){
				cumulative += this.p[i];
				if(cumulative > mass){
					p[i] += 1;
					break;
				}
			}
		}
	}
}