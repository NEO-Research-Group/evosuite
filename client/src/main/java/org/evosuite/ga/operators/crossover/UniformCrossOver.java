/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.operators.crossover;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.utils.Randomness;

/**
 * Implement uniform crossover. In a uniform crossover, we do not divide the
 * chromosome into segments, rather we treat each gene separately. In this,
 * we essentially flip a coin for each chromosome.
 *
 * @author Yan Ge
 */
public class UniformCrossOver<T extends Chromosome<T>> extends CrossOverFunction<T> {

	private static final long serialVersionUID = 2981387570766261795L;

	/**
	 * {@inheritDoc}
	 * @param parent1
	 * @param parent2
	 */
	@Override
	public void crossOver(T parent1, T parent2)
			throws ConstructionFailedException {

		if (parent1.size() < 2 || parent2.size() < 2) {
			return;
		}

		int maxNumGenes = Math.min(parent1.size(), parent2.size());

		T t1 = parent1.clone();
		T t2 = parent2.clone();

		for (int i = 0; i < maxNumGenes; i++) {
			if (Randomness.nextDouble() <= Properties.CROSSOVER_RATE) {
				parent1.crossOver(t2, i);
				parent2.crossOver(t1, i);
			}
		}
	}
}

