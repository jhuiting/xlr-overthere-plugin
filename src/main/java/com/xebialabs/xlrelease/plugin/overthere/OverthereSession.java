/**
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS
 * FOR A PARTICULAR PURPOSE. THIS CODE AND INFORMATION ARE NOT SUPPORTED BY XEBIALABS.
 */
package com.xebialabs.xlrelease.plugin.overthere;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OverthereSession {

    private static final String SCRIPT_NAME = "uploaded-script";
    private final ConfigurationItem host;
    private final String extension;

    public OverthereSession(ConfigurationItem host) {
        this.host = host;
        this.extension = host.<OperatingSystemFamily>getProperty(OPERATING_SYSTEM).getScriptExtension();
    }

    public CmdResponse execute(String script, String remotePath) {
        int rc;
        CapturingOverthereExecutionOutputHandler stdout = capturingHandler();
        CapturingOverthereExecutionOutputHandler stderr = capturingHandler();

        try (OverthereConnection connection = new OverthereConnectionBuilder().getConnection(host)) {
            if (remotePath != null && !remotePath.trim().isEmpty()) {
                connection.setWorkingDirectory(connection.getFile(remotePath));
            }

            OverthereFile targetFile = connection.getTempFile(SCRIPT_NAME, extension);
            OverthereUtils.write(script.getBytes(UTF_8), targetFile);
            targetFile.setExecutable(true);

            CmdLine scriptCommand = CmdLine.build(targetFile.getPath());
            rc = connection.execute(stdout, stderr, scriptCommand);
        } catch (Exception e) {
            StringWriter stacktrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stacktrace, true));
            stderr.handleLine(stacktrace.toString());
            rc = 1;
        }

        return new CmdResponse(rc, stdout.getOutput(), stderr.getOutput());
    }

    public class CmdResponse {
        public int rc;
        public String stdout;
        public String stderr;

        public CmdResponse(int rc, String stdout, String stderr) {
            this.rc = rc;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

}
