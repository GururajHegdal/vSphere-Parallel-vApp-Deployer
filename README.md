# vSphere- Parallel vApp Deployer
### 1. Details
Utility Class to Deploy/Clone vAPP in a threaded fashion, from source host to all destination hosts in inventory for specified number iterations. It does the following operations,
 * Clone source vAPP on to each of the destination host
 * Power on VMs in vAPP (in a threaded fashion) using PowerOnMultiVM_Task API
 * Power off VMs in vAPP
 * Destroy the vAPP

### 2. How to run the Utility?
##### Run from Dev IDE

 * Import files under the src/vappdeployer/ folder into your IDE.
 * Required libraries are embedded within Runnable-Jar/vAppDeployer.jar, extract & import the libraries into the project.
 *  Run the utility from 'RunApp' program by providing arguments like:  
 _--vsphereip 192.168.1.1 --username adminUser --password dummyPasswd --srcvapp SrcDummyvApp --iteration 1_

##### Run from Pre-built Jars
 * Copy/Download the vAppDeployer.jar from Runnable-jar folder (from the uploaded file) and unzip on to local drive folder say c:\vAppDeployer
 * Open a command prompt and cd to the folder, lets say cd hostdstat
 * Run a command like shown below to see various usage commands:  
 _C:\vAppDeployer>java -jar vAppDeployer.jar --help_
 
### 3. Sample output
```
Logging into vSphere : 192.168.1.1, with provided credentials
Succesfully logged into vSphere: 192.168.1.1
Succesfully logged into VC: 192.168.1.1
Search for specified vApp in inventory...
Found vApp: SrcDummyvApp in inventory
Found Source Host: ESXiHost1
Retrieve Hosts list from inventory ...
Found more than one host in inventory, forming target host's list for vApp deployment

***************  ITERATION - 1  *************** 
vAPP Clone/Deploy operation is about to start ...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Host : ESXiHost2
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
vAPP Clone/Deploy operation is about to start ...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Host : ESXiHost3
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Monitor vApp: SrcDummyvApp20170508-162016-084ESXiHost2 deployment task ...
[SrcDummyvApp20170508-162016-084ESXiHost2-Deploy task] Task is still running
[SrcDummyvApp20170508-162016-084ESXiHost2-Deploy task] Task is still running
Monitor vApp: SrcDummyvApp20170508-162019-225ESXiHost3 deployment task ...
[SrcDummyvApp20170508-162019-225ESXiHost3-Deploy task] Task is still running
[SrcDummyvApp20170508-162016-084ESXiHost2-Deploy task] Task is still running
[SrcDummyvApp20170508-162019-225ESXiHost3-Deploy task] Task is still running
[SrcDummyvApp20170508-162016-084ESXiHost2-Deploy task] Task Completed
vApp: SrcDummyvApp20170508-162016-084ESXiHost2 deployment suceeded
Poweron All VMs from vApp: SrcDummyvApp20170508-162016-084ESXiHost2
[SrcDummyvApp20170508-162016-084ESXiHost2-PowerOn VM] Task Completed
All VMs from vAPP: SrcDummyvApp20170508-162016-084ESXiHost2 have been poweredOn successfully
Begin cleanup tasks ...
[SrcDummyvApp20170508-162019-225ESXiHost3-Deploy task] Task is still running
[SrcDummyvApp20170508-162019-225ESXiHost3-Deploy task] Task Completed
vApp: SrcDummyvApp20170508-162019-225ESXiHost3 deployment suceeded
Poweron All VMs from vApp: SrcDummyvApp20170508-162019-225ESXiHost3
[SrcDummyvApp20170508-162019-225ESXiHost3-PowerOn VM] Task Completed
All VMs from vAPP: SrcDummyvApp20170508-162019-225ESXiHost3 have been poweredOn successfully
Begin cleanup tasks ...
Power off all poweredOn VMs, if any
[SrcDummyvApp20170508-162016-084ESXiHost2-PowerOff VM] Task is still running
Power off all poweredOn VMs, if any
[SrcDummyvApp20170508-162016-084ESXiHost2-PowerOff VM] Task Completed
All VMs from vAPP: SrcDummyvApp20170508-162016-084ESXiHost2 have been poweredOff successfully
[SrcDummyvApp20170508-162016-084ESXiHost2-PowerOff VM] Task Completed
[SrcDummyvApp20170508-162016-084ESXiHost2-PowerOff VM] Task Completed
All VMs from vAPP: SrcDummyvApp20170508-162016-084ESXiHost2 have been poweredOff successfully
Destory the vAPP
[SrcDummyvApp20170508-162019-225ESXiHost3-PowerOff VM] Task is still running
[SrcDummyvApp20170508-162019-225ESXiHost3-PowerOff VM] Task Completed
All VMs from vAPP: SrcDummyvApp20170508-162019-225ESXiHost3 have been poweredOff successfully
[SrcDummyvApp20170508-162019-225ESXiHost3-PowerOff VM] Task Completed
[SrcDummyvApp20170508-162019-225ESXiHost3-PowerOff VM] Task is still running
[SrcDummyvApp20170508-162019-225ESXiHost3-PowerOff VM] Task Completed
All VMs from vAPP: SrcDummyvApp20170508-162019-225ESXiHost3 have been poweredOff successfully
Destory the vAPP
[SrcDummyvApp20170508-162016-084ESXiHost2-Destroy vApp] Task is still running
[SrcDummyvApp20170508-162016-084ESXiHost2-Destroy vApp] Task Completed
vApp: SrcDummyvApp20170508-162016-084ESXiHost2 destroyed successfully
[SrcDummyvApp20170508-162019-225ESXiHost3-Destroy vApp] Task is still running
[SrcDummyvApp20170508-162019-225ESXiHost3-Destroy vApp] Task Completed
vApp: SrcDummyvApp20170508-162019-225ESXiHost3 destroyed successfully
```
