package abs.backend.java.observing;

import abs.backend.java.lib.types.ABSValue;

/**
 * An empty implementation of the {@link TaskObserver} interface
 * 
 * @author Jan Schäfer
 */
public class EmptyTaskObserver implements TaskObserver {

    @Override
    public void taskStarted(TaskView task) {
    }

    @Override
    public void taskFinished(TaskView task) {
    }

    @Override
    public void taskBlockedOnFuture(TaskView task, FutView fut) {
    }

    @Override
    public void taskRunningAfterWaiting(TaskView view, FutView fut) {
    }

    @Override
    public void taskStep(TaskView task, String fileName, int line) {
    }

    @Override
    public void taskDeadlocked(TaskView task) {
    }

    @Override
    public void stackFrameCreated(TaskView task, TaskStackFrameView stackFrame) {
    }

    @Override
    public void localVariableChanged(TaskStackFrameView stackFrame, String name, ABSValue v) {
        
    }
}
