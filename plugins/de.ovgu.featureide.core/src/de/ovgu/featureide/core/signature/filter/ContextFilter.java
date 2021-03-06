/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2013  FeatureIDE team, University of Magdeburg, Germany
*
* This file is part of FeatureIDE.
* 
* FeatureIDE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* FeatureIDE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
*
* See http://www.fosd.de/featureide/ for further information.
*/
package de.ovgu.featureide.core.signature.filter;

import java.util.Arrays;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.SatSolver;
import org.sat4j.specs.TimeoutException;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.core.signature.ProjectSignatures;
import de.ovgu.featureide.core.signature.abstr.AbstractSignature;
import de.ovgu.featureide.core.signature.abstr.AbstractSignature.FeatureData;
import de.ovgu.featureide.fm.core.editing.NodeCreator;

public class ContextFilter implements ISignatureFilter {
	
	private final ProjectSignatures projectSignatures;
	private final Node fmNode;
	private final boolean[] selcetedFeatures;
	private SatSolver solver;
	
	public ContextFilter(String featureName, ProjectSignatures projectSignatures) {
		this(new Node[] {new Literal(featureName, true)}, projectSignatures);
	}
	
	public ContextFilter(Node[] constraints, ProjectSignatures projectSignatures) {
		this.projectSignatures = projectSignatures;
		fmNode = NodeCreator.createNodes(projectSignatures.getFeatureModel());
		selcetedFeatures = new boolean[projectSignatures.getFeatureModel().getNumberOfFeatures()];
		
		init(constraints);
	}
	
	public void init(String featureName) {
		init(new Node[] {new Literal(featureName, true)});
	}

	public void init(Node[] constraints) {
		Node[] fixClauses = new Node[constraints.length + 1];
		fixClauses[0] = fmNode;
		System.arraycopy(constraints, 0, fixClauses, 1, constraints.length);
		Arrays.fill(selcetedFeatures, false);
		
		solver = new SatSolver(new And(fixClauses), 2000);
		
		for (Literal literal : solver.knownValues()) {
			if (literal.positive) {
				int id = projectSignatures.getFeatureID(literal.var.toString());
				if (id > -1) {
					selcetedFeatures[id] = true;
				}
			}
		}
	}
	
	@Override
	public boolean isValid(AbstractSignature signature) {
		FeatureData[] ids = signature.getFeatureData();
		Node[] negativeLiterals = new Node[ids.length];
		for (int i = 0; i < ids.length; ++i) {
			int id = ids[i].getId();
			if (selcetedFeatures[id]) {
				return true;
			} 
			negativeLiterals[i] = new Literal(projectSignatures.getFeatureName(id), false);
		}
		try {
			return !solver.isSatisfiable(negativeLiterals);
		} catch (TimeoutException e) {
			CorePlugin.getDefault().logError(e);
			return false;
		}
	}

}
