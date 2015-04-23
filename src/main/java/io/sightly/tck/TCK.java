/*******************************************************************************
 * Copyright 2014 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package io.sightly.tck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code TCK} singleton is the main entry point for standalone TCK execution.
 */
public enum TCK {

    /**
     * Singleton instance.
     */
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(TCK.class);

    private static final String TESTFILES = "testfiles/";
    private static final String TEST_DEFINITIONS_PATH = TESTFILES + "definitions/";
    private static final String SCRIPTS = "testfiles/scripts/";
    private static final String OUTPUT = "testfiles/output";

    /**
     * CLI definitions
     */
    private static final String CLI_EXTRACT = "extract";
    private static final String CLI_EXTRACT_DESCRIPTION = "extract test files in the specified folder";
    private static final String CLI_URL = "url";
    private static final String CLI_URL_DESCRIPTION = "defines the URL where the Sightly scripts are deployed for testing";
    private static final String CLI_AUTH_USER = "authUser";
    private static final String CLI_AUTH_USER_DESCRIPTION = "in case Basic auth is needed this option defines the username";
    private static final String CLI_AUTH_PASS = "authPass";
    private static final String CLI_AUTH_PASS_DESCRIPTION = "in case Basic auth is needed this option defines the password";

    private JarFile jarFile;
    private List<JSONObject> testDefinitions;


    TCK() {
        String jarPath = TCK.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            jarFile = new JarFile(jarPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to instantiate TCK.", e);
        }
        testDefinitions = new ArrayList<JSONObject>();
    }

    private void run() {
        JUnitCore jUnitCore = new JUnitCore();
        LOG.info("Running " + TestsRunner.class.getName());
        Result result = jUnitCore.run(TestsRunner.suite());
        LOG.info(String.format("Tests run: %d, Failures: %d, Time elapsed: %.3f sec%n", result.getRunCount(),
                result.getFailureCount(), result.getRunTime() / 1000f));
        if (result.getFailures().size() > 0) {
            for (Failure f : result.getFailures()) {
                LOG.error(f.toString());
                String trace = f.getTrace();
                if (StringUtils.isNotEmpty(trace)) {
                    if (!(f.getException() instanceof AssertionError)) {
                        LOG.error(trace);
                    }
                }
            }
            die();
        }
    }

    private void extract(String extractDir) throws IOException {
        File extractFolder = new File(extractDir);
        if (extractFolder.exists()) {
            if (!extractFolder.isDirectory()) {
                throw new IOException("File entry " + extractFolder.getAbsolutePath() + " already exists and it is not a folder.");
            }
            if (!extractFolder.canWrite()) {
                throw new IOException("Folder " + extractFolder.getAbsolutePath() + " exists but it is not writable.");
            }
        } else {
            if (!extractFolder.mkdirs()) {
                throw new IOException("Unable to create folder " + extractFolder.getAbsolutePath() + ".");
            }
        }
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(TESTFILES)) {
                File file = new File(extractFolder, entryName);
                if (entry.isDirectory()) {
                    if (!file.mkdir()) {
                        throw new IOException("Unable to create folder " + file.getAbsolutePath());
                    }
                    continue;
                }
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = jarFile.getInputStream(entry);
                    fos = new FileOutputStream(file);
                    while (is.available() > 0) {
                        fos.write(is.read());
                    }
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    LOG.error("Unable to extract file " + file.getAbsolutePath());
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }
            }
        }
    }

    /**
     * Reads the embedded test definition files and provides a list of {@link JSONObject}.
     *
     * @return the list of test definitions as JSON objects
     */
    public List<JSONObject> getTestDefinitions() {
        if (testDefinitions.size() == 0) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(TEST_DEFINITIONS_PATH) && entryName.length() > TEST_DEFINITIONS_PATH.length()) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        InputStream testDefinitionStream = jarFile.getInputStream(entry);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(testDefinitionStream, "UTF-8"));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        testDefinitions.add(new JSONObject(sb.toString()));
                        reader.close();
                    } catch (IOException e) {
                        LOG.error("Skipping entry " + entryName, e);
                    }
                }
            }
        }
        return testDefinitions;
    }

    public static void main(String[] args) {

        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption(
                OptionBuilder.withLongOpt(CLI_EXTRACT).withDescription(CLI_EXTRACT_DESCRIPTION).hasOptionalArg().withArgName("DIR")
                        .create());
        options.addOption(OptionBuilder.withLongOpt(CLI_URL).withDescription(CLI_URL_DESCRIPTION).hasOptionalArg().withArgName("URL")
                .create());
        options.addOption(OptionBuilder.withLongOpt(CLI_AUTH_USER).withDescription(CLI_AUTH_USER_DESCRIPTION).hasOptionalArg()
                .withArgName("USER").create());
        options.addOption(OptionBuilder.withLongOpt(CLI_AUTH_PASS).withDescription(CLI_AUTH_PASS_DESCRIPTION).hasOptionalArg()
                .withArgName("PASS").create());
        try {
            CommandLine line = parser.parse(options, args);
            if (!line.iterator().hasNext()) {
                printUsage(options);
                die();
            } else if (line.hasOption(CLI_EXTRACT)) {
                String extractDir = line.getOptionValue(CLI_EXTRACT);
                if (StringUtils.isEmpty(extractDir)) {
                    // assume user wants to extract stuff in current directory
                    extractDir = System.getProperty("user.dir");
                }
                INSTANCE.extract(extractDir);
                LOG.info("Extracted testing resources in folder {}.", extractDir + File.separator + TESTFILES);
            } else {
                if (line.hasOption(CLI_URL)) {
                    String url = line.getOptionValue(CLI_URL);
                    if (StringUtils.isEmpty(url)) {
                        LOG.error("Missing value for --" + CLI_URL + " command line option.");
                        printUsage(options);
                        die();
                    }
                    System.setProperty(Constants.SYS_PROP_SERVER_URL, url);
                }
                if (line.hasOption(CLI_AUTH_USER) || line.hasOption(CLI_AUTH_PASS)) {
                    String user = line.getOptionValue(CLI_AUTH_USER);
                    if (StringUtils.isEmpty(user)) {
                        LOG.error("Missing value for --" + CLI_AUTH_USER + " command line option");
                        printUsage(options);
                        die();
                    }
                    String pass = line.getOptionValue(CLI_AUTH_PASS);
                    if (StringUtils.isEmpty(pass)) {
                        LOG.error("Missing value for --" + CLI_AUTH_PASS + " command line option");
                        printUsage(options);
                        die();
                    }
                    System.setProperty(Constants.SYS_PROP_USER, user);
                    System.setProperty(Constants.SYS_PROP_PASS, pass);
                }
                INSTANCE.run();
            }

        } catch (ParseException e) {
            printUsage(options);
            die();
        } catch (IOException e) {
            LOG.error("IO Error.", e);
            die();
        }
    }

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar io.sightly.tck-<version>-standalone.jar", options, true);
    }

    private static void die() {
        System.exit(1);
    }

}
