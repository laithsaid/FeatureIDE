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
package de.ovgu.featureide.ui.statistics.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.progress.UIJob;

import de.ovgu.featureide.ui.statistics.core.composite.LazyParent;
import de.ovgu.featureide.ui.statistics.core.composite.Parent;

/**
 * TODO description 
 * 
 * @author Dominik Hamann
 * @author Haese Patrick
 */
public class CheckBoxListener implements ICheckStateListener {
	private CheckboxTreeViewer viewer;

	/**
	 * 
	 * 
	 * @param viewer
	 */
	public CheckBoxListener(final CheckboxTreeViewer viewer) {
		super();
		this.viewer = viewer;
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object[] sources = ((TreeSelection) event.getSelection())
						.toArray();
				for (Object source : sources) {
					if (source instanceof Parent) {
						Parent selected = (Parent) source;
						if (selected.hasChildren()) {
							if (selected instanceof LazyParent) {
								((LazyParent) selected).getChildren();
							}
							boolean allChecked = allChildrenChecked(selected);
							viewer.setChecked(selected, !allChecked);
							for (Parent child : selected.getChildren()) {
								viewer.setChecked(child, !allChecked);
							}
							viewer.setExpandedState(selected, true);
						} else {
							viewer.setChecked(selected, viewer.getChecked(selected));
						}
					}
				}
				refreshViewer();
			}
		});
		viewer.setCheckStateProvider(new ICheckStateProvider() {

			@Override
			public boolean isGrayed(Object element) {
				return viewer.getGrayed(element);
			}

			@Override
			public boolean isChecked(Object element) {
				return viewer.getChecked(element);
			}
		});
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		// Object element = event.getElement();
		// if(element instanceof Parent){
		// ((Parent) element).getParent();
		// }
		// refreshViewer();
	}

	// /**
	// * @param element
	// * @return true, if the element has at least one child-element, which is
	// * checked but not grayed. False otherwise.
	// */
	// private boolean hasCheckedChild(Object element) {
	// boolean hasCheckedChild = false;
	// if (element instanceof Parent) {
	// Parent parent = (Parent) element;
	// if (parent.hasChildren() && (!(parent instanceof LazyParent) || (parent
	// instanceof LazyParent && !((LazyParent) parent).isLazy()))) {
	// for (Parent child : parent.getChildren()) {
	// if (isOnlyChecked(child)) {
	// hasCheckedChild = true;
	// break;
	// }
	// }
	// }
	// }
	// return hasCheckedChild;
	// }

	/**
	 * @param element
	 * @return true if the element is checked but not grayed.
	 */
	private boolean isOnlyChecked(Object element) {
		return viewer.getChecked(element) && !viewer.getGrayed(element);
	}

	/**
	 * @param element
	 * @return
	 */
	private boolean allChildrenChecked(Object element) {
		boolean allChildrenChecked = true;
		for (Object o : ((Parent) element).getChildren()) {
			if (!viewer.getChecked(o)) {
				allChildrenChecked = false;
				break;
			}
		}
		return allChildrenChecked;
	}

	private void refreshViewer() {
		UIJob job = new UIJob("refresh dialog - treeviewer") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				viewer.refresh();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}