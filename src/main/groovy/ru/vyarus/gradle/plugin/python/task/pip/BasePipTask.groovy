package ru.vyarus.gradle.plugin.python.task.pip

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import ru.vyarus.gradle.plugin.python.cmd.Pip
import ru.vyarus.gradle.plugin.python.task.BasePythonTask
import ru.vyarus.gradle.plugin.python.util.CliUtils

/**
 * Base task for pip tasks.
 *
 * @author Vyacheslav Rusakov
 * @since 01.12.2017
 */
@CompileStatic
class BasePipTask extends BasePythonTask {

    /**
     * Minimal required python version.
     * By default use {@link ru.vyarus.gradle.plugin.python.PythonExtension#minVersion} value.
     */
    @Input
    @Optional
    String minPythonVersion

    /**
     * List of modules to install. Module declaration format: 'name:version'.
     * For default pipInstall task modules are configured in
     * {@link ru.vyarus.gradle.plugin.python.PythonExtension#modules}
     */
    @Input
    @Optional
    List<String> modules = []

    /**
     * Shortcut for {@link #pip(java.lang.Iterable)}.
     *
     * @param modules modules to install
     */
    void pip(String... modules) {
        pip(Arrays.asList(modules))
    }

    /**
     * Add modules to install. Module format: 'name:version'. Duplicate declarations are allowed: in this case the
     * latest declaration will be used.
     *
     * @param modules modules to install
     */
    void pip(Iterable<String> modules) {
        getModules().addAll(modules)
    }

    protected void validatePython() {
        String dir = python.homeDir
        String version = python.version
        String minVersion = getMinPythonVersion()
        if (!CliUtils.isVersionMatch(version, minVersion)) {
            throw new GradleException("Python ($dir) verion $version does not match minimal " +
                    "required version: $minVersion")
        }
        logger.lifecycle('Using Python {} ({})', version, dir)
    }

    /**
     * Resolve modules list for installation by removing duplicate definitions (the latest definition wins).
     *
     * @return pip modules to install
     */
    @Internal
    @Memoized
    protected List<PipModule> getModulesList() {
        Map<String, PipModule> mods = [:] // linked map
        // sequential parsing in order to override duplicate definitions
        // (latter defined module overrides previous definition) and preserve definition order
        getModules().each {
            PipModule mod = PipModule.parse(it)
            mods[mod.name] = mod
        }
        return new ArrayList(mods.values())
    }

    @Internal
    @Memoized
    @SuppressWarnings('UnnecessaryGetter')
    protected Pip getPip() {
        return new Pip(project, getPythonPath())
    }
}
