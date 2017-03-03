package org.openoffice.maven.installer;

import java.io.File;

import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;
import org.openoffice.maven.ConfigurationManager;

/**
 * @author Frederic Morin <frederic.morin.8@gmail.com>
 * @goal install
 * @phase install
 */
public class OOoInstalMojo extends AbstractMojo {

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * OOo instance to build the extension against.
     *
     * @parameter
     */
    private File ooo;

    /**
     * OOo SDK installation where the build tools are located.
     *
     * @parameter
     */
    private File sdk;

    /**
     * User Directoty for running
     *
     * @parameter default-value="${project.build.directory}/soffice_debug"
     */
    private File userInstallation;

    /**
     * <p>
     * This method install an openoffice plugin package to the specified
     * openoffice installation
     * </p>
     *
     * @throws MojoExecutionException
     * if there is a problem during the packaging execution.
     * @throws MojoFailureException
     * if the packaging can't be done.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        ooo = ConfigurationManager.initOOo(ooo);
        getLog().info("OpenOffice.org used: " + ooo.getAbsolutePath());

        sdk = ConfigurationManager.initSdk(sdk);
        getLog().info("OpenOffice.org SDK used: " + sdk.getAbsolutePath());

        userInstallation = ConfigurationManager.initUserInstallation(userInstallation);
        if (userInstallation != null) {
            getLog().info("User installation used: " + userInstallation.getAbsolutePath());
        }


        File unoPluginFile = project.getArtifact().getFile();
        if (!unoPluginFile.exists()) {
            throw new MojoExecutionException("Could not find plugin artefact [" + unoPluginFile + "]");
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String unopkg = "unopkg";
            if (os.startsWith("windows")) {
                unopkg = "unopkg.com";
            }

            getLog().info("Installing plugin to OOo... please wait");
            int returnCode = ConfigurationManager.runCommand(unopkg, "gui", "-f", unoPluginFile.getAbsolutePath());
            if (returnCode == 0) {
                getLog().info("Plugin installed successfully");
            } else {
                throw new MojoExecutionException("'unopkg gui -f " + unoPluginFile + "' returned with " + returnCode);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error while installing package to OOo.", e);
        }
    }
}
