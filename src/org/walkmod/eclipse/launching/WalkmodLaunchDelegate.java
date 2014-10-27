package org.walkmod.eclipse.launching;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.walkmod.eclipse.preferences.PreferenceConstants;

public class WalkmodLaunchDelegate extends JavaLaunchDelegate {

	private Bundle bundle = Platform.getBundle("org.walkmod.eclipse.libs");

	private String log4jCfgFile = "log4j.properties";

	private List<String> configFiles = null;

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

	public String getLog4jCfgFile(ILaunchConfiguration configuration) throws CoreException {
		loadConfigFiles(configuration);
		return log4jCfgFile;
	}

	private void loadConfigFiles(ILaunchConfiguration configuration)
			throws CoreException {
		if (configFiles == null) {
			configFiles = new LinkedList<String>();
			String installDir = configuration.getAttribute(
					LaunchingConstants.INSTALL_DIR, "");
			if (installDir == null || "".equals(installDir.trim())
					|| installDir.startsWith("EMBEDDED")) {

				Enumeration<String> classPath = bundle.getEntryPaths("/");
				while (classPath.hasMoreElements()) {
					String entry = classPath.nextElement();
					try {
						String file = new File(FileLocator.resolve(
								bundle.getEntry(entry)).toURI()).getAbsolutePath();
						if (file.endsWith(log4jCfgFile)) {
							log4jCfgFile = file;
						}
						// result.add(file);
					} catch (Exception e) {
						RuntimeException re = new RuntimeException(
								"Error resolving the lib paths");
						re.setStackTrace(e.getStackTrace());
						throw re;
					}
				}

			} else {
				if (installDir.startsWith("EXTERNAL (")) {
					installDir = installDir.substring("EXTERNAL (".length(),
							installDir.lastIndexOf(")"));
				}
				File configDir = new File(installDir, "config");
				File[] content = configDir.listFiles();
				for (File entry : content) {
					if (entry.getName().endsWith(log4jCfgFile)) {
						log4jCfgFile = entry.getAbsolutePath();
					}
					configFiles.add(entry.getAbsolutePath());
				}
			}
		}
	}

	public List<String> getConfigFiles(ILaunchConfiguration configuration)
			throws CoreException {

		loadConfigFiles(configuration);

		return configFiles;
	}

	public List<String> getLibFiles(ILaunchConfiguration configuration)
			throws CoreException {

		String installDir = configuration.getAttribute(
				LaunchingConstants.INSTALL_DIR, "");
		List<String> result = new LinkedList<String>();

		if (installDir == null || "".equals(installDir.trim())
				|| installDir.startsWith("EMBEDDED")) {
			try {
				result.add(BundleUtils.getBundleLocation(bundle)
						.getAbsolutePath());
			} catch (IOException e) {
				RuntimeException re = new RuntimeException(
						"Error resolving bundle");
				re.setStackTrace(e.getStackTrace());
				throw re;
			}

		}

		else {
			if (installDir.startsWith("EXTERNAL (")) {
				installDir = installDir.substring("EXTERNAL (".length(),
						installDir.lastIndexOf(")"));
			}
			File lib = new File(installDir, "lib");
			File[] content = lib.listFiles();
			for (File entry : content) {
				result.add(entry.getAbsolutePath());
			}
		}

		return result;

	}

	@Override
	public String[] getClasspath(ILaunchConfiguration configuration)
			throws CoreException {

		List<String> classPathEntries = new LinkedList<String>();

		classPathEntries.addAll(getLibFiles(configuration));

		classPathEntries.addAll(getConfigFiles(configuration));

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

		String args = "";

		
		try {
			args = "-Dlog4j.configuration=\"" + getLog4jCfgFile(configuration) + "\"";
		} catch (Exception e) {
			RuntimeException re = new RuntimeException();
			re.setStackTrace(e.getStackTrace());
			throw re;
		}
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
