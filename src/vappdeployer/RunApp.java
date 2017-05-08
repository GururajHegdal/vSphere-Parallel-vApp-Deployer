/**
 * Utility Class to Deploy/Clone vAPP in a threaded fashion, from source host to all destination
 * hosts in inventory for specified number iterations. It does the following operations,
 * - clone the vAPP on to each of the destination host
 * - Power on VMs in vAPP (in a threaded fashion) using PowerOnMultiVM_Task API
 * - Power off VMs in vAPP
 * - Destroy the vAPP
 *
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * @author Gururaja Hegdal (ghegdal@vmware.com)
 * @version 1.0
 *
 *          The above copyright notice and this permission notice shall be
 *          included in all copies or substantial portions of the Software.
 *
 *          THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *          EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *          OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *          NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *          HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *          WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *          FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *          OTHER DEALINGS IN THE SOFTWARE.
 */

package vappdeployer;

// Entry point into the vApp deployer tool
public class RunApp
{
    /**
     * Usage method - how to use/invoke the script, reveals the options supported through this script
     */
    public static void usagevAppDeployScript()
    {
        System.out.println(
            "Usage: java -jar vAppDeployer.jar --vsphereip <VC IP> --username <uname> --password <pwd> --srcvapp <SourcevAppName> --iteration <no of loops>");
        System.out.println(
            "\"java -jar vAppDeployer.jar --vsphereip 10.4.5.6 --username admin --password dummyPwd --srcvapp MyvApp --iteration 5\"");
    }

    /**
     * Main entry point
     *
     * @throws Exception
     */
    public static void main(String[] args)
    {

        System.out.println(
            "######################### Parallel vApp Deployer Script execution STARTED #########################");

        try {
            // Read command line arguments
            if (args.length > 0 && args.length >= 10) {
                DeployvApp vAppClassObj = new DeployvApp(args);
                if (vAppClassObj.validateProperties()) {
                    vAppClassObj.vAppDeploymentHandler();
                } else {
                    usagevAppDeployScript();
                }
            } else {
                usagevAppDeployScript();
            }

            Thread.sleep(1000 * 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(
            "######################### Parallel vApp Deployer Script execution completed #########################");
    }
}