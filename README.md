# Peacemaker

Peacemaker is an EMF-based conflicts resolution tool that uses line-based merge conflicts instead of standard model comparisons. It provides an Eclipse-based editor to inspect models and apply conflict resolution actions, and it can also be used in a headless way to find conflicts programmatically.

## Using the Peacemaker editor in Eclipse

Peacemaker requires an Eclipse installation including EMF and Eclipse Plugin dependencies.

Currently, the way to test Peacemaker is to import `org.eclipse.epsilon.peacemaker` and `org.eclipse.epsilon.peacemaker.dt` in an Eclipse workspace, and then run an Eclipse instance from there.

## Headless mode

It is also possible to use Peacemaker from Java. To do that, either:

- Import and use the `org.eclipse.epsilon.peacemaker` project in your Eclipse workspace.
- To install Peacemaker in your local Maven repository, run `mvn clean install` in a terminal from the `org.eclipse.epsilon.peacemaker` folder.

## Eclipse projects in this repository

- `org.eclipse.epsilon.peacemaker`: contains the core Peacemaker conflict detection libraries that can be used in a headless mode as explained above
- `org.eclipse.epsilon.peacemaker.dt`: provides the Eclipse-based editor to find and resolve conflicts.
- `org.eclipse.epsilon.peacemaker.tests`: unit tests for development and to prevent regressions
- `org.eclipse.epsilon.examples.peacemaker.psl`: contains some conflict examples over an EMF-based project scheduling language (PSL)