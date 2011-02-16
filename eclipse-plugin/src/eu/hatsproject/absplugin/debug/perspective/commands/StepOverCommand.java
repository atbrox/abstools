package eu.hatsproject.absplugin.debug.perspective.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import static eu.hatsproject.absplugin.debug.DebugUtils.executeSingleStep;;

/**
 * Class handling the f6 shortcut for single steps
 * @author mweber
 */
public class StepOverCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(executeSingleStep.isEnabled()){
			executeSingleStep.run();
		}
		return null;
	}

}