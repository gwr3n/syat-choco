#author: Pierre Schaus pschaus@gmail.com
#modified by: Gilles Pesant gilles.pesant@polymtl.ca

generate<-function(filename){

	maxacuity = 105
	maxcap = 3
	mincap = 1

	getZone<-function(){
		getAcuity <-function(z){
			x = rbinom(1,8,0.23)
			a = runif(1,10*(x+1),10*(x+1)+10)
			return(floor(a))
		}
		n = rpois(1,3.8)+10 #offset by 5 or 10
		patients = rep(1,n)
		res = tapply(patients,1:n,FUN=getAcuity)
		res = as.vector(res)
		acuity = sort(res,decreasing=T)

		totPatients = rep(0,n)
		totAcuity = rep(0,n)
		for(i in 1:n){
			for(nurse in 1:n){
				if(totPatients[nurse]<maxcap && totAcuity[nurse]+acuity[i]<=maxacuity){
					totPatients[nurse] = totPatients[nurse]+1
					totAcuity[nurse] = totAcuity[nurse]+acuity[i]
					break	
				}
			}
		}
		nbNurses = sum(totPatients>0)
		res = list()
		res$nbNurses = nbNurses
		res$acuity = acuity
		return(res)
	}

	nbGroups = 15
	totNurse = 0
	groups = list()
	for(i in 1:nbGroups){
		group = getZone()
		totNurse = totNurse+group$nbNurses
		group$acuity=c(length(group$acuity),group$acuity)
		groups[[i]]=group
	}
	write(c(nbGroups,totNurse),file=filename)
	write(c(mincap,maxcap,maxacuity),file=filename,append=T)
	for(i in 1:nbGroups){
		write(groups[[i]]$acuity,file=filename,ncolumns=length(groups[[i]]$acuity),append=T)
	}
	print(totNurse)
}

#for(i in 0:9){
#	a=paste("instance",i,".txt",sep="")
#	generate(a)
#}

generate("15zones.txt");
