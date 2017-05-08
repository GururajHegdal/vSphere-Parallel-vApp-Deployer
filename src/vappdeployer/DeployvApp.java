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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VAppCloneSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualApp;
import com.vmware.vim25.mo.VirtualMachine;

public class DeployvApp
{
    // VC inventory related objects
    private static final String DC_MOR_TYPE = "Datacenter";
    private static final String RESPOOL_MOR_TYPE = "ResourcePool";
    private static final String HOST_MOR_TYPE = "HostSystem";
    private static final String VIRTUAL_APP_MOR_TYPE = "VirtualApp";

    private String vsphereIp;
    private String userName;
    private String password;
    private String srcvappName;
    private int iteration;
    private String url;
    private ServiceInstance si;
    private List<ManagedObjectReference> allDestHostsMor;
    private VirtualApp srcvAppObject = null;

    /**
     * Constructors
     */
    public
    DeployvApp(String[] cmdProps)
    {
        makeProperties(cmdProps);
    }

    public
    DeployvApp()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * Read properties from command line arguments
     */
    private void
    makeProperties(String[] cmdProps)
    {
        // get the property value and print it out
        System.out.println("Reading vSphere IP and Credentials information from command line arguments");
        System.out.println("-------------------------------------------------------------------");

        for (int i = 0; i < cmdProps.length; i++) {
            if (cmdProps[i].equals("--vsphereip")) {
                vsphereIp = cmdProps[i + 1];
                System.out.println("vSphere IP:" + vsphereIp);
            } else if (cmdProps[i].equals("--username")) {
                userName = cmdProps[i + 1];
                System.out.println("VC Username:" + userName);
            } else if (cmdProps[i].equals("--password")) {
                password = cmdProps[i + 1];
                System.out.println("VC password: ******");
            } else if (cmdProps[i].equals("--srcvapp")) {
                srcvappName = cmdProps[i + 1];
                System.out.println("Source vApp name:" + srcvappName);
            } else if (cmdProps[i].equals("--iteration")) {
                iteration = Integer.parseInt(cmdProps[i + 1]);
                System.out.println("Iterations:" + iteration);
            }
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    /**
     * Validate property values
     */
    boolean
    validateProperties()
    {
        boolean val = false;
        if (vsphereIp != null) {
            url = "https://" + vsphereIp + "/sdk";

            // Login to provided server IP to determine if we are running against single ESXi
            try {
                System.out.println("Logging into vSphere : " + vsphereIp + ", with provided credentials");
                si = loginTovSphere(url);

                if (si != null) {
                    System.out.println("Succesfully logged into vSphere: " + vsphereIp);
                    val = true;
                } else {
                    System.err.println(
                        "Service Instance object for vSphere:" + vsphereIp + " is null, probably we failed to login");
                    printFailedLoginReasons();
                }
            } catch (Exception e) {
                System.err.println(
                    "Caught an exception, while logging into vSphere :" + vsphereIp + " with provided credentials");
                printFailedLoginReasons();
            }
        }
        return val;
    }

    /**
     * Method prints out possible reasons for failed login
     */
    private void
    printFailedLoginReasons()
    {
        System.err.println(
            "Possible reasons:\n1. Provided username/password credentials are incorrect\n"
                + "2. If username/password or other fields contain special characters, surround them with double "
                + "quotes and for non-windows environment with single quotes (Refer readme doc for more information)\n"
                + "3. vCenter Server/ESXi server might not be reachable");
    }

    /**
     * Login method to VC
     */
    private
    ServiceInstance loginTovSphere(String url)
    {
        try {
            si = new ServiceInstance(new URL(url), userName, password, true);
        } catch (Exception e) {
            System.out.println("Caught exception while logging into vSphere server");
            e.printStackTrace();
        }
        return si;
    }

    /**
     * vApp Deployment handler main function
     */
    public void
    vAppDeploymentHandler() throws Exception
    {
        // login to vcva
        si = loginTovSphere(url);
        assert (si != null);
        System.out.println("Succesfully logged into VC: " + vsphereIp);

        System.out.println("Search for specified vApp in inventory...");
        ManagedEntity orivAppMe = retrievevApp(srcvappName);

        if (orivAppMe != null) {
            System.out.println("Found vApp: " + srcvappName + " in inventory");
            ManagedObjectReference orivAppMor = orivAppMe.getMOR();
            srcvAppObject = new VirtualApp(si.getServerConnection(), orivAppMor);
            VirtualMachine[] vms = srcvAppObject.getVMs();

            if (vms.length > 0) {
                ManagedObjectReference srcHostMor = vms[0].getRuntime().getHost();
                HostSystem hs = new HostSystem(si.getServerConnection(), srcHostMor);
                String srcHostName = hs.getName();
                System.out.println("Found Source Host: " + srcHostName);

                System.out.println("Retrieve Hosts list from inventory ...");
                ManagedEntity[] allHosts = retrieveHosts();
                allDestHostsMor = new ArrayList<ManagedObjectReference>();

                if (allHosts.length > 1) {
                    System.out.println(
                        "Found more than one host in inventory, forming target host's list for vApp deployment");

                    for (ManagedEntity tempHostMe : allHosts) {
                        if (!tempHostMe.getName().equals(srcHostName)) {
                            allDestHostsMor.add(tempHostMe.getMOR());
                        }
                    }
                } else {
                    System.out.println("There is only one host in the inventory, using the same as target host");
                    allDestHostsMor.add(srcHostMor);
                }
            } else {
                System.err.println("Could not find any VMs in vApp: " + srcvappName);
            }
        } else {
            System.err.println("Could not find vApp: " + srcvappName + " in inventory");
        }

        if (allDestHostsMor != null && allDestHostsMor.size() > 0) {

            for (int i = 1; i <= iteration; i++) {
                try {
                    System.out.println("\n***************  ITERATION - " + i + "  *************** ");
                    List<Thread> allThreads = new ArrayList<Thread>();

                    for (ManagedObjectReference tempHostMor : allDestHostsMor) {
                        VAppDeploymentClass vappDepClassObj = new VAppDeploymentClass(tempHostMor);
                        Thread vAppDepThread = new Thread(vappDepClassObj);
                        vAppDepThread.start();
                        allThreads.add(vAppDepThread);
                        Thread.sleep(1000);
                    }
                    for (Thread t : allThreads) {
                        t.join();
                    }
                } catch (Exception e) {
                    System.err.println("[Error] Caught exception while deploying vApps");
                }
                System.out.println("**********************************************************************");
            }
        }

        try {
            Thread.sleep(1000 * 3);
        } catch (Exception e) {
            // eat out the exception
        }
        System.out.println("######################### Script execution completed #########################");

    }

    /**
     * Class to handle deployment of vAPPs, per Host once
     */
    class VAppDeploymentClass implements Runnable
    {
        ManagedObjectReference destHostMor;

        VAppDeploymentClass(ManagedObjectReference hostMor)
        {
            destHostMor = hostMor;
        }

        @Override
        public void
        run() {
            try {

                HostSystem ihs = new HostSystem(si.getServerConnection(), destHostMor);
                String hostName = ihs.getName();
                System.out.println("vAPP Clone/Deploy operation is about to start ...");
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println("Host : " + hostName);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

                VAppCloneSpec vappCloneSpec = new VAppCloneSpec();
                vappCloneSpec.setHost(destHostMor);

                Datastore targetDs = null;
                HostSystem tempIhs = new HostSystem(si.getServerConnection(), destHostMor);

                for (Datastore tempDs : tempIhs.getDatastores()) {

                    if (tempDs.getSummary().isAccessible()) {
                        targetDs = tempDs;
                        break;
                    }
                }
                vappCloneSpec.setLocation(targetDs.getMOR());

                ResourcePool targetResPool = null;
                ComputeResource hostResource = new ComputeResource(si.getServerConnection(), destHostMor);
                ManagedEntity[] allRespools = new InventoryNavigator(si.getRootFolder())
                    .searchManagedEntities(RESPOOL_MOR_TYPE);

                for (int i = 0; i < allRespools.length; i++) {
                    ResourcePool tempResPool = (ResourcePool) allRespools[i];

                    if (tempResPool.getOwner().getName().equals(hostResource.getName())) {
                        targetResPool = tempResPool;
                        break;
                    }
                }

                ManagedEntity tempMgdEntityObj = tempIhs.getParent();

                while (!tempMgdEntityObj.getMOR().getType().equals(DC_MOR_TYPE)) {
                    tempMgdEntityObj = tempMgdEntityObj.getParent();
                }

                Folder vmFolder = ((Datacenter) tempMgdEntityObj).getVmFolder();
                vappCloneSpec.setVmFolder(vmFolder.getMOR());

                String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(Calendar.getInstance().getTime());
                String newvAppName = srcvappName + timeStamp + hostName;
                Task taskWhole = srcvAppObject.cloneVApp_Task(newvAppName, targetResPool.getMOR(), vappCloneSpec);

                VirtualApp newvAppObject = null;
                System.out.println("Monitor vApp: " + newvAppName + " deployment task ...");

                if (taskTracker(taskWhole)) {
                    ManagedEntity newvAppMe = retrievevApp(newvAppName);

                    if (newvAppMe != null) {
                        System.out.println("vApp: " + newvAppName + " deployment suceeded");
                        newvAppObject = new VirtualApp(si.getServerConnection(), newvAppMe.getMOR());

                        System.out.println("Poweron All VMs from vApp: " + newvAppName);
                        Datacenter dcObj = new Datacenter(si.getServerConnection(),
                            ((Datacenter) tempMgdEntityObj).getMOR());
                        Task listOfPoweronVmTasks = dcObj.powerOnMultiVM_Task(newvAppObject.getVMs());

                        // Monitor all VMs poweron task
                        if (taskTracker(listOfPoweronVmTasks)) {
                            System.out
                                .println("All VMs from vAPP: " + newvAppName + " have been poweredOn successfully");
                        } else {
                            System.err.println("Not All VMs from vApp: " + newvAppName + " could be powered on");
                        }
                    } else {
                        System.err.println("vApp: " + newvAppName + " deployment task failed");
                    }
                } else {
                    System.err.println("Could not find deployed vApp: " + srcvappName + " in inventory");
                }

                System.out.println("Begin cleanup tasks ...");
                Thread.sleep(1000 * 10);

                System.out.println("Power off all poweredOn VMs, if any");
                List<Task> listOfPoweroffVmTasks = new ArrayList<Task>();

                for (VirtualMachine tempVm : newvAppObject.getVMs()) {

                    if (tempVm.getRuntime().getPowerState().equals(VirtualMachinePowerState.poweredOn)) {
                        Task poweroffVmTask = tempVm.powerOffVM_Task();
                        listOfPoweroffVmTasks.add(poweroffVmTask);
                    }

                    // Monitor all VMs poweroff task
                    if (taskTracker(listOfPoweroffVmTasks)) {
                        System.out.println("All VMs from vAPP: " + newvAppName + " have been poweredOff successfully");
                    } else {
                        System.err.println("Not All VMs from vApp: " + newvAppName + " could be powered off");
                    }
                }
                System.out.println("Destory the vAPP");
                Thread.sleep(1000 * 10);

                if (taskTracker(newvAppObject.destroy_Task())) {
                    System.out.println("vApp: " + newvAppName + " destroyed successfully");
                } else {
                    System.err.println("vApp: " + newvAppName + " could not be destroyed");
                }

            } catch (Exception e) {
                System.err.println("[Error] Caught exception");
                e.printStackTrace();
            }

        } // End of run method

        /**
         * Monitor Task progress and return final state for list of tasks
         */
        private boolean
        taskTracker(List<Task> listOfPoweronVmTasks) throws Exception
        {
            boolean allTasksSucceded = false;
            List<Boolean> taskStatusList = new ArrayList<Boolean>();

            for (Task tempTaskMor : listOfPoweronVmTasks) {
                Boolean tempTaskStatus = taskTracker(tempTaskMor);
                taskStatusList.add(tempTaskStatus);
            }

            if (taskStatusList.contains(Boolean.FALSE)) {
                allTasksSucceded = false;
            } else {
                allTasksSucceded = true;
            }

            return allTasksSucceded;
        }

        /**
         * Monitor Task progress and return final state
         */
        private boolean
        taskTracker(Task taskMor) throws Exception
        {
            boolean isTaskSuccess = false;

            TaskInfoState taskState = taskMor.getTaskInfo().getState();

            while (!(taskState.equals(TaskInfoState.success))) {

                if ((taskState.equals(TaskInfoState.error))) {
                    System.err.println("Task errored out");
                    break;
                } else {
                    System.out.println("Task is still running");
                    Thread.sleep(2000);
                }
                taskState = taskMor.getTaskInfo().getState();
            }

            if (taskState.equals(TaskInfoState.success)) {
                System.out.println("Task Completed");
                isTaskSuccess = true;
            }

            return isTaskSuccess;
        }

    } // End of vAPP deployment class

    /**
     * All hosts
     */
    private ManagedEntity[]
    retrieveHosts()
    {
        // get first datacenters in the environment.
        InventoryNavigator navigator = new InventoryNavigator(si.getRootFolder());
        ManagedEntity[] hosts = null;

        try {
            hosts = navigator.searchManagedEntities(HOST_MOR_TYPE);
        } catch (Exception e) {
            System.err.println("[Error] Unable to retrive Hosts from inventory");
            e.printStackTrace();
        }
        return hosts;
    }

    /**
     * Search for specified vAPP and return its ManagedEntity
     */
    private ManagedEntity
    retrievevApp(String vAppName)
    {
        // get first datacenters in the environment.
        InventoryNavigator navigator = new InventoryNavigator(si.getRootFolder());
        ManagedEntity vAppMe = null;

        try {
            ManagedEntity[] vAppMeArray = navigator.searchManagedEntities(VIRTUAL_APP_MOR_TYPE);

            for (ManagedEntity tempvApp : vAppMeArray) {
                if (tempvApp.getName().equals(vAppName)) {
                    vAppMe = tempvApp;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Error] Unable to retreive specified vApp from inventory");
            e.printStackTrace();
        }
        return vAppMe;
    }

} // End of main class