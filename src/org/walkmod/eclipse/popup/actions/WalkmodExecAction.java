package org.walkmod.eclipse.popup.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Event;
import org.walkmod.eclipse.launching.Activator;
import org.walkmod.eclipse.launching.LaunchingConstants;
import org.walkmod.eclipse.preferences.PreferenceConstants;

/**
 * Starts Tomcat on a specific JRE
 */
public class WalkmodExecAction extends Action implements IAction,
		IJavaLaunchConfigurationConstants {

	private String option = "apply";

	private IPath workingDir;

	public WalkmodExecAction(IVMInstall vm, IPath workingDir, String option) {
		super(vm.getName());
		this.option = option;
		this.workingDir = workingDir;
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		try {
			ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();

			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType("org.walkmod.eclipse.launching.launchConfigurationType");

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(
					null, "walkmod " + option);

			workingCopy
					.setAttribute(LaunchingConstants.SELECTED_OPTION, option);

			IPreferenceStore store = Activator.getDefault()
					.getPreferenceStore();
			if (store.getBoolean(PreferenceConstants.WALKMOD_IS_EMBEDDED)) {
				workingCopy.setAttribute(LaunchingConstants.INSTALL_DIR, "");
			} else {
				String walkmodHome = store
						.getString(PreferenceConstants.WALKMOD_EXTERNAL_HOME);
				workingCopy.setAttribute(LaunchingConstants.INSTALL_DIR,
						walkmodHome);
			}

			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					workingDir.toFile().getAbsolutePath());

			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					workingDir.toFile().getName());

			ILaunchConfiguration configuration = workingCopy.doSave();

			DebugUITools.launch(configuration, ILaunchManager.RUN_MODE);

		} catch (CoreException e) {
			RuntimeException re = new RuntimeException();
			re.setStackTrace(e.getStackTrace());
			throw re;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets
	 * .Event)
	 */
	public void runWithEvent(Event event) {
		run();
	}

}