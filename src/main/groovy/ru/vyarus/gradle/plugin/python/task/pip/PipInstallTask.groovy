package ru.vyarus.gradle.plugin.python.task.pip

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.gradle.api.tasks.*

/**
 * Install required modules (with correct versions) into python using pip.
 * Default task is registered as pipInstall to install all modules declared in
 * {@link ru.vyarus.gradle.plugin.python.PythonExtension#modules}.
 * {@link ru.vyarus.gradle.plugin.python.task.CheckPythonTask} always run before pip install task to validate
 * environment.
 * <p>
 * All {@link ru.vyarus.gradle.plugin.python.task.PythonTask}s are depend on pipInstall by default.
 *
 * @author Vyacheslav Rusakov
 * @since 11.11.2017
 */
@CompileStatic
class PipInstallTask extends BasePipTask {

    /**
     * True to show list of all installed python modules (not only modules installed by plugin!).
     * By default use {@link ru.vyarus.gradle.plugin.python.PythonExtension#showInstalledVersions} value.
     */
    @Console
    boolean showInstalledVersions
    /**
     * True to always call 'pip install' for configured modules, otherwise pip install called only
     * if module is not installed or different version installed.
     * By default use {@link ru.vyarus.gradle.plugin.python.PythonExtension#showInstalledVersions} value.
     */
    @Input
    @Optional
    boolean alwaysInstallModules

    PipInstallTask() {
        onlyIf { !modulesList.empty }
        // task will always run for the first time (even if deps are ok), but all consequent runs will be up-to-date
        outputs.upToDateWhen { modulesToInstall.empty }
    }

    @TaskAction
    void run() {
        modulesToInstall.each { pip.install(it) }

        // could be at first run (upToDateWhen requires at least one task execution)
        if (modulesToInstall.empty) {
            logger.lifecycle('All required modules are already installed with correct versions')
        }

        if (isShowInstalledVersions()) {
            // show all installed modules versions (to help problems resolution)
            pip.exec('list --format=columns')
        }
    }

    @Memoized
    @Internal
    protected List<String> getModulesToInstall() {
        List<String> res = []
        if (!modulesList.isEmpty()) {
            // use list of installed modules to check if 'pip install' is required for module
            List<String> installed = (isAlwaysInstallModules()
                    ? '' : python.readOutput('-m pip freeze')).toLowerCase().readLines()
            // install modules
            modulesList.each { PipModule mod ->
                String pipDef = mod.toPipString()
                // don't install if already installed (assume dependencies are also installed)
                if (!installed.contains(pipDef.toLowerCase())) {
                    res.add(pipDef)
                }
            }
        }
        return res
    }
}