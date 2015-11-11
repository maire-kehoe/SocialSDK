/*
 * © Copyright IBM Corp. 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ibm.sbt.provisioning.sample.app;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import com.ibm.sbt.provisioning.sample.app.model.Rest;
import com.ibm.sbt.provisioning.sample.app.model.Weight;
import com.ibm.sbt.provisioning.sample.app.model.Weights;
import com.ibm.sbt.provisioning.sample.app.task.BSSProvisioning;
import com.ibm.sbt.provisioning.sample.app.util.ShutdownCommand;

/**
 * This class represents a singleton responsible for managing the current weight associated with the organization .
 * */
public class WeightManager {

	private static final Logger logger = Logger.getLogger(WeightManager.class.getName());
	
	private static WeightManager instance = null;
	private static ShutdownCommand command;
	/**
	 * An int keeping track of the load generated by a customer towards the BSS API 
	 * while provisioning its subscribers
	 * */
	private int currentWeight = 0 ;
	/**
	 * A boolean that evaluates to true when the load {@link #threshold} for the customer in input has been reached 
	 * */
	private boolean thresholdReached ;
	/**
	 * The amount of time in millisecond that separates each {@link #currentWeight} reset operation
	 * */
	private long resetDuration ;
	private long maxSystemWeight ;
	/**
	 *  An int representing the load threshold for a customer
	 * */
	private long threshold ;
	
	protected ScheduledExecutorService resetterExec;
	/**
	 * <code>Executor</code> needed for periodically submit he {@link com.ibm.sbt.provisioning.sample.app.task.SubscriberTask} 
	 * instances to the provisioning  threadpool ( {@link com.ibm.sbt.provisioning.sample.app.task.BSSProvisioning#getThreadPool()} ) .
	 * */
	protected ScheduledExecutorService bssProvExec;
	
	public static synchronized WeightManager getInstance() {
		if(instance == null) {
			instance = new WeightManager(BSSProvisioning.getWeights());
		}
        return instance;
    }

	/**
	 * Allows to set an arbitrary command to be executed at shutdown time
	 * @param shutdownCommand
	 */
	public static void setCommand(ShutdownCommand shutdownCommand){
		command = shutdownCommand;
	}

	/**
	 * Constructor responsible for the {@link #resetterExec} and {@link #bssProvExec} <code>Executor</code>(s)
	 * initialization, for the weights json input file parsing and loading of its content in the {@link #weightPerBSSCall}
	 * <code>Map</code>, and initialization of the {@link #resetDuration}, {@link #maxSystemWeight} and
	 * {@link #threshold} fields
	 * */
	private WeightManager(Weights weights){
		this.resetterExec = Executors.newSingleThreadScheduledExecutor();
		this.bssProvExec = Executors.newSingleThreadScheduledExecutor();
    	this.threshold = weights.getLimit();
		
		// number of seconds needed for scheduling the BSSProvisioning.getSubscribersTasks() queue +
		// ( approximate time for the initial customer and subscription creation and activation + 30sec )
		long firstBSSProvisioningIt = (((long)BSSProvisioning.getSubscribersQuantity().get())/4L)*1000L + 60000L ;
		this.resetDuration = weights.getResetDuration();
		this.resetterExec.scheduleAtFixedRate( new Resetter(), this.resetDuration , this.resetDuration, TimeUnit.MILLISECONDS );
		this.bssProvExec.scheduleAtFixedRate( new BSSProvisioning(), firstBSSProvisioningIt , 30000L, TimeUnit.MILLISECONDS );
	}
	
	/**
	 * This class represents a task that will periodically executed every
	 * {@link #resetDuration} millisecond and when executed it will reset the {@link #currentWeight}
	 * */
	class Resetter implements Runnable {
		/**
		 * Business logic of the task
		 * <p>
		 * It will simply call the {@link #resetCurrentWeight} method
		 * */
		@Override
		public void run(){
			Thread.currentThread().setName("Resetter");
			logger.finest("Resetting current weight...");
			WeightManager.this.resetCurrentWeight();
		}
	}

	/**
	 * This <code>synchronized</code> method simply reset the {@link #currentWeight} and 
	 * set the {@link #thresholdReached} value to <code>false</code>
	 * */
	private synchronized void resetCurrentWeight(){
		this.currentWeight = 0 ;
		WeightManager.this.thresholdReached = false ;
	}
	
	/**
	 * This method will update the {@link #currentWeight} associated with the organization depending
	 * on the call being made.
	 * <p>
	 * 
	 * @param url
	 * @param method
	 * @return <code>true</code> if the call is permitted because the {@link #threshold} has not been
	 *         reached after the {@link #currentWeight} update , <code>false</code> otherwise
	 */
	public synchronized boolean updateCurrentWeight(String url, Rest method){
		boolean callPermitted = true ;
		if( !thresholdReached ){
			logger.finest("currentWeight = " + this.currentWeight);
      		this.currentWeight = this.currentWeight + getWeightValuePerBSSCall(url, method);
			logger.finest("currentWeight updated = " + this.currentWeight);
			if( currentWeight >= threshold ){
				callPermitted = false ;
				logger.warning("THRESHOLD REACHED OR EXCEEDED !!!");
				this.thresholdReached = true ;
			}else{
        		this.incrementCounterPerBSSCall(url, method, 1);
			}
		}else{
			callPermitted = false ;
		}
		return callPermitted ;
	}

	/**
	 * {@link #resetDuration} getter method
	 */
	public long getResetDuration() {
		return resetDuration;
	}

	/**
	 * {@link #maxSystemWeight} getter method
	 */
	public long getMaxSystemWeight() {
		return maxSystemWeight;
	}

	/**
	 * {@link #threshold} getter method
	 */
	public long getThreshold() {
		return threshold;
	}

	/**
	 * {@link #thresholdReached} getter method
	 */
	public synchronized boolean isThresholdReached() {
		return thresholdReached;
	}
	
	/**
	 * Shuts down the ExecutorServices
	 */
	public void shutdown(){
		if(!this.resetterExec.isShutdown()){
			this.resetterExec.shutdownNow();
		}
		if(!this.bssProvExec.isShutdown()){
			this.bssProvExec.shutdownNow();
		}
		if(!BSSProvisioning.getThreadPool().isShutdown()){
			BSSProvisioning.getThreadPool().shutdown();
		}
		if(command != null){
			command.execute();
		}
	}
	
	/**
	 * weightPerBSSCall getter method
	 */
	public Weight getWeightPerBSSCall(String url, Rest method){
		return BSSProvisioning.getWeights().getWeight(url, method);
	}

	public int getWeightValuePerBSSCall(String url, Rest method){
		return BSSProvisioning.getWeights().getWeightValue(url, method);
	}

	public int incrementCounterPerBSSCall(String url, Rest method, int amount){
		return BSSProvisioning.getWeights().incrementCounter(url, method, amount);
	}

}
