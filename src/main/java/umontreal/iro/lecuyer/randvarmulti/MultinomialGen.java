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
import umontreal.iro.lecuyer.randvarmulti.RandomMultivariateGen;

public class MultinomialGen extends RandomMultivariateGen {
	
	UniformGen gen1;
	double p[];
	int N;
	
	public MultinomialGen(UniformGen gen1, double p[], int N){
		this.gen1 = gen1;
		this.p = p;
		this.N = N;
	}

	@Override
	public void nextPoint(double[] p) {
		// TODO Auto-generated method stub
		for(int j = 0; j < this.N; j++){
			double mass = this.gen1.nextDouble();
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