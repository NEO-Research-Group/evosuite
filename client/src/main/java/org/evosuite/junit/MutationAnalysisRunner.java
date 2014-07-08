package org.evosuite.junit;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.testcase.ExecutionTracer;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutationAnalysisRunner extends BlockJUnit4ClassRunner {

	private static final Logger logger = LoggerFactory.getLogger(MutationAnalysisRunner.class);
	
	private Set<Mutation> killedMutants = new LinkedHashSet<Mutation>();

	private Set<Mutation> liveMutants;

	public MutationAnalysisRunner(Class<?> klass, Collection<Mutation> allMutants) throws InitializationError {
		super(klass);
		this.liveMutants = new LinkedHashSet<Mutation>(allMutants);
	}
	
	public MutationAnalysisRunner(Class<?> klass) throws InitializationError {
		this(klass, MutationPool.getMutants());
	}
	
	public Set<Mutation> getLiveMutants() {
		return liveMutants;
	}
	
	public Set<Mutation> getKilledMutants() {
		return killedMutants;
	}

	private static class SimpleRunListener extends RunListener {
		public boolean hasFailure = false;
		public Failure lastFailure = null;
		@Override
		public void testFailure(Failure failure) throws Exception {
			hasFailure = true;
			lastFailure = failure;
			super.testFailure(failure);
		}		
	}
	
	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		logger.info("Running method "+method.getName());
		SimpleRunListener resultListener = new SimpleRunListener();
		notifier.addListener(resultListener);
		
		// First run without mutants
		ExecutionTracer.enable();
		super.runChild(method, notifier);
		boolean result = resultListener.hasFailure;
		logger.info("Result without mutant: "+result);
		if(result) {
			logger.info("Failure: "+resultListener.lastFailure.getMessage());
		}
		
		Set<Integer> touchedMutants = ExecutionTracer.getExecutionTracer().getTrace().getTouchedMutants();
		logger.info("Touched mutants: "+touchedMutants.size());
		// Now run it for all touched mutants
		for(Integer mutantID : touchedMutants) {
			// logger.info("Current mutant: "+mutantID);
			Mutation m = MutationPool.getMutant(mutantID);
			if(killedMutants.contains(m)) {
				// logger.info("Already dead: "+mutantID);
				continue;
			}

			ExecutionTracer.getExecutionTracer().clear();
			resultListener.hasFailure = false;
			MutationObserver.activateMutation(m);
			super.runChild(method, notifier);
			MutationObserver.deactivateMutation(m);
			
			// If killed
			if(resultListener.hasFailure != result) {
				logger.info("Now killed: "+mutantID);
				try {
				liveMutants.remove(m);
				} catch(Throwable t) {
					logger.info("Error: "+t);
					t.printStackTrace();
				}
				try {
				killedMutants.add(m);
				} catch(Throwable t) {
					logger.info("Error: "+t);
					t.printStackTrace();
				}
				
			//} else {
			//	logger.info("Remains live: "+mutantID);
			}
		}
		notifier.removeListener(resultListener);
		logger.info("Done with "+method.getName());
	}
}