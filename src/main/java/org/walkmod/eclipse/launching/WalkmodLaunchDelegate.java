package org.walkmod.eclipse.launching;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.walkmod.eclipse.Activator;
import org.walkmod.eclipse.preferences.PreferenceConstants;

public class WalkmodLaunchDelegate extends JavaLaunchDelegate {

	

	@SuppressWarnings("restriction")
	@Override
	public IVMInstall verifyVMInstall(ILaunchConfiguration configuration)
			throws CoreException {
		IVMInstall vm = getVMInstall(configuration);
		if (vm == null) {
			abort(LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_for__0__does_not_exist___1__6,
					null,
					IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST);
		}
		File location = vm.getInstallLocation();
		if (!location.exists()) {
			abort(NLS.bind(
					LaunchingMessages.AbstractJavaLaunchConfigurationDelegate_JRE_home_directory_for__0__does_not_exist___1__6,
					new String[] { vm.getName(), location.getAbsolutePath() }),
					null,
					IJavaLaunchConfigurationConstants.ERR_VM_INSTALL_DOES_NOT_EXIST);
		}
		return vm;
	}

	public File getConfigDir(ILaunchConfiguration configuration)
			throws CoreException {
		URI configDirectoryURI = null;
		String installDir = configuration.getAttribute(
				LaunchingConstants.INSTALL_DIR, "");

		if (installDir == null || "".equals(installDir.trim()) || installDir.startsWith("EMBEDDED")) {

			URL configPath = Thread.currentThread().getContextClassLoader()
					.getResource("/org/walkmod/config");
			try {
				configDirectoryURI = FileLocator.resolve(configPath).toURI();
			} catch (Exception e) {
				throw new RuntimeException(
						"The system cannot resolve the config directory");
			}

		} else {
			if(installDir.startsWith("EXTERNAL (")){
				installDir = installDir.substring("EXTERNAL (".length(), installDir.lastIndexOf(")"));
			}
			configDirectoryURI = new File(installDir, "config").toURI();
		}
		return new File(configDirectoryURI);
	}

	public File getLibDir(ILaunchConfiguration configuration)
			throws CoreException {
		URI libDirectoryURI = null;
		String installDir = configuration.getAttribute(
				LaunchingConstants.INSTALL_DIR, "");

		if (installDir == null || "".equals(installDir.trim()) || installDir.startsWith("EMBEDDED")) {

			URL classPath = Thread.currentThread().getContextClassLoader()
					.getResource("/org/walkmod/lib");
			try {
				libDirectoryURI = FileLocator.resolve(classPath).toURI();
			} catch (Exception e) {
				throw new RuntimeException(
						"The system cannot resolve walkmod lib directory");
			}
		}

		else {
			if(installDir.startsWith("EXTERNAL (")){
				installDir = installDir.substring("EXTERNAL (".length(), installDir.lastIndexOf(")"));
			}
			libDirectoryURI = new File(installDir, "lib").toURI();
		}

		return new File(libDirectoryURI);

	}

	@Override
	public String[] getClasspath(ILaunchConfiguration configuration)
			throws CoreException {

		List<String> classPathEntries = new LinkedList<String>();

		File file = getLibDir(configuration);
		if (file.exists()) {
			File[] libs = file.listFiles();
			for (File lib : libs) {
				classPathEntries.add(lib.getAbsolutePath());
			}
		}

		File configDir = getConfigDir(configuration);
		if (configDir.exists()) {
			File[] configFiles = configDir.listFiles();
			for (File config : configFiles) {
				classPathEntries.add(config.getAbsolutePath());
			}
		}

		return classPathEntries.toArray(new String[classPathEntries.size()]);

	}

	/**
	 * Returns the main type name specified by the given launch configuration,
	 * or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the main type name specified by the given launch configuration,
	 *         or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public String getMainTypeName(ILaunchConfiguration configuration)
			throws CoreException {
		return "org.walkmod.WalkModDispatcher";
	}

	/**
	 * Returns the program arguments specified by the given launch
	 * configuration, as a string. The returned string is empty if no program
	 * arguments are specified.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the program arguments specified by the given launch
	 *         configuration, possibly an empty string
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	@Override
	public String getProgramArguments(ILaunchConfiguration configuration)
			throws CoreException {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean isOffline = configuration.getAttribute(
				LaunchingConstants.OFFLINE_OPTION,
				store.getBoolean(PreferenceConstants.WALKMOD_IS_OFFLINE));
		boolean printErrorMessages = configuration.getAttribute(
				LaunchingConstants.PRINT_ERRORS_OPTION, false);

		String args = "-Dlog4j.configuration=\""
				+ new File(getConfigDir(configuration), "log4j.properties")
						.getAbsolutePath() + "\"";
		if (isOffline) {
			args += " --offline";
		}
		if (printErrorMessages) {
			args += " -e";
		}

		List<String> chains = configuration.getAttribute(
				LaunchingConstants.SELECTED_CHAINS, new LinkedList<String>());

		String chainNames = "";
		for (String chain : chains) {
			chainNames = chainNames + " " + chain;
		}

		args = configuration.getAttribute(LaunchingConstants.SELECTED_OPTION,
				"apply") + " " + args;
		return args;
	}

}
