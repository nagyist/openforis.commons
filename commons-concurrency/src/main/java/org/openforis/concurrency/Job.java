package org.openforis.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.openforis.concurrency.Worker.Status;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Synchronously executes a series of Tasks in order.
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class Job extends Worker implements Iterable<Task> {
	
	@Autowired
	private transient JobManager jobManager;

	private int currentTaskIndex;

	private List<Task> tasks;
	
	protected Job() {
		this.currentTaskIndex = -1;
		this.tasks = new ArrayList<Task>();
	}

	/**
	 * Builds all the tasks. Each task will be initialized before running it.
	 * @throws Throwable 
	 */
	@Override
	protected void initInternal() throws Throwable {
		buildAndAddTasks();
	}
	
	@Override
	public int getProgressPercent() {
		if ( getStatus() == Status.RUNNING ) {
			int currentTaskProgress = getCurrentTask().getProgressPercent();
			return 100 * ( currentTaskIndex + currentTaskProgress  / 100 ) / tasks.size();
		} else {
			return 0;
		}
	}
	
	@Override
	public synchronized void run() {
		log().debug("Starting job");
		super.run();
		log().debug(String.format("Finished in %.1f sec", getDuration() / 1000f));
	}
	
	/**
	 * Runs each contained task in order.
	 * 
	 * @throws Exception
	 */
	protected void execute() throws Throwable {
		this.currentTaskIndex = -1;
		while ( hasTaskToRun() ) {
			Task task = nextTask();
			
			prepareTask(task);
			
			if ( task.isPending() ) {
				runTask(task);
			} else {
				setErrorMessage(task.getErrorMessage());
				setLastException(task.getLastException());
				changeStatus(Status.FAILED);
			}
		}
	}

	protected void runTask(Task task) throws Throwable {
		try {
			task.run();

			switch ( task.getStatus() ) {
			case COMPLETED:
				onTaskCompleted(task);
				break;
			case FAILED:
				if ( task.getLastException() != null ) {
					throw task.getLastException();
				} else {
					this.changeStatus(Status.FAILED);
				}
				break;
			case ABORTED:
				abort();
				break;
			default:
			}
		} finally {
			onTaskEnd(task);
		}
	}

	protected abstract void buildAndAddTasks() throws Throwable;

	protected <T extends Task> T createTask(Class<T> type) {
		T task = jobManager.createTask(type);
		return task;
	}
	
	protected boolean hasTaskToRun() {
		return isRunning() && currentTaskIndex + 1 < tasks.size();
	}

	protected Task nextTask() {
		this.currentTaskIndex ++;
		return tasks.get(currentTaskIndex);
	}

	/**
	 * Creates and adds a task of the specified type.
	 * @param type
	 * @return
	 */
	protected <T extends Task> T addTask(Class<T> type) {
		T task = createTask(type);
		addTask(task);
		return task;
	}
	
	/**
	 * Throws IllegalStateException if invoked after run() is called
	 * 
	 * @param task
	 */
	protected <T extends Task> void addTask(T task) {
		if ( !isPending() ) {
			throw new IllegalStateException("Cannot add tasks to a job once started");
		}
		tasks.add(task);
	}

	protected <C extends Collection<? extends Task>> void addTasks(C tasks) {
		for (Task task : tasks) {
			addTask(task);
		}
	}

	/**
	 * Called when the task ends its execution. The status can be {@link Status#COMPLETED}, {@link Status#FAILED}, {@link Status#ABORTED}
	 * @param task
	 */
	protected void onTaskEnd(Task task) {
		
	}

	/**
	 * Called when the task ends its execution with the status {@link Status#COMPLETED}
	 * @param task
	 */
	protected void onTaskCompleted(Task task) {
		onTaskEnd(task);
	}

	/**
	 * Called before task execution.
	 * @param task
	 */
	protected void prepareTask(Task task) {
		task.init();
	}
	
	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	public int getCurrentTaskIndex() {
		return this.currentTaskIndex;
	}

	public Task getCurrentTask() {
		return currentTaskIndex >= 0 ? tasks.get(currentTaskIndex) : null;
	}
	
	@Override
	public Iterator<Task> iterator() {
		return getTasks().iterator();
	}	

	public Task getTask(UUID taskId) {
		for (Task task : tasks) {
			if ( task.getId().equals(taskId) ) {
				return task;
			}
		}
		return null;
	}

	public JobManager getJobManager() {
		return jobManager;
	}
	
	protected void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}
}