package com.zhao;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

public class MyHMM {
	private double[] Pi = {0.5, 0.0, 0.0, 0.5};//Pi
	private double[][] mixedMatrix=new double[4][7004];//B
	private double[][] transferMatrix=new double[4][4];//A
	/*
	 * create a HMM with 2 states and default parameter
	 */
	public void buildHmm(){
		
		OpdfIntegerFactory factory = new OpdfIntegerFactory(2);
		Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(2, factory);
		hmm.setPi(0, 0.95);
		hmm.setPi(1, 0.05);
	
		hmm.setOpdf(0, new OpdfInteger(new double[] { 0.95, 0.05 }));
		hmm.setOpdf(1, new OpdfInteger(new double[] { 0.2, 0.8 }));

		hmm.setOpdf(0, new OpdfInteger(new double[] { 0.95, 0.05 }));
		hmm.setOpdf(1, new OpdfInteger(new double[] { 0.2, 0.8 }));
	}
	//ÑµÁ·hmmÄ£ÐÍ
    private Hmm<ObservationInteger> buildHMM(){
        Hmm<ObservationInteger> hmm=new Hmm < ObservationInteger >(4 ,new OpdfIntegerFactory (7004) );
        int i,j;
        for( i=0;i<4;i++){
            hmm.setPi(i, Pi[i]);
        }
        for(i=0;i<4;i++){
            for(j=0;j<4;j++){
                hmm.setAij(i, j, transferMatrix[i][j]);
            }
            hmm.setOpdf(i, new OpdfInteger(mixedMatrix[i]));
        }
        return hmm;
    }
}
