/*************************************************************************
 * $RCSfile: ConfigurationManager.java,v $
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
package org.openoffice.maven;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.cli.*;
import org.openoffice.maven.utils.FileFinder;

/**
 * Stores the Mojo configuration for use in the build visitors.
 * 
 * @author Cedric Bosdonnat
 */
public class ConfigurationManager {
    
    private static Log log = new SystemStreamLog();

    /**
     * Path to the URD directory in the output directory.
     */
    private static final String URD_DIR = "urd";

    /**
     * Path to the <code>types.rdb</code> file in the output directory.
     */
    private static final String TYPES_FILE = "types.rdb";

    /**
     * The path to the IDL directory in the resources directory.
     */
    private static final String IDL_DIR = "idl";

    private static File sOoo;

    private static File sSdk;
    
    private static File idlDir;

    private static File sOutput;

    private static File sClassesOutput;

    private static File sUserInstallation;

    private static String sJpdaAddress;
    
    /**
     * We want to use the same Log stream as the Mojos in this project.
     * So you can set it here.
     * 
     * @param mojoLog the new Log
     */
    public static void setLog(Log mojoLog) {
        log = mojoLog;
    }
    
    /**
     * Gets the log.
     *
     * @return the log
     */
    public static Log getLog() {
        return log;
    }

    /**
     * @return the folder where OpenOffice.org is installed.
     */
    public static File getOOo() {
        if (sOoo == null) {
            sOoo = Environment.getOfficeHome();
        }
        return sOoo;
    }
    
    /**
     * Sets the OpenOffice.org installation folder to use for the build.
     * 
     * @param pOoo
     *            the OpenOffice.org installation folder.
     */
    public static void setOOo(File pOoo) {
        assert pOoo != null;
        sOoo = pOoo;
        Environment.setOfficeHome(pOoo);
    }
    
    /**
     * The office home attribute is initialized with the given dir parameter if
     * it is set.
     *
     * @param dir the init value for office home
     * @return the office home directory
     */
    public static File initOOo(final File dir) {
        if (dir != null) {
            setOOo(dir);
        }
        return getOOo();
    }

    /**
     * @return the OpenOffice.org <code>types.rdb</code> file path
     */
    public static String getOOoTypesFile() {
        File oooTypes = FileFinder.tryFiles(new File(getOOo(), "/program/types.rdb"),
                new File(Environment.getOoSdkUreHome(), "/share/misc/types.rdb"),
                new File(Environment.getOoSdkUreHome(), "/misc/types.rdb"));
        if (oooTypes == null) {
            throw new RuntimeException("types.rdb not found");
        }
        return oooTypes.getPath();
    }

    /**
     * @return the OpenOffice.org <code>offapi.rdb</code> file path
     */
    public static String getOffapiTypesFile() {
        File offapiTypesFile = FileFinder.tryFiles(
                new File(Environment.getOfficeBaseHome(), "program/offapi.rdb"),
                new File(Environment.getOfficeHome(), "program/types/offapi.rdb"));

        return offapiTypesFile.getPath();
    }
    
    /**
     * @return the folder where OpenOffice.org SDK is installed.
     */
    public static File getSdk() {
        if (sSdk == null) {
            sSdk = Environment.getOoSdkHome();
        }
        return sSdk;
    }

    /**
     * @return the folder where the classes files are generated.
     */
    public static File getClassesOutput() {
        if (!sClassesOutput.exists()) {
            sClassesOutput.mkdirs();
        }
        return sClassesOutput;
    }

    /**
     * @return the path to the folder where URD files should be generated
     */
    public static String getUrdDir() {
        return new File(sOutput, URD_DIR).getPath();
    }

    /**
     * @return the path to the generated <code>types.rdb</code>.
     */
    public static String getTypesFile() {
        return new File(sOutput, TYPES_FILE).getPath();
    }

    /**
     * Sets the idl dir.
     *
     * @param dir the new idl dir
     */
    public static synchronized void setIdlDir(File dir) {
        idlDir = dir;
    }
    
    /**
     * @return the path to the folder containing the IDL files to build or
     *         <code>null</code> if no IDL folder has been found.
     */
    public static synchronized File getIdlDir() {
        if (idlDir == null) {
            File dir = new File("src/main/", IDL_DIR);
            if (dir.isDirectory()) {
                idlDir = dir;
            }
        }
        return idlDir;
    }

    /**
     * Sets the OpenOffice.org SDK installation folder to use for the build.
     * 
     * @param pSdk
     *            the OpenOffice.org SDK installation folder.
     */
    public static void setSdk(File pSdk) {
        assert pSdk != null;
        sSdk = pSdk;
        Environment.setOoSdkHome(pSdk);
    }
    
    /**
     * The SDK attribute is initialized with the given dir parameter if
     * it is set.
     *
     * @param dir the init value for the SDK
     * @return the SDK directory
     */
    public static File initSdk(final File dir) {
        if (dir != null) {
            setSdk(dir);
        }
        return getSdk();
    }

    /**
     * Sets the directory where the generated files should go.
     * 
     * @param pOutput
     *            the output directory.
     */
    public static void setOutput(File pOutput) {
        sOutput = pOutput;
    }
    
    /**
     * Gets the output.
     *
     * @return the output
     */
    public static File getOutput() {
        return sOutput;
    }

    /**
     * Sets the directory where the generated classes should go.
     * 
     * @param pOutputDirectory
     *            the classes output directory.
     */
    public static void setClassesOutput(File pOutputDirectory) {
        sClassesOutput = pOutputDirectory;
    }

    /**
     * Returns the path to the SDK binaries depending on the OS and the
     * architecture.
     * 
     * @param pHome
     *            the OpenOffice.org SDK home
     * @return the full path to the SDK binaries
     */
    private static String getSdkBinPath() {
        File sdkHome = Environment.getOoSdkHome();
        // OOo SDK does not seem to include the target os in their packaging
        // anymore. Tested with 3.2.0
        String path = "/bin";
        if (new File(sdkHome, path).exists()) {
            return new File(sdkHome, path).getPath();
        }

        // Get the Architecture properties
        String arch = System.getProperty("os.arch").toLowerCase();

        if (SystemUtils.IS_OS_WINDOWS) {
            path = "/windows/bin/";
        } else if (SystemUtils.IS_OS_SOLARIS) {
            if (arch.equals("sparc")) {
                path = "/solsparc/bin";
            } else {
                path = "/solintel/bin";
            }
        } else if (SystemUtils.IS_OS_MAC) {
            path = "/bin";
        } else {
            path = "/linux/bin";
        }

        return new File(sdkHome, path).getPath();
    }
    
    /**
     * Returns the path to the OOO binaries depending on the OS and the
     * architecture.
     * 
     * @return the full path to the OOo binaries
     */
    private static String getOOoBinPath() {
        File oooHome = Environment.getOfficeHome();
        File binDir = new File(oooHome, "program");
        if (SystemUtils.IS_OS_MAC) {
            binDir = new File(oooHome, "Contents/MacOS");
        }
        return binDir.getAbsolutePath();
    }

    /**
     * Sets the directory os the user installation to use
     *
     * @param pUserInstallation
     * the user installation directory
     */
    public static void setUserInstallation(File pUserInstallation) {
        assert pUserInstallation != null;
        sUserInstallation = pUserInstallation;
    }

    /**
     * Returns the path os installation where extension will be installed
     *
     * @return the user installatino path
     */
    public static File getUserInstallation() {
        return sUserInstallation;
    }

    public static File initUserInstallation(File pUserInstallation) {
        if (pUserInstallation != null) {
            setUserInstallation(pUserInstallation);
        }
        return getUserInstallation();
    }

    public static String getJpdaAddress() {
        return sJpdaAddress;
    }

    public static void setJpdaAddress(String sJpdaAddress) {
        ConfigurationManager.sJpdaAddress = sJpdaAddress;
    }

    /**
     * Run command.
     * See {@link "http://docs.codehaus.org/display/MAVENUSER/Mojo+Developer+Cookbook"}.
     *
     * @param cmd the command
     * @return the exit code of the command
     * @throws CommandLineException the command line exception
     */
    public static int runCommand(final String cmd) throws CommandLineException {
        Commandline cl = new Commandline(cmd);
        return runCommand(cl);
    }

    /**
     * Run command.
     * See {@link "http://docs.codehaus.org/display/MAVENUSER/Mojo+Developer+Cookbook"}.
     *
     * @param cmd the command
     * @param args the args
     * @return the exit code of the command
     * @throws CommandLineException the command line exception
     */
    public static int runCommand(final String cmd, final String... args) throws CommandLineException {
        Commandline cl = new Commandline(cmd);
        cl.addArguments(args);
        return runCommand(cl);
    }

    private static int runCommand(Commandline cl) throws CommandLineException {
        try {
            setUpEnvironmentFor(cl);
        } catch (Exception e) {
            log.warn("can't setup environment for '" + cl + "' - will try without environment...");
        }
        CommandLineUtils.StringStreamConsumer output = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer error = new CommandLineUtils.StringStreamConsumer();
        int returnValue = CommandLineUtils.executeCommandLine(cl, output, error);
        String outmsg = output.getOutput().trim();
        if (StringUtils.isNotEmpty(outmsg)) {
            log.info(outmsg);
        }
        String errmsg = error.getOutput().trim();
        if (StringUtils.isNotEmpty(errmsg)) {
            log.warn(errmsg);
        }
        log.info("'" + cl + "' returned with " + returnValue);
        return returnValue;
    }

    private static void setUpEnvironmentFor(final Commandline cl) throws Exception {
        cl.addSystemEnvironment();
        Properties envVars = cl.getSystemEnvVars();
        String path = envVars.getProperty("PATH", "");
        String pathSep = System.getProperty("path.separator", ":");
        path = getSdkBinPath() + pathSep + getOOoBinPath() + pathSep + Environment.getOoSdkUreBinDir() + pathSep + path;        
        cl.addEnvironment("PATH", path);
        //log.debug("PATH=" + path);
        String oooLibs = Environment.getOoSdkUreLibDir().getCanonicalPath();
        if (SystemUtils.IS_OS_WINDOWS) {
            // I'm not sure if this works / is necessary
            cl.addEnvironment("DLLPATH", oooLibs);
            cl.addEnvironment("LIB", oooLibs);
            //log.debug("DLLPATH=" + oooLibs);
        } else if (SystemUtils.IS_OS_MAC) {
            cl.addEnvironment("DYLD_LIBRARY_PATH", oooLibs);
            //log.debug("DYLD_LIBRARY_PATH=" + oooLibs);
        } else {
            // *NIX environment
            cl.addEnvironment("LD_LIBRARY_PATH", oooLibs);
            //log.debug("LD_LIBRARY_PATH=" + oooLibs);
        }
        if (sJpdaAddress != null) {
            cl.addEnvironment("JAVA_TOOL_OPTIONS", "-Xdebug -Xrunjdwp:transport=dt_socket,server=n,address=" + sJpdaAddress);
        }
        
        if (sUserInstallation != null) {
            cl.addEnvironment("UserInstallation", "file://" + sUserInstallation.toURI().getRawPath());
        }
        /*log.debug("Environment");
        for (int i = 0; i < cl.getEnvironmentVariables().length; i++) {
            String string = cl.getEnvironmentVariables()[i];
            log.debug(string);
        }*/
    }

}
