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
package de.ovgu.featureide.fm.ui.editors.featuremodel.actions;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.ovgu.featureide.fm.core.Constraint;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.ui.editors.featuremodel.editparts.ConstraintEditPart;
import de.ovgu.featureide.fm.ui.editors.featuremodel.editparts.FeatureEditPart;

/**
 * Action to send a selection request when ModelEditParts get selected.
 * 
 * @author Cyrill Meyer
 * @author Eric Schubert
 */
public class SelectionAction extends Action {

	private ISelectionChangedListener listener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event
					.getSelection();
			if (isSelectionValid(selection)){				
				if (selection.getFirstElement() instanceof ConstraintEditPart) {
					for (Constraint constraint : model.getConstraints()) {
						constraint.setFeatureSelected(false);
					}
					
					((ConstraintEditPart) selection.getFirstElement()).performRequest(new Request(RequestConstants.REQ_SELECTION));
					
					
				} else  if (selection.getFirstElement() instanceof FeatureEditPart){
					for (Feature feature : model.getFeatures()) {
					feature.setConstraintSelected(false);
					}
					
					((FeatureEditPart) selection.getFirstElement()).performRequest(new Request(RequestConstants.REQ_SELECTION));
					
				} else {
					for (Feature feature : model.getFeatures()) {
						feature.setConstraintSelected(false);
					}
					
					for (Constraint constraint : model.getConstraints()) {
						constraint.setFeatureSelected(false);
					}
				}
			} else {
				for (Feature feature : model.getFeatures()) {
					feature.setConstraintSelected(false);
				}
				
				for (Constraint constraint : model.getConstraints()) {
					constraint.setFeatureSelected(false);
				}
			}
			
		}
	};
	
	private GraphicalViewerImpl view;
	
	private FeatureModel model;
	
	public SelectionAction (GraphicalViewerImpl viewer, FeatureModel featureModel){
		super("Selection");
		this.view = viewer;
		this.model = featureModel;
		
		view.addSelectionChangedListener(listener);
	}
	
	public boolean isSelectionValid(IStructuredSelection selection){
		return selection.size() == 1;
	}
	
	
	
	
}