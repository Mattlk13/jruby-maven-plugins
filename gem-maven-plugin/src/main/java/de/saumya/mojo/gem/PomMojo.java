package de.saumya.mojo.gem;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;

import de.saumya.mojo.gems.GemspecConverter;
import de.saumya.mojo.jruby.AbstractJRubyMojo;
import de.saumya.mojo.ruby.EmbeddedLauncherFactory;
import de.saumya.mojo.ruby.Log;
import de.saumya.mojo.ruby.RubyScriptException;

/**
 * goal to converts a gemspec file into pom.xml.
 * 
 * @goal pom
 */
public class PomMojo extends AbstractJRubyMojo {

    private static List<String> NO_CLASSPATH = Collections.emptyList();

    protected final Log         log          = new Log() {
                                                 public void info(
                                                         final CharSequence content) {
                                                     getLog().info(content);
                                                 }
                                             };

    /**
     * @parameter expression="${pom}" default-value="pom.xml"
     */
    protected File              pom;

    /**
     * @parameter default-value="${pom.force}"
     */
    protected boolean           force        = false;

    /**
     * @parameter default-value="${pom.gemspec}"
     */
    protected File              gemspecFile;

    public void execute() throws MojoExecutionException {
        if (this.pom.exists() && !this.force) {
            getLog().info(this.pom.getName()
                    + " already exists. use '-Dpom.force=true' to overwrite");
            return;
        }
        if (this.gemspecFile == null) {
            getLog().debug("no gemspec file given, see if there is single one");
            for (final File file : new File(".").listFiles()) {
                if (file.getName().endsWith(".gemspec")) {
                    if (this.gemspecFile != null) {
                        getLog().info("there is no gemspec file given but there are more then one in the current directory.");
                        getLog().info("use '-Dpom.gemspec=...' to select the gemspec file to process");
                        return;
                    }
                    this.gemspecFile = file;
                }
            }
        }
        if (this.gemspecFile == null) {
            getLog().info("there is no gemspec file given and no gemspec file found (*.gemspec). nothing to do.");
            return;
        }
        else {
            try {
                final GemspecConverter gemspec = new GemspecConverter(this.log,
                        new EmbeddedLauncherFactory().getLauncher(this.verbose,
                                                                  NO_CLASSPATH,
                                                                  setupEnv(),
                                                                  resolveJRUBYCompleteArtifact().getFile(),
                                                                  null));

                gemspec.createPom(this.gemspecFile, "0.21.0-TODO", this.pom);

            }
            catch (final RubyScriptException e) {
                throw new MojoExecutionException("error in rake script", e);
            }
            catch (final DependencyResolutionRequiredException e) {
                throw new MojoExecutionException("could not resolve jruby", e);
            }
            catch (final IOException e) {
                throw new MojoExecutionException("IO error", e);
            }
        }
    }
}