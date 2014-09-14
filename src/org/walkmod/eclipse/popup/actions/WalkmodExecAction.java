/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.walkmod.eclipse.popup.actions;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

/**
 * Starts Tomcat on a specific JRE
 */
public class WalkmodExecAction extends Action implements IAction, IJavaLaunchConfigurationConstants {
	
	protected IVMInstall jre;
	
	private IPath workingDir;
	
	private String option = "apply";

	public WalkmodExecAction(IVMInstall vm, IPath workingDir, String option) {
		super(vm.getName());
		jre = vm;
		this.workingDir = workingDir;
		this.option = option;
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		try {
			
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = manager.getLaunchConfigurationType(ID_JAVA_APPLICATION);
		
			ILaunchConfiguration[] configurations = manager.getLaunchConfigurations(type);
			for (int i = 0; i < configurations.length; i++) {
				ILaunchConfiguration configuration = configurations[i];
				if (configuration.getName().equals("Walkmod "+option)) {
					configuration.delete();
					break;
				}
			}
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "Walkmod "+option);
			
			// specify a JRE
			workingCopy.setAttribute(ATTR_VM_INSTALL_NAME, jre.getName());
			workingCopy.setAttribute(ATTR_VM_INSTALL_TYPE, jre.getVMInstallType().getId());
			
			// specify main type and program arguments
			workingCopy.setAttribute(ATTR_MAIN_TYPE_NAME, "org.walkmod.WalkModDispatcher");
			workingCopy.setAttribute(ATTR_PROGRAM_ARGUMENTS, option);
			workingCopy.setAttribute(ATTR_WORKING_DIRECTORY, workingDir.toFile().getAbsolutePath());
			
			List<IRuntimeClasspathEntry> classPathEntries = new LinkedList<IRuntimeClasspathEntry>();
			
			// specify classpath
			File jdkHome = jre.getInstallLocation();
			IPath toolsPath = new Path(jdkHome.getAbsolutePath()).append("lib").append("tools.jar");
			IRuntimeClasspathEntry toolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath);
			toolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			classPathEntries.add(toolsEntry);			
			
			URL classPath = Thread.currentThread().getContextClassLoader().getResource("/org/walkmod/lib");
			File file = new File(FileLocator.resolve(classPath).toURI());
			
			File[] libraries = file.listFiles();
			
			for(int i = 0; i < libraries.length; i++){				
				IPath bootstrapPath = new Path(libraries[i].getAbsolutePath());
				IRuntimeClasspathEntry bootstrapEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(bootstrapPath);				
				bootstrapEntry.setClasspathProperty(IRuntimeClasspathEntry.ARCHIVE);
				classPathEntries.add(bootstrapEntry);
			}
			
						
			URL configPath = Thread.currentThread().getContextClassLoader().getResource("/org/walkmod/config");
			File configDir = new File(FileLocator.resolve(configPath).toURI());
			File log4j = new File(configDir, "log4j.properties");
			File[] configFiles = configDir.listFiles();
			for(int i = 0; i< configFiles.length; i++){
				
				IPath bootstrapConfigPath = new Path(configFiles[i].getAbsolutePath());
				IRuntimeClasspathEntry bootstrapConfigEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(bootstrapConfigPath);
				bootstrapConfigEntry.setClasspathProperty(IRuntimeClasspathEntry.OTHER);
				classPathEntries.add(bootstrapConfigEntry);	
			}
			
			
			IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
			IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath, IRuntimeClasspathEntry.STANDARD_CLASSES);
			classPathEntries.add(systemLibsEntry);
			
			List<String> classpath = new ArrayList<String>();
			for(IRuntimeClasspathEntry entry:classPathEntries){
				classpath.add(entry.getMemento());	
			}
			
			workingCopy.setAttribute(ATTR_CLASSPATH, classpath);
			workingCopy.setAttribute(ATTR_DEFAULT_CLASSPATH, false);
			
			// specify System properties
			workingCopy.setAttribute(ATTR_VM_ARGUMENTS, "-Dlog4j.configuration="+log4j.toURI());
			
			// save and launch
			ILaunchConfiguration configuration = workingCopy.doSave();
			
			DebugUITools.launch(configuration, ILaunchManager.RUN_MODE);
			
		} catch (Exception e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		run();
	}

}