package ru.vyarus.gradle.plugin.python.task.pip

import groovy.transform.CompileStatic
import org.gradle.api.tasks.TaskAction

/**
 * Print available new versions for the registered pip modules.
 *
 * @author Vyacheslav Rusakov
 * @since 01.12.2017
 */
@CompileStatic
class PipUpdatesTask extends BasePipTask {

    @TaskAction
    @SuppressWarnings('DuplicateNumberLiteral')
    void run() {
        if (modulesList.isEmpty()) {
            logger.lifecycle('No modules declared')
        } else {
            List<String> res = []
            List<String> updates = python
                    .readOutput('-m pip list -o -l --format=columns').toLowerCase().readLines()
            // header
            res.addAll(updates[0..1])
            2.times { updates.remove(0) }

            // search for lines matching modules
            modulesList.each { PipModule mod ->
                String line = updates.find { it =~ /$mod.name\s+/ }
                if (line) {
                    res.add(line)
                }
            }

            if (res.size() > 2) {
                logger.lifecycle('The following modules could be updated:\n\n{}',
                        res.collect { '\t' + it }.join('\n'))
            } else {
                logger.lifecycle('All modules use the most recent versions')
            }
        }
    }
}