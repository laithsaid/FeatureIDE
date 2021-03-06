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
package de.ovgu.featureide.fm.core.io.sxfm;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;

import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.core.io.IFeatureModelReader;
import de.ovgu.featureide.fm.core.io.IFeatureModelWriter;
import de.ovgu.featureide.fm.core.io.TAbstractFeatureModelReaderWriter;
import de.ovgu.featureide.fm.core.io.UnsupportedModelException;

/**
 * Test class for SXFM reader and writer
 * 
 * @author Fabian Benduhn
 */
public class TSXFMReaderWriter extends TAbstractFeatureModelReaderWriter{

	/**
	 * @param file
	 * @throws UnsupportedModelException 
	 */
	public TSXFMReaderWriter(FeatureModel fm, String s) throws UnsupportedModelException {
		super(fm,s);
	}

	/* (non-Javadoc)
	 * @see de.ovgu.featureide.fm.core.io.TAbstractFeatureModelReaderWriter#getWriter(de.ovgu.featureide.fm.core.FeatureModel)
	 */
	@Override
	protected IFeatureModelWriter getWriter(FeatureModel fm) {
		return new SXFMWriter(fm);
	}

	/* (non-Javadoc)
	 * @see de.ovgu.featureide.fm.core.io.TAbstractFeatureModelReaderWriter#getReader(de.ovgu.featureide.fm.core.FeatureModel)
	 */
	@Override
	protected IFeatureModelReader getReader(FeatureModel fm) {
		return new SXFMReader(fm);
	}

	
	@Override
	public void testFeatureHidden(){
		
	}
	
	@Test
	public void testPropNodes() {
		for (Node n : newFm.getPropositionalNodes()) {
			if (n instanceof Literal) {
				// case: feature
				continue;
			}
			if (n instanceof Not) {
				// case: ~feature
				if (n.getChildren().length == 1 && n.getChildren()[0] instanceof Literal) {
					continue;
				}
			}
			// case: feature1 or feature2 or feature3 ...
			assertTrue(n + " is no Or Node",n instanceof Or);
			isCnf(n);
		}
	}
	
	private void isCnf(Node node) {
		for (Node n : node.getChildren()) {
			if (n instanceof Not) {
				assertTrue("Not statement has to much children", n.getChildren().length == 1);
				assertTrue(n + "is not a Literal after Not",n.getChildren()[0] instanceof Literal);
			} else if (n instanceof Or) { 
				isCnf(n);
			} else {
				assertTrue(n + " is no Literal",n instanceof Literal);
			}
		}
	}
	
	@Override
	public void testDescription() {
		// description not implemented
	}
}
