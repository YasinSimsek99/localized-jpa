package com.localizedjpa.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * Maven goal that compiles Java sources with LocalizedJPA annotation processor.
 * 
 * <p>This plugin wraps maven-compiler-plugin and automatically configures all
 * required JVM arguments for annotation processing.
 * 
 * <p>Usage:
 * <pre>{@code
 * <plugin>
 *     <groupId>com.localizedjpa</groupId>
 *     <artifactId>localized-jpa-maven-plugin</artifactId>
 *     <version>0.1.0-SNAPSHOT</version>
 *     <executions>
 *         <execution>
 *             <goals>
 *                 <goal>compile</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 * </plugin>
 * }</pre>
 *
 * @since 0.1.0
 */
@Mojo(
    name = "compile",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.COMPILE,
    threadSafe = true
)
public class LocalizedJpaCompilerMojo extends AbstractMojo {

    private static final String COMPILER_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    private static final String COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin";
    private static final String COMPILER_PLUGIN_VERSION = "3.11.0";

    private static final String[] JDK_MODULES = {
        "jdk.compiler/com.sun.tools.javac.api",
        "jdk.compiler/com.sun.tools.javac.code",
        "jdk.compiler/com.sun.tools.javac.processing",
        "jdk.compiler/com.sun.tools.javac.tree",
        "jdk.compiler/com.sun.tools.javac.util"
    };

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    private BuildPluginManager pluginManager;

    /**
     * LocalizedJPA compiler version. Defaults to plugin version.
     */
    @Parameter(property = "localizedjpa.version", defaultValue = "0.1.0")
    private String processorVersion;

    /**
     * Java source version.
     */
    @Parameter(property = "maven.compiler.source", defaultValue = "17")
    private String sourceVersion;

    /**
     * Java target version.
     */
    @Parameter(property = "maven.compiler.target", defaultValue = "17")
    private String targetVersion;

    /**
     * Skip plugin execution.
     */
    @Parameter(property = "localizedjpa.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Lombok version. Set to empty string to disable Lombok support.
     */
    @Parameter(property = "lombok.version", defaultValue = "1.18.30")
    private String lombokVersion;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("LocalizedJPA compilation skipped");
            return;
        }

        getLog().info("LocalizedJPA: Compiling with annotation processor...");

        try {
            executeCompilerPlugin();
            getLog().info("LocalizedJPA: Compilation completed successfully");
        } catch (Exception e) {
            throw new MojoExecutionException("LocalizedJPA compilation failed", e);
        }
    }

    private void executeCompilerPlugin() throws MojoExecutionException {
        Element[] compilerArgs = buildCompilerArguments();
        Element[] processorPaths = buildAnnotationProcessorPaths();

        executeMojo(
            plugin(
                groupId(COMPILER_PLUGIN_GROUP_ID),
                artifactId(COMPILER_PLUGIN_ARTIFACT_ID),
                version(COMPILER_PLUGIN_VERSION)
            ),
            goal("compile"),
            configuration(
                element("source", sourceVersion),
                element("target", targetVersion),
                element("fork", "true"),
                element("annotationProcessorPaths", processorPaths),
                element("compilerArgs", compilerArgs)
            ),
            executionEnvironment(project, session, pluginManager)
        );
    }

    private Element[] buildCompilerArguments() {
        // -parameters + (exports + opens for each module)
        Element[] args = new Element[1 + JDK_MODULES.length * 2];

        int index = 0;
        args[index++] = element("arg", "-parameters");

        for (String module : JDK_MODULES) {
            args[index++] = element("arg", "-J--add-exports=" + module + "=ALL-UNNAMED");
            args[index++] = element("arg", "-J--add-opens=" + module + "=ALL-UNNAMED");
        }

        return args;
    }

    private Element[] buildAnnotationProcessorPaths() {
        if (lombokVersion != null && !lombokVersion.isEmpty()) {
            return new Element[] {
                element("path",
                    element("groupId", "com.localizedjpa"),
                    element("artifactId", "localized-jpa-compiler"),
                    element("version", processorVersion)
                ),
                element("path",
                    element("groupId", "org.projectlombok"),
                    element("artifactId", "lombok"),
                    element("version", lombokVersion)
                )
            };
        } else {
            return new Element[] {
                element("path",
                    element("groupId", "com.localizedjpa"),
                    element("artifactId", "localized-jpa-compiler"),
                    element("version", processorVersion)
                )
            };
        }
    }
}
