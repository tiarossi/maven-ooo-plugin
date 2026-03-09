/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openoffice.maven.runner;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openoffice.maven.ConfigurationManager;

/**
 * Runs OpenOffice.org in debug mode.
 *
 * @author Tiago Drummond de Figueiredo Rossi <tiago.rossi@gmail.com>
 */
@Mojo(name = "debug", requiresDirectInvocation = true)
@Execute(phase = LifecyclePhase.INSTALL)
public class OOoDebugMojo extends AbstractMojo {

    /**
     * OOo instance to build the extension against.
     */
    @Parameter
    private File ooo;

    /**
     * OOo SDK installation where the build tools are located.
     */
    @Parameter
    private File sdk;

    /**
     * User Directory for debugging
     */
    @Parameter(defaultValue = "${project.build.directory}/soffice_debug")
    private File userInstallation;

    /**
     * Debug jpda configuration
     */
    @Parameter(property = "jpda.address")
    private String sJpdaAddress;

    /**
     * <p>
     * This method run an openoffice plugin package.</p>
     *
     * @throws MojoExecutionException
     * if there is a problem during the packaging execution.
     * @throws MojoFailureException
     * if the packaging can't be done.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ooo = ConfigurationManager.initOOo(ooo);
        getLog().info("Office used: " + ooo.getAbsolutePath());

        sdk = ConfigurationManager.initSdk(sdk);
        getLog().info("Office SDK used: " + sdk.getAbsolutePath());

        userInstallation = ConfigurationManager.initUserInstallation(userInstallation);
        getLog().info("User installation used: " + userInstallation.getAbsolutePath());

        ConfigurationManager.setJpdaAddress(sJpdaAddress);
       
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String executable = "soffice";
            if (os.startsWith("windows")) {
                executable = "soffice.exe";
            }

            getLog().info("Debugging OOo... please wait");
            int returnCode = ConfigurationManager.runCommand(executable, "--nofirststartwizard");
            if (returnCode == 0) {
                getLog().info("Debbug successfully");
            } else {
                throw new MojoExecutionException("'soffice --nofirststartwizard' returned with " + returnCode);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error while debugging OOo.", e);
        }
    }
}
