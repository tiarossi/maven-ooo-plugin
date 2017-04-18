package org.openoffice.maven.installer;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openoffice.maven.ConfigurationManager;

/**
 * 
 * @author Frederic Morin <frederic.morin.8@gmail.com>
 * 
 * @goal uninstall
 */
public class OOoUninstalMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.attachedArtifacts}
     * @required
     * @readonly
     */
    private List<Artifact> attachedArtifacts;

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
     * <p>This method uninstall an openoffice plugin package.</p>
     * 
     * @throws MojoExecutionException
     *             if there is a problem during the packaging execution.
     * @throws MojoFailureException
     *             if the packaging can't be done.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ooo = ConfigurationManager.initOOo(ooo);
        getLog().info("OpenOffice.org used: " + ooo.getAbsolutePath());

        sdk = ConfigurationManager.initSdk(sdk);
        getLog().info("OpenOffice.org SDK used: " + sdk.getAbsolutePath());

        Artifact unoPlugin = null;
        for (Artifact attachedArtifact : attachedArtifacts) {
            String extension = FilenameUtils.getExtension(attachedArtifact.getFile().getPath());
            if ("zip".equals(extension) || "oxt".equals(extension)) {
                unoPlugin = attachedArtifact;
                break;
            }
        }

        if (unoPlugin == null) {
            throw new MojoExecutionException("Could not find plugin artefact (.zip)");
        }
        
        File unoPluginFile = unoPlugin.getFile();

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String unopkg = "unopkg";
            if (os.startsWith("windows")) {
                unopkg = "unopkg.com";
            }
            
            final FileInputStream fis = new FileInputStream(unoPluginFile);
            final ZipInputStream zip = new ZipInputStream(fis);
            String extensionIdentifier = "";
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                if (!entry.getName().equals("description.xml")) {
                    entry = zip.getNextEntry();
                    continue;
                }
                final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                final XMLStreamReader streamReader = inputFactory.createXMLStreamReader(zip);
                while (streamReader.hasNext()) {
                    if (streamReader.isStartElement()) {
                        if (streamReader.getLocalName().equals("identifier")) {
                            extensionIdentifier = streamReader.getAttributeValue(streamReader.getAttributeNamespace(0), "value");
                            break;
                        }
                    }
                    streamReader.next();
                }
                break;
            }
            getLog().info("Uninstalling plugin to OOo... please wait");
            //int returnCode = ConfigurationManager.runCommand(unopkg, "list");
            int returnCode = ConfigurationManager.runCommand(unopkg, "remove", extensionIdentifier, "--log-file", File.createTempFile("unopkg", ".log").getAbsolutePath());
            if (returnCode == 0) {
                getLog().info("Plugin uninstalled successfully");
            } else {
                throw new MojoExecutionException("'unopkg remove " + extensionIdentifier + "' returned with " + returnCode);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error while uninstalling package to OOo.", e);
        }
    }

}
