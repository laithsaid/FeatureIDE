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
package de.ovgu.featureide.fm.core.job.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.core.runtime.jobs.Job;

import de.ovgu.featureide.fm.core.FMCorePlugin;
import de.ovgu.featureide.fm.core.job.IJob;

/**
 * Class for starting jobs.
 * {@link IJob}s in a specific {@link JobSequence} are executed consecutively.
 * {@link IJob}s in different {@link JobSequence}s are executed independent of each other.
 * </br>
 * It is possible to wait for a sequence to finish.
 * 
 * @author Sebastian Krieter
 */
public final class JobSequence implements IJob {
	private final LinkedList<IJob> jobs = new LinkedList<IJob>();
	
	private LinkedList<JobFinishListener> jobFinishedListeners = null;
	private boolean ignorePreviousJobFail = true;
	private JobStatus status = JobStatus.NOT_STARTED;
		
	/**
	 * Adds a new job to the sequence if it has not already finished
	 * @param newJob the job to add
	 * @return {@code true} if job was added to the sequence, {@code false} otherwise
	 */
	public boolean addJob(IJob newJob) {
		synchronized (this) {
			if (status == JobStatus.NOT_STARTED || status == JobStatus.RUNNING) {
				newJob.addJobFinishedListener(new JobFinishListener() {
					@Override
					public void jobFinished(boolean success) {
						JobSequence.this.startNextJob();
					}
				});
				jobs.add(newJob);
				return true;
			} else {
				return false;
			}
		}
	}
	
	public void addJobFinishedListener(JobFinishListener listener) {
		if (jobFinishedListeners == null) {
			jobFinishedListeners = new LinkedList<JobFinishListener>();
		}
		jobFinishedListeners.add(listener);
	}
	
	public boolean cancel() {
		synchronized (this) {
			status = JobStatus.FAILED;
			if (jobs.isEmpty()) {
				return true;
			}
			final IJob curJob = jobs.getFirst();
			jobs.clear();
			return curJob.cancel();
		}
	}
	
	public JobStatus getStatus() {
		return status;
	}
	
	/**
	 * @return
	 */
	public boolean ignoresPreviousJobFail() {
		return ignorePreviousJobFail;
	}
	
	public void insertJobs(IJob lastJob, Collection<IJob> newJobs) {
		synchronized (this) {
			for (ListIterator<IJob> it = jobs.listIterator(); it.hasNext();) {
				if (it.next().equals(lastJob)) {
					for (IJob newJob : newJobs) {
						newJob.addJobFinishedListener(new JobFinishListener() {
							@Override
							public void jobFinished(boolean success) {
								JobSequence.this.startNextJob();
							}
						});
						it.add(newJob);
					}
					break;
				}
			}
		}
	}
	
	public void removeJobFinishedListener(JobFinishListener listener) {
		if (jobFinishedListeners != null) {
			jobFinishedListeners.remove(listener);
		}
	}
	
	@Override
	public void schedule() {
		final IJob firstJob = jobs.peekFirst();
		if (firstJob != null) {
			synchronized (this) {
				if (status == JobStatus.NOT_STARTED) {
					status = JobStatus.RUNNING;
					firstJob.schedule();
				}
			}
		}
	}
	
	/**
	 * If a job in this sequence fails to do its work all subsequent jobs are canceled.
	 * @param ignorePreviousJobFail
	 */
	public void setIgnorePreviousJobFail(boolean ignorePreviousJobFail) {
		this.ignorePreviousJobFail = ignorePreviousJobFail;
	}
	
	private void startNextJob() {
		synchronized (this) {
			final IJob lastJob = jobs.poll();
			if (lastJob != null) {
				JobStatus lastStatus = lastJob.getStatus();
				IJob nextJob = null;

				for (final Iterator<IJob> it = jobs.iterator(); it.hasNext();) {
					nextJob = it.next();
					if (nextJob.getStatus() == JobStatus.FAILED) {
						lastStatus = JobStatus.FAILED;
						it.remove();
					} else if (lastStatus == JobStatus.FAILED && !ignoresPreviousJobFail()) {
						it.remove();
					} else {
						break;
					}
				}
				if (jobs.isEmpty()) {
					if (jobFinishedListeners != null) {
						for (final Iterator<JobFinishListener> it = jobFinishedListeners.iterator(); it.hasNext();) {
						    try {
						    	it.next().jobFinished(lastStatus == JobStatus.OK);
						    }
						    catch (RuntimeException e) {
						    	FMCorePlugin.getDefault().logError(e);
						    }
						}
					}
					status = JobStatus.OK;
				} else {
					nextJob.schedule();
				}
			}
		}
	}
}