# XL Release Overthere plugin

## Build Status

[![Build Status](https://travis-ci.org/xebialabs-community/xlr-overthere-plugin.svg?branch=master)](https://travis-ci.org/xebialabs-community/xlr-overthere-plugin)

## Preface

This document describes the functionality provided by the XL Release Overthere plugin.

For information about XL Release, refer to [https://docs.xebialabs.com/xl-release/](https://docs.xebialabs.com/xl-release/).

## Overview

The XL Release Overthere plugin allows XL Release to manipulate files and execute commands on remote hosts. It does so by using the **Overthere** framework. Overthere is a Java library to manipulate files and execute processes on remote hosts, i.e. do stuff "over there". See the [Overthere GitHub repository](https://github.com/xebialabs/overthere) for more information.

## Requirements

* XL Release: 4.8.1 or later

### Features ###

* Supports SSH for connectivity to Unix, Microsoft Windows, and z/OS hosts.
* Supports CIFS, Telnet, and WinRM for connectivity to Windows hosts.
* Allows SSH jumpstations to be used to access hosts to which a direct network connection is not possible.

    **Note:** In addition to SSH connections, CIFS, Telnet, and WinRM connections can be tunneled through an SSH jumpstation.

* All connection methods are implemented internally in XL Release itself, so no external dependencies are required.

    An exception is the `WINRM_NATIVE` connection type which uses the Windows `winrs` command, but a _winrs proxy_ can be used to use this connection type when XL Release runs on a Unix host.

## Installation

Copy the plugin JAR file to the `SERVER_HOME/plugins` directory of XL Release.

## Supported Tasks

The Overthere plugin defines a generic task type called **remoteScript.Overthere** that executes a shell or batch script (depending on the chosen host).

Input properties:

| Property | Description |
| -------- | ----------- |
| Host | Remote host where the script executes |
| Username | User ID to use when connecting to the host |
| Password | Password to use when connecting to the host |
| Private Key File | Private key file to use for authentication (*SSH host only*) |
| Passphrase | Passphrase for the private key in the private key file (*SSH host only*) |
| Connection Options | Additional options passed to the Overthere connection; refer to the [Overthere documentation](https://github.com/xebialabs/overthere#common_connection_options) for a comprehensive list of available options |
| Remote Path | Path on the remote host where the script will be executed |
| Script | Script to execute on the remote host |

Output properties:

| Property | Description |
| -------- | ----------- |
| Output | Variable in which the remote process standard output will be stored |
| Error | Variable in which the remote process error output will be stored |

### Host types ###

The Overthere plugin includes the following configuration item (CI) types, which define the protocol that is used to access the target host:

* `remoteScript.SshHost`: Connects to a Unix, Microsoft Windows, or z/OS host using the [SSH protocol](http://en.wikipedia.org/wiki/Secure_Shell). For Windows, OpenSSH on [Cygwin](http://www.cygwin.com) (i.e. [Copssh](https://www.itefix.no/i2/copssh)) and [WinSSHD](http://www.bitvise.com/winsshd) are supported.
* `remoteScript.CifsHost`: Connects to a Microsoft Windows host using the [CIFS protocol](http://en.wikipedia.org/wiki/Server_Message_Block) (also known as SMB) for file manipulation and either [WinRM](http://en.wikipedia.org/wiki/WS-Management) or [Telnet](http://en.wikipedia.org/wiki/Telnet) for process execution. This protocol is not supported for Unix or z/OS hosts.

For more information about options, usage, and troubleshooting, refer to the [Overthere documentation](https://github.com/xebialabs/overthere#table-of-contents).

## Advanced configuration

### Connecting through an SSH jumpstation

When XL Release cannot reach a remote host directly, but that host can be reached by setting up one (or more) SSH tunnels, configure one (or more) `remoteScript.SshJumpstation` CIs as follows:

1. Create a `remoteScript.SshJumpStation` CI that represents a host to which XL Release can connect directly.
1. For each remote host that XL Release cannot reach directly, create a `remoteScript.Host` and configure the **jumpstation** property to refer to the `remoteScript.SshJumpStation` CI created in step 1.

When XL Release creates a connection to the remote host and determines that it needs to connect through a jumpstation, it will first open a connection to that jumpstation and then set up a SSH tunnel ("local port forward") to the remote host.

**Note:** Jumpstations can also refer to other jumpstations for even more complex network setups, but cycles are not allowed.

### Additional Overthere properties in XL Release

You can provide additional Overthere connection properties (as defined in the [Overthere documentation](https://github.com/xebialabs/overthere/blob/master/README.md)) using the **connectionOptions** key-value map. These connection options are available on the `remoteScript.Host` configuration and the `remoteScript.Overthere` task. When the same property is defined on both, the task value overrides the host-defined value.

Note that you can also override UI-exposed configuration properties of `remoteScript.Host` (such as **Connection Type**) using the `Connection Options` map; for example, `connectionType=WINRM_NATIVE`.

	<type-modification type="remoteScript.SshHost">
		<property name="listFilesCommand" hidden="true" default="/bin/ls -a1 {0}" />
		<property name="getFileInfoCommand" hidden="true" default="/bin/ls -ld {0}" />
		<property name="connectionTimeoutMillis" category="Advanced" kind="integer" default="1800000" description="Number of milliseconds Overthere waits for a connection to a remote host to be established"/>
	</type-modification>
