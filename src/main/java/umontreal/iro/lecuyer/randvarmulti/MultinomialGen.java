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