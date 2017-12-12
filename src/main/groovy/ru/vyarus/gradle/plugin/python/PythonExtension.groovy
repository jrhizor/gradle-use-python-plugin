package ru.vyarus.gradle.plugin.python

import groovy.transform.CompileStatic

/**
 * Use-python plugin extension. Used to declare python location (use global if not declared) and specify
 * required modules to install.
 *
 * @author Vyacheslav Rusakov
 * @since 11.11.2017
 */
@CompileStatic
class PythonExtension {

    /**
     * Minimal required python version. Full format: "major.minor.micro". Any precision could be used, for example:
     * '3' (python 3 or above), '3.2' (python. 3.2 or above), '3.6.1' (python 3.6.1 or above).
     */
    String minVersion

    /**
     * Path to python binary (folder where python executable is located). When not set, global python is called.
     */
    String pythonPath
    /**
     * Python binary name to use. By default, for linux it would be python3 (if installed) or python and
     * for windows - python.
     * <p>
     * Useful when you want to force python 2 usage on linux or python binary name differs.
     */
    String pythonBinary
    /**
     * Install packages only for the current user (pip --user option). On linux this is often important
     * to overcome permission problems.
     * When false installs globally.
     */
    boolean userScope = true
    /**
     * Pip modules to install. Modules are installed in the declaration order.
     * Duplicates are allowed: only the latest declaration will be used. So it is possible
     * to pre-define some modules (for example, in plugin) and then override module version
     * by adding new declaration (with {@code pip ( .. )} method).
     * <p>
     * Use {@code pip ( .. )} methods for modules declaration.
     */
    List<String> modules = []

    /**
     * Calls 'pip list' to show all installed python modules (for problem investigations).
     * Enabled by default for faster problems detection (mostly to highlight used transitive dependencies versions).
     */
    boolean showInstalledVersions = true

    /**
     * By default, plugin will not call "pip install" for modules already installed (with exactly
     * the same version). Enable option if you need to always call "pip install module" (for example,
     * to make sure correct dependencies installed).
     */
    boolean alwaysInstallModules

    /**
     * Shortcut for {@link #pip(java.lang.Iterable)}.
     *
     * @param modules pip modules to install
     */
    void pip(String... modules) {
        pip(Arrays.asList(modules))
    }

    /**
     * Declare pip modules to install. Format: "moduleName:version". Only exact version
     * matching is supported (pip support ranges, but this is intentionally not supported in order to avoid
     * side effects).
     * <p>
     * Duplicate declarations are allowed: in this case the latest declaration will be used.
     * <p>
     * Note: if newer package version is already installed, pip will remove it and install correct version.
     *
     * @param modules pip modules to install
     */
    void pip(Iterable<String> modules) {
        this.modules.addAll(modules)
    }

    /**
     * Checks if module is already declared. Could be used by other plugins to test if required module
     * is manually declared or not (in order to apply defaults).
     *
     * @param name module name to check
     * @return true of module already declared, false otherwise
     */
    boolean isModuleDeclared(String name) {
        String nm = name.toLowerCase() + ':'
        String res = modules.find {
            it.toLowerCase().startsWith(nm)
        }
        return res != null
    }
}
