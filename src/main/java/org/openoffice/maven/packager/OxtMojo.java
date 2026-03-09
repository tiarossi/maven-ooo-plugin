/*************************************************************************
 * $RCSfile: OxtMojo.java,v $
 * $Revision: 1.1 $
 * last change: $Author: cedricbosdo $ $Date: 2007/10/08 18:35:15 $
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
 * Sun Microsystems Inc., October, 2000
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 * Copyright: 2002 by Sun Microsystems, Inc.
 * All Rights Reserved.
 * Contributor(s): Cedric Bosdonnat
 ************************************************************************/
package org.openoffice.maven.packager;

import java.io.File;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.openoffice.maven.ConfigurationManager;
import org.openoffice.plugin.core.model.UnoPackage;

/**
 * Packages the OpenOffice.org extension into an .oxt file.
 *
 * @author Cedric Bosdonnat
 */
@Mojo(name = "oxt", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class OxtMojo extends AbstractOxtMojo {

    /**
     * List of attached artifacts.
     */
    @Parameter(defaultValue = "${project.attachedArtifacts}", required = true, readonly = true)
    private List<Artifact> attachedArtifacts;

    /**
     * OOo instance to build the extension against.
     */
    @Parameter(property = "ooo")
    private File ooo;

    /**
     * OOo SDK installation where the build tools are located.
     */
    @Parameter(property = "sdk")
    private File sdk;

    /**
     * OXT directory where the OXT sources can be found
     */
    @Parameter(property = "oxtDir", defaultValue = "src/main/resources")
    private File oxtDir;

    /**
     * Directory containing the jars that should be packaged into the lib
     * directory of the oxt
     */
    @Parameter(property = "libDir", defaultValue = "${project.build.directory}/lib")
    private File libDir;

    /**
     * This is where build results go.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private File directory;

    /**
     * Directory containing the classes and resource files that should be
     * packaged into the JAR.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will
     * be an attachment instead.
     */
    @Parameter
    private String classifier;

    @Override
    protected String getClassifier() {
        return classifier;
    }

    /**
     * Return the main classes directory, so it's used as the root of the jar.
     */
    @Override
    protected File getClassesDirectory() {
        return classesDirectory;
    }

    @Override
    protected String[] getExcludes() {
        List<String> excludes = new ArrayList<String>(Arrays.asList(super.getExcludes()));

        excludes.add("oxt/**");
        excludes.add("idl/**");

        return excludes.toArray(new String[excludes.size()]);
    }

    /**
     * Main method of the OpenOffice.org Extension packager Mojo.
     * <p>
     * This method generates the package for the OpenOffice.org extension. It
     * uses some configuration files to describe the package
     * </p>
     * 
     * @throws MojoExecutionException
     *             if there is a problem during the packaging
     *             execution.
     */
    @Override
    public void execute() throws MojoExecutionException {
        // first package compiled sources in a jar archive.
        super.execute();

        // build the oxt package
        ooo = ConfigurationManager.initOOo(ooo);
        getLog().info("OpenOffice.org used: " + ooo.getAbsolutePath());
        sdk = ConfigurationManager.initSdk(sdk);
        getLog().info("OpenOffice.org SDK used: " + sdk.getAbsolutePath());
        ConfigurationManager.setOutput(directory);
        ConfigurationManager.setClassesOutput(outputDirectory);

        if (oxtDir == null) {
            throw new MojoExecutionException("No OXT folder configured");
        }

        File oxtFile = new File(outputDirectory, finalName + ".oxt");
        UnoPackage pkg = new UnoPackage(oxtFile);

        for (Artifact a : attachedArtifacts) {
            if (a.getType().equalsIgnoreCase("jar")) {
                pkg.addComponentFile(a.getFile().getName(), a.getFile(), "Java");
            } else {
                System.out.println(a.getFile() + " of type " + a.getType() + " skipped");
            }
        }

        pkg.addDirectory(oxtDir, this.getIncludes(), this.getExcludes());
        //Add libraries to the oxt file
        if (libDir != null && libDir.isDirectory()) {
            for (File child : libDir.listFiles()) {
                final String extension = FileUtils.extension(child.getName());
                if (extension.equalsIgnoreCase("jar")) {
                    pkg.addComponentFile("lib/" + child.getName(), child, "Java");
                } else {
                    getLog().info(String.format("%s of type %s skipped", child.getName(), extension));
                }
            }
        }



        if (pkg.close() == null) {
            getLog().error("OXT Package Build failed");
        }

        if (classifier != null) {
            projectHelper.attachArtifact(getProject(), "oxt", classifier, oxtFile);
        } else {
            getProject().getArtifact().setFile(oxtFile);
        }

        getLog().warn("OxtMojo not finished yet...");
    }

}
