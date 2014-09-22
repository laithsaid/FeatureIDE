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
package de.ovgu.featureide.ui.views.collaboration.action;

import org.eclipse.jface.action.Action;

import de.ovgu.featureide.ui.views.collaboration.CollaborationView;
import de.ovgu.featureide.ui.views.collaboration.model.CollaborationModelBuilder;

/**
 * TODO description
 * 
 * @author "Andy Kenner"
 */
public class ShowEmptyRolesAction extends Action {
	
	private CollaborationView collaborationView;
	public ShowEmptyRolesAction(String text, CollaborationView collaborationView) {
		super(text);
		this.collaborationView = collaborationView;
	}

	public void setEnabled(boolean enabled) {
		// TODO  XYZ - Andy
		super.setChecked(CollaborationModelBuilder.showEmptyRoles());
		super.setEnabled(true);
	}
	
	public void run() {
		// TODO: do new action in here 
		CollaborationModelBuilder.showEmptyRoles(!CollaborationModelBuilder.showEmptyRoles());
		collaborationView.refresh();
	}
}
