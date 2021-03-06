/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2011  FeatureIDE Team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.core.typecheck;

import java.util.ArrayList;
import java.util.List;

import de.ovgu.featureide.core.typecheck.check.ICheckPlugin;
import de.ovgu.featureide.core.typecheck.check.MethodCheck;
import de.ovgu.featureide.fm.core.FeatureModel;

/**
 * TODO description
 * 
 * @author soenke
 */
public class TypeCheckerCLI {
    public static void main(String[] args) {
	if (args.length != 2) {
	    return;
	}
	String fmfile = args[0];
	String source_path = args[1];

	List<ICheckPlugin> plugins = new ArrayList<ICheckPlugin>();
	plugins.add(new MethodCheck());
	// plugins.add(new TypeCheck());
	// plugins.add(new FieldCheck());

	TypeChecker checker = new TypeChecker(TypeChecker.defaultCheckPlugins(),
		TypeChecker.defaultProblemHandlers());

	checker.log("Reading feature model from file " + fmfile);
	FeatureModel fm = checker.readFM(fmfile);
	checker.log("\tdone");

	checker.setParameters(fm, source_path);
	checker.run();
    }
}
