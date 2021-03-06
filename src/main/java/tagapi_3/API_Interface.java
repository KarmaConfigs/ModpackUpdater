/*
 *    MIT License

 *    Copyright (c) 2018 Ammar Ahmad

 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:

 *    The above copyright notice and this permission notice shall be included in all
 *    copies or substantial portions of the Software.

 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *    SOFTWARE.
 */

package tagapi_3;

import com.minecraft.moonlake.nbt.NBTTagCompound;
import com.minecraft.moonlake.nbt.NBTTagList;
import com.minecraft.moonlake.nbt.NBTUtil;
import ml.karmaconfigs.modpackupdater.utils.*;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

/**
 *
 * @author Ammar Ahmad
 */
public class API_Interface {

    public String getAPIVersion() {
        return "v0.11-alpha";
    }

    public String getUpdateStatus() {
        //download the file..
        Network network = new Network();
        LauncherUtils launcherUtils = new LauncherUtils();
        Local local = new Local();
        String OperatingSystemToUse = launcherUtils.getOS();
        network.downloadAPIMeta(OperatingSystemToUse);
        int versionBehind = 0;
        if (!local.getAPIMetaList(OperatingSystemToUse).contains(getAPIVersion())) {
            return "Unknown";
        } else {
            for (Object val : local.getAPIMetaList(OperatingSystemToUse)) {
                if (!getAPIVersion().equals(val)) {
                    versionBehind = versionBehind + 1;
                } else if (getAPIVersion().equals(val)) {
                    break;
                }
            }
        }
        return String.valueOf(versionBehind);
    }

    private String runLogs;

    //run logs getter/setter
    private String getRunLogs() {
        return runLogs; //run logs
    }

    private void setRunLogs(String runLogs_) {
        System.out.println(runLogs_);
        this.setLog("[rl] " + runLogs_);
        runLogs = "[rl] " + runLogs_;
    }

    private String downloadLogs;

    //download logs getter/setter
    private String getDownloadLogs() {
        return downloadLogs; //download logs
    }

    private void setDownloadLogs(String downloadLogs_) {
        System.out.println(downloadLogs_);
        this.setLog("[dl] " + downloadLogs_);
        downloadLogs = "[dl] " + downloadLogs_;
    }

    private String errorLogs;

    //last error logs getter/setter
    private String getErrorLogs() {
        return errorLogs;
    }

    private void setErrorLogs(String errorLogs_) {
        System.out.println(errorLogs_);
        this.setLog("[el] " + errorLogs_);
        errorLogs = "[el] " + errorLogs_;
    }

    private String log;

    //interface for log
    public String getLog() {
        return log;
    }

    private void setLog(String log_) {
        this.setLogs(log_);
        log = log_;
    }

    //interface for full logs
    private List logs = new ArrayList();

    public List getLogs() {
        return logs;
    }

    private void setLogs(String logs_) {
        logs.add(logs_);
    }

    public void dumpLogs() {
        LauncherUtils launcherUtils = new LauncherUtils();
        String OperatingSystemToUse = launcherUtils.getOS();
        launcherUtils.writeLogs(OperatingSystemToUse, (ArrayList) logs);
    }

    public List getInstallableVersionsList() {
        Local local = new Local();
        LauncherUtils launcherUtils = new LauncherUtils();
        String OperatingSystemToUse = launcherUtils.getOS();
        local.readJson_versions_id(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));
        local.readJson_versions_type(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));
        //local.version_manifest_versions_id;
        List InstallableVersionsList = new ArrayList();

        if (local.version_manifest_versions_id.size() == local.version_manifest_versions_type.size()) {
            //we can merge them..
            for (int i = 0; i < local.version_manifest_versions_id.size(); i++) {
                InstallableVersionsList.add(local.version_manifest_versions_id.get(i) + " % " + local.version_manifest_versions_type.get(i));
            }
        } else {
            //don't merge them..

            for (int i = 0; i < local.version_manifest_versions_id.size(); i++) {
                InstallableVersionsList.add(local.version_manifest_versions_id.get(i));
            }
        }
        return InstallableVersionsList;
    }

    private List getProfileInstalledVersionsList() {
        LauncherUtils launcherUtils = new LauncherUtils();
        Local local = new Local();
        String OperatingSystemToUse = launcherUtils.getOS();
        local.readJson_profiles_KEY(launcherUtils.getMineCraft_Launcher_Profiles_json(OperatingSystemToUse));
        local.readJson_profiles_KEY_lastVersionId(launcherUtils.getMineCraft_Launcher_Profiles_json(OperatingSystemToUse));
        return local.profiles_lastVersionId;
    }

    public List getInstalledVersionsList() {
        LauncherUtils launcherUtils = new LauncherUtils();
        Local local = new Local();
        String OperatingSystemToUse = launcherUtils.getOS();
        local.generateVersionJsonPathList(launcherUtils.getMineCraftVersionsLocation(OperatingSystemToUse));
        local.generateVersionList(launcherUtils.getMineCraftVersionsLocation(OperatingSystemToUse));

        return local.versions_list;
    }

    public List getServersIPList() {
        LauncherUtils launcherUtils = new LauncherUtils();
        String OperatingSystemToUse = launcherUtils.getOS();
        return launcherUtils.getMineCraftServerDatNBTIP(OperatingSystemToUse);
    }
    
    public List getServersNameList() {
        LauncherUtils launcherUtils = new LauncherUtils();
        String OperatingSystemToUse = launcherUtils.getOS();
        return launcherUtils.getMineCraftServerDatNBTName(OperatingSystemToUse);
    }
    
    public void addServerToServersDat(String Name, String IP) {
        LauncherUtils launcherUtils = new LauncherUtils();
        String OperatingSystemToUse = launcherUtils.getOS();
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList<NBTTagCompound> server = new NBTTagList<>("servers");
        NBTTagCompound data = new NBTTagCompound();
        
        List names = new ArrayList(launcherUtils.getMineCraftServerDatNBTName(OperatingSystemToUse));
        List ips = new ArrayList(launcherUtils.getMineCraftServerDatNBTIP(OperatingSystemToUse));
        data.setString("name", Name);
        data.setString("ip", IP);
        server.add(data);
        try {
            for (int i = 0; i < ips.size(); i++) {
                data = new NBTTagCompound();
                data.setString("name", names.get(i).toString());
                data.setString("ip", ips.get(i).toString());
                server.add(data);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        root.put(server);
        //System.out.println(root.toString());
        try {
            NBTUtil.writeFile(root, new File(launcherUtils.getMineCraft_ServersDat(OperatingSystemToUse)), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void syncVersions() {
        LauncherUtils launcherUtils = new LauncherUtils();
        Local local = new Local();
        String OperatingSystemToUse = launcherUtils.getOS();
        //this function is used to sync json and file system versions together.
        local.fixLauncherProfiles(OperatingSystemToUse); //<-- just fix it!
        API_Interface api_Interface = new API_Interface();

        List ProfileInstalledVersionsList = new ArrayList();    //json
        List InstalledVersionsList = new ArrayList();           //filesys

        InstalledVersionsList = api_Interface.getInstalledVersionsList();    //get json
        ProfileInstalledVersionsList = api_Interface.getProfileInstalledVersionsList();    //get filesys

        List union = new ArrayList(InstalledVersionsList);
        union.addAll(ProfileInstalledVersionsList);
        // Prepare an intersection
        List intersection = new ArrayList(InstalledVersionsList);
        intersection.retainAll(ProfileInstalledVersionsList);
        // Subtract the intersection from the union
        union.removeAll(intersection);
        union.removeAll(ProfileInstalledVersionsList); //this is required so that we can get rid of redundant versions
        // Print the result
        if (!union.isEmpty()) {
            for (Object n : union) {
                //add these versions to the system.
                if (n != null) {
                    System.out.println(n);
                    local.writeJson_launcher_profiles(OperatingSystemToUse, n.toString() + "_Cracked_" + launcherUtils.nextSessionId(), n.toString());
                }
            }
        }
    }

    private void injectNetty(){
        LauncherUtils launcherUtils = new LauncherUtils();
        String OperatingSystemToUse = launcherUtils.getOS();
        try {
            launcherUtils.injectNetty(OperatingSystemToUse);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        try {
            launcherUtils.injectPatchy(OperatingSystemToUse);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void runMinecraft(String UsernameToUse, String VersionToUse, Boolean HashCheck, Boolean injectNetty) {
        LauncherUtils launcherUtils = new LauncherUtils();
        Local local = new Local();
        Network network = new Network();
        String OperatingSystemToUse = launcherUtils.getOS();
        //get list of all 
        local.readJson_versions_id(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));
        local.readJson_versions_type(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));
        local.readJson_versions_url(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));

        //inject netty
        if (injectNetty) {
            injectNetty();
        }

        //declaration for mods
        String MOD_inheritsFrom = null;
        String MOD_jar = null;
        String MOD_assets = null;
        String MOD_minecraftArguments;
        String MOD_mainClass = null;
        String MOD_id = null;
        //check if it is vanilla or not
        if (local.checkIfVanillaMC(VersionToUse).equals(true)) {
            this.setRunLogs("Vanilla Minecraft found!");
        } else {
            this.setRunLogs("Modded Minecraft found!");
            local.MOD_readJson_libraries_name_PLUS_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            for (int i = 0; i < local.version_name_list.size(); i++) {
                this.setRunLogs((String) local.version_name_list.get(i));
                this.setRunLogs((String) local.HALF_URL_version_url_list.get(i));
            }

            this.setRunLogs("Fixing url using name.");
            for (int i = 0; i < local.version_name_list.size(); i++) {
                local.version_path_list.add(local.generateLibrariesPath(OperatingSystemToUse, local.version_name_list.get(i).toString()));

            }

            for (int i = 0; i < local.version_name_list.size(); i++) {
                local.version_url_list.add(local.HALF_URL_version_url_list.get(i) + "/" + local.version_path_list.get(i));
            }

            MOD_inheritsFrom = local.readJson_inheritsFrom(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setRunLogs("inheritsFrom: " + MOD_inheritsFrom);

            MOD_jar = local.readJson_jar(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setRunLogs("jar: " + MOD_jar);

            MOD_assets = local.readJson_assets(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setRunLogs("assets: " + MOD_assets);

            MOD_minecraftArguments = local.readJson_minecraftArguments(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setRunLogs("minecraftArguments: " + MOD_minecraftArguments);

            MOD_mainClass = local.readJson_mainClass(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setRunLogs("mainClass: " + MOD_mainClass);

            MOD_id = local.readJson_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setRunLogs("id: " + MOD_id);
        }

        if (MOD_inheritsFrom == null) {
            this.setRunLogs("Using: " + VersionToUse);

        } else {
            VersionToUse = MOD_inheritsFrom;
            this.setRunLogs("Using: " + VersionToUse);

        }

        //incase the url is empty.. we have to assume that the user has old path system.
        for (int i = 0; i < local.version_manifest_versions_id.size(); i++) {
            this.setRunLogs((String) local.version_manifest_versions_id.get(i));
            this.setRunLogs((String) local.version_manifest_versions_type.get(i));
            this.setRunLogs((String) local.version_manifest_versions_url.get(i));
        }

        //download 1.7.10.json_libs
        try {
            for (int i = 0; i < local.version_manifest_versions_id.size(); i++) {
                if (local.version_manifest_versions_id.get(i).equals(VersionToUse)) {
                    //we will download versionjson everytime.
                    if (HashCheck) {
                        network.downloadVersionJson(OperatingSystemToUse, local.version_manifest_versions_url.get(i).toString(), local.version_manifest_versions_id.get(i).toString());
                    }
                    break;
                } else {
                    //do nothing...
                }
            }

        } catch (Exception e) {
            this.setErrorLogs("Something went wrong downloadVersionJson" + e);
        }

        this.setRunLogs(launcherUtils.getMineCraftLocation(OperatingSystemToUse));

        local.generateVersionJsonPathList(launcherUtils.getMineCraftVersionsLocation(OperatingSystemToUse));
        local.generateVersionList(launcherUtils.getMineCraftVersionsLocation(OperatingSystemToUse));

        for (int i = 0; i < local.versions_json_path_list.size(); i++) {
            this.setRunLogs(local.versions_json_path_list.get(i).toString());
        }

        for (int i = 0; i < local.versions_list.size(); i++) {
            this.setRunLogs(local.versions_list.get(i).toString());
        }

        this.setRunLogs(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

        try {
            local.readJson_libraries_downloads_artifact_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

        } catch (Exception ex) {
            this.setErrorLogs("Unable to get libraries_downloads_artifact_url " + ex);
        }
        try {
            local.readJson_libraries_downloads_artifact_path(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

        } catch (Exception ex) {
            this.setErrorLogs("Unable to get libraries_downloads_artifact_path " + ex);
        }
        try {
            local.readJson_libraries_name(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

        } catch (Exception ex) {
            this.setErrorLogs("Unable to get libraries_name " + ex);
        }

        try {
            this.setRunLogs(local.readJson_assetIndex_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)));

        } catch (Exception ex) {
            this.setErrorLogs("Unable to get assetIndex_url" + ex);
        }
        try {
            this.setRunLogs(local.readJson_assetIndex_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)));
        } catch (Exception ex) {
            this.setErrorLogs("Unable to get assetIndex_id" + ex);
        }

        this.setRunLogs(launcherUtils.getMineCraftAssetsIndexes_X_json(OperatingSystemToUse, VersionToUse));

        try {
            local.readJson_objects_KEY(launcherUtils.getMineCraftAssetsIndexes_X_json(OperatingSystemToUse, local.readJson_assetIndex_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse))));

        } catch (Exception e) {
            this.setErrorLogs("Error reading objects KEY" + e);
        }
        try {
            local.readJson_objects_KEY_hash(launcherUtils.getMineCraftAssetsIndexes_X_json(OperatingSystemToUse, local.readJson_assetIndex_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse))));

        } catch (Exception e) {
            this.setErrorLogs("Error reading objects KEY_hash" + e);

        }

        if (HashCheck) {
            Debug.util.add(Text.util.create("Initializing hash check", Color.LIGHTGREEN, 12), true);
            try {
                for (int i = 0; i < local.objects_hash.size(); i++) {
                    launcherUtils.copyToVirtual(OperatingSystemToUse, local.objects_hash.get(i).toString().substring(0, 2), local.objects_hash.get(i).toString(), local.objects_KEY.get(i).toString());
                }
            } catch (Exception e) {
                this.setErrorLogs("Error reading objects KEY + KEY_hash" + e);
            }
        }


        this.setRunLogs("Getting NATIVES URL");
        local.readJson_libraries_downloads_classifiers_natives_X(launcherUtils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse), OperatingSystemToUse);
        this.setRunLogs("Getting NATIVES PATH");
        local.readJson_libraries_downloads_classifiers_natives_Y(launcherUtils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse), OperatingSystemToUse);

        for (int i = 0; i < local.version_url_list_natives.size(); i++) {
            this.setRunLogs("NATIVE URL: " + local.version_url_list_natives.get(i));
            //extract them here..
            this.setRunLogs("Extracting...");
            this.setRunLogs(local.version_url_list_natives.get(i).toString());
            this.setRunLogs(launcherUtils.getMineCraft_Versions_X_Natives_Location(OperatingSystemToUse, VersionToUse));

            launcherUtils.jarExtract(OperatingSystemToUse, local.version_path_list_natives.get(i).toString(), launcherUtils.getMineCraft_Versions_X_Natives_Location(OperatingSystemToUse, VersionToUse));

        }

        //String HalfArgumentTemplate = local.readJson_minecraftArguments(utils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse));
        int Xmx = this.getMemory();
        int Xms = this.getMinMemory();
        int Width = this.getWidth();
        int Height = this.getHeight();
        String JavaPath = this.getJavaPath();
        String JVMArgument = this.getJVMArgument();


        String mainClass;
        if (MOD_mainClass == null) {
            mainClass = local.readJson_mainClass(launcherUtils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse));

        } else {
            mainClass = MOD_mainClass;
        }

        String NativesDir = launcherUtils.getMineCraft_Versions_X_Natives(OperatingSystemToUse, VersionToUse);
        String assetsIdexId;
        if (MOD_assets == null) {
            assetsIdexId = local.readJson_assets(launcherUtils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse));

        } else {
            assetsIdexId = MOD_assets;
        }
        if (assetsIdexId == null) {
            assetsIdexId = "NULL";
        }

        String gameDirectory = launcherUtils.getMineCraftGameDirectoryLocation(OperatingSystemToUse);
        String AssetsRoot = launcherUtils.getMineCraftAssetsRootLocation(OperatingSystemToUse);

        String versionName;
        if (MOD_id == null) {
            versionName = local.readJson_id(launcherUtils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse));
        } else {
            versionName = MOD_id;
        }

        String authuuid = local.readJson_id(launcherUtils.getMineCraft_X_json(OperatingSystemToUse, UsernameToUse));
        String Username = UsernameToUse;
        String MinecraftJar;
        if (MOD_jar == null) {
            MinecraftJar = launcherUtils.getMineCraft_Versions_X_X_jar(OperatingSystemToUse, VersionToUse);

        } else {
            MinecraftJar = launcherUtils.getMineCraft_Versions_X_X_jar(OperatingSystemToUse, MOD_jar);
        }

        String VersionType = this.getVersionData();
        String AuthSession = "OFFLINE";

        String GameAssets = launcherUtils.getMineCraftAssetsVirtualLegacyLocation(OperatingSystemToUse);
        System.out.println("NativesPath: " + NativesDir);

        for (int i = 0; i < local.version_path_list.size(); i++) {
            local.libraries_path.add(launcherUtils.setMineCraft_librariesLocation(OperatingSystemToUse, local.version_path_list.get(i).toString()));
            System.out.println(local.libraries_path.get(i));
        }

        String HalfLibraryArgument = local.generateLibrariesArguments(OperatingSystemToUse);
        String FullLibraryArgument = local.generateLibrariesArguments(OperatingSystemToUse) + launcherUtils.getArgsDiv(OperatingSystemToUse) + MinecraftJar;
        System.out.println("HalfLibraryArgument: " + HalfLibraryArgument);
        System.out.println("FullLibraryArgument: " + FullLibraryArgument);

        //argument patch for netty and patchy comes here
        if (injectNetty) {
            System.out.println("Netty/Patchy Patch Detected!");

            String patchy_mod = "";
            String patchy = "";

            String netty_mod = "";
            String netty = "";


            try {
                Map<String, String> patchyMAP = new HashMap<String, String>(launcherUtils.getMineCraftLibrariesComMojangPatchy_jar(OperatingSystemToUse));
                for (Map.Entry<String, String> entry : patchyMAP.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    if (value.startsWith("mod_")) {
                        patchy_mod = value;
                    } else {
                        patchy = value;
                    }

                    System.out.println("KEY:::::" + key);
                    System.out.println("VALUE:::::" + value);
                }
                HalfLibraryArgument = HalfLibraryArgument.replace(patchy, patchy_mod);
                FullLibraryArgument = FullLibraryArgument.replace(patchy, patchy_mod);
            } catch (Exception ex) {
                System.out.print(ex);
            }

            try {
                Map<String, String> nettyMAP = new HashMap<String, String>(launcherUtils.getMineCraftLibrariesComMojangNetty_jar(OperatingSystemToUse));
                for (Map.Entry<String, String> entry : nettyMAP.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    if (value.startsWith("mod_")) {
                        netty_mod = value;
                    } else {
                        netty = value;
                    }

                    System.out.println("KEY:::::" + key);
                    System.out.println("VALUE:::::" + value);
                }
                HalfLibraryArgument = HalfLibraryArgument.replace(netty, netty_mod);
                FullLibraryArgument = FullLibraryArgument.replace(netty, netty_mod);

            } catch (Exception ex) {
                System.out.print(ex);
            }

        }

        //argument patch netty and patchy ends here

        String[] HalfArgument = local.generateMinecraftArguments(OperatingSystemToUse, Username, versionName, gameDirectory, AssetsRoot, assetsIdexId, authuuid, "aeef7bc935f9420eb6314dea7ad7e1e5", "{\"twitch_access_token\":[\"emoitqdugw2h8un7psy3uo84uwb8raq\"]}", "mojang", VersionType, GameAssets, AuthSession);
        //System.out.println("HalfArgument: " + HalfArgument);
        for (String HalfArgsVal : HalfArgument) {
            System.out.println("HalfArg: " + HalfArgsVal);
        }
        System.out.println("Minecraft.jar: " + MinecraftJar);

        this.setRunLogs("username: " + Username);
        this.setRunLogs("version number: " + versionName);
        this.setRunLogs("game directory: " + gameDirectory);
        this.setRunLogs("assets root directory: " + AssetsRoot);
        this.setRunLogs("assets Index Id: " + assetsIdexId);
        this.setRunLogs("assets legacy directory: " + GameAssets);
        //won't be using this
        //this.setRunLogs(local.generateRunnableArguments(Xmx, NativesDir, FullLibraryArgument, mainClass, HalfArgument));

        try {
            String cmds[] = {"-Xms" + Xms + "M", "-Xmx" + Xmx + "M", "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump", "-Djava.library.path=" + NativesDir, "-cp", FullLibraryArgument, mainClass, "--width", String.valueOf(Width), "--height", String.valueOf(Height), "--workDir", new Cache().getModpackMc().getAbsolutePath().replaceAll("\\\\", "/")};
            //put jvm arguments here
            String[] JVMArguments = JVMArgument.split(" ");
            //we now have all the arguments. merge cmds with JVMArguments
            if (!JVMArgument.isEmpty()) {
                //no need to join.
                cmds = Stream.concat(Arrays.stream(JVMArguments), Arrays.stream(cmds)).toArray(String[]::new);
            }
            String javaPathArr[] = {JavaPath};
            cmds = Stream.concat(Arrays.stream(javaPathArr), Arrays.stream(cmds)).toArray(String[]::new);

            String[] finalArgs = Stream.concat(Arrays.stream(cmds), Arrays.stream(HalfArgument)).toArray(String[]::new);
            for (String finalArgs_ : finalArgs) {
                this.setRunLogs(finalArgs_);
            }
            this.setRunLogs("Starting game... Please wait....");
            Debug.util.add(Text.util.create("Trying to run minecraft in " + new Cache().getModpackMc().getPath().replaceAll("\\\\", "/"), Color.LIGHTGREEN, 12), true);
            new AsyncScheduler(() -> {
                try {
                    Cache cache = new Cache();
                    cache.saveLaunchStatus(true);

                    DefaultExecutor executor = new DefaultExecutor();
                    executor.setWorkingDirectory(new Cache().getModpackMc());
                    String cmd = Arrays.toString(finalArgs).replace(",", "").replace("[", "").replace("]", "");
                    LogWindow window = new LogWindow();
                    window.initialize(executor);
                    executor.setStreamHandler(window);
                    try {
                        int exit_code = executor.execute(CommandLine.parse(cmd));
                        Debug.util.add(Text.util.create("Minecraft finished with exit code: " + exit_code, exit_code == 0 ? Color.LIGHTGREEN : Color.INDIANRED, 12), true);
                        cache.saveLaunchStatus(false);
                        window.initialize(null);
                    } catch (Throwable ex) {
                        Debug.util.add(Text.util.create("Minecraft finished with exit code: NOT_LAUNCHED", Color.INDIANRED, 12), true);
                        cache.saveLaunchStatus(false);
                        window.initialize(null);
                    }
                } catch (Throwable ex) {
                    Text text = new Text(ex);
                    text.format(Color.INDIANRED, 14);

                    Debug.util.add(text, true);
                    Cache cache = new Cache();
                    cache.saveLaunchStatus(false);
                }
            }).run();
        } catch (Exception ex) {
            Text text = new Text(ex);
            text.format(Color.INDIANRED, 14);

            Debug.util.add(text, true);
            Cache cache = new Cache();
            cache.saveLaunchStatus(false);
        }
    }

    private String jvmArgument = "";
     public void setJVMArgument(String jvmArgument_) {
        jvmArgument = jvmArgument_;
    }

    private String getJVMArgument() {
        return jvmArgument;
    }
    
    private String javaPath = "java";
     public void setJavaPath(String javaPath_) {
        javaPath = javaPath_;
    }

    private String getJavaPath() {
        return javaPath;
    }
    
    private int width = 854;

    public void setWidth(int width_) {
        width = width_;
    }

    private int getWidth() {
        return width;
    }
    
    private int height = 480;

    public void setHeight(int height_) {
        height = height_;
    }

    private int getHeight() {
        return height;
    }
    
    private int memory = 1024;

    public void setMemory(int memory_) {
        memory = memory_;
    }

    private int getMemory() {
        return memory;
    }

    private int minMemory = 1024;

   public void setMinMemory(int memory_) {
        minMemory = memory_;
    }

    private int getMinMemory() {
        return minMemory;
    }
    
    private String versionData = "#ammarbless";

    public void setVersionData(String versionData_) {
        versionData = versionData_;
    }

    private String getVersionData() {
        return versionData;
    }

    public void downloadVersionManifest() {
        LauncherUtils launcherUtils = new LauncherUtils();
        Network network = new Network();
        System.out.println("Downloading: version_manifest.json");
        String OperatingSystemToUse = launcherUtils.getOS();
        network.downloadVersionManifest(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));

    }

    public void downloadProfile(String UsernameToUse) {
        LauncherUtils launcherUtils = new LauncherUtils();
        Network network = new Network();
        String OperatingSystemToUse = launcherUtils.getOS();
        System.out.println("Downloading: " + UsernameToUse + ".json");
        network.downloadProfile(OperatingSystemToUse, UsernameToUse);

    }

    public void downloadMinecraft(String VersionToUse, Boolean ForceDownload) {
        LauncherUtils launcherUtils = new LauncherUtils();
        Debug.util.add(Text.util.create("Downloading minecraft in " + new Cache().getModpackMc().getPath().replaceAll("\\\\", "/"), Color.LIGHTGREEN, 12), true);
        Local local = new Local();
        Network network = new Network();
        String OperatingSystemToUse = launcherUtils.getOS();
        this.setDownloadLogs("Downlaoding: " + VersionToUse);

        //add version in launcher_profiles.json
        local.writeJson_launcher_profiles(OperatingSystemToUse, "_Cracked_" + launcherUtils.nextSessionId() + "_" + VersionToUse, VersionToUse);

        //get list of all 
        local.readJson_versions_id(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));
        local.readJson_versions_type(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));
        local.readJson_versions_url(launcherUtils.getMineCraft_Version_Manifest_json(OperatingSystemToUse));

        //declaration for mods
        String MOD_inheritsFrom = null;
        String MOD_jar = null;
        String MOD_assets = null;
        String MOD_minecraftArguments;
        String MOD_mainClass = null;
        String MOD_id = null;
        //check if it is vanilla or not
        if (local.checkIfVanillaMC(VersionToUse).equals(true)) {
            this.setRunLogs("Vanilla Minecraft found!");
        } else {
            this.setRunLogs("Modded Minecraft found!");
            local.MOD_readJson_libraries_name_PLUS_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

            this.setDownloadLogs("Fixing url using name.");

            for (int i = 0; i < local.version_name_list.size(); i++) {
                String name = local.version_name_list.get(i).toString();
                local.version_path_list.add(local.generateLibrariesPath(OperatingSystemToUse, name));
            }
            for (int i = 0; i < local.version_name_list.size(); i++) {
                String path = local.version_path_list.get(i).toString();
                if (local.getURLFromPath(path) == null) {
                    if (!path.startsWith("net/minecraftforge/forge/")) {
                        local.version_url_list.add(local.HALF_URL_version_url_list.get(i) + "/" + path);
                    } else {
                        //get forge library
                        String jar_value = path.split("/")[path.split("/").length - 1];
                        local.version_url_list.add(local.HALF_URL_version_url_list.get(i) + "/" + path.replace(jar_value, jar_value.replace(".jar", "-universal.jar")));
                    }
                } else {
                    local.version_url_list.add(local.getURLFromPath(path));
                }
            }
            for (int i = 0; i < local.version_name_list.size(); i++) {
                this.setDownloadLogs("Downloading: " + local.version_url_list.get(i));
                network.downloadLibraries(OperatingSystemToUse, local.version_url_list.get(i).toString(), local.version_path_list.get(i).toString(), ForceDownload);
            }

            MOD_inheritsFrom = local.readJson_inheritsFrom(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setDownloadLogs("inheritsFrom: " + MOD_inheritsFrom);

            MOD_jar = local.readJson_jar(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setDownloadLogs("jar: " + MOD_jar);

            MOD_assets = local.readJson_assets(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setDownloadLogs("assets: " + MOD_assets);

            MOD_minecraftArguments = local.readJson_minecraftArguments(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setDownloadLogs("minecraftArguments: " + MOD_minecraftArguments);

            MOD_mainClass = local.readJson_mainClass(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setDownloadLogs("mainClass: " + MOD_mainClass);

            MOD_id = local.readJson_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
            this.setDownloadLogs("id: " + MOD_id);
        }

        if (MOD_inheritsFrom == null) {
            this.setDownloadLogs("Using: " + VersionToUse);
        } else {
            VersionToUse = MOD_inheritsFrom;
            this.setDownloadLogs("Using: " + VersionToUse);
        }

        //incase the url is empty.. we have to assume that the user has old path system.
        for (int i = 0; i < local.version_manifest_versions_id.size(); i++) {
            this.setDownloadLogs("ID: " + local.version_manifest_versions_id.get(i));
            this.setDownloadLogs("TYPE: " + local.version_manifest_versions_type.get(i));
            this.setDownloadLogs("URL: " + local.version_manifest_versions_url.get(i));
        }

        //download 1.7.10.json_libs
        try {
            for (int i = 0; i < local.version_manifest_versions_id.size(); i++) {
                if (local.version_manifest_versions_id.get(i).equals(VersionToUse)) {
                    network.downloadVersionJson(OperatingSystemToUse, local.version_manifest_versions_url.get(i).toString(), local.version_manifest_versions_id.get(i).toString());
                    break;
                }
            }

        } catch (Exception e) {
            this.setErrorLogs("Something went wrong getting version json" + e);
        }

        this.setRunLogs(launcherUtils.getMineCraftLocation(OperatingSystemToUse));

        local.generateVersionJsonPathList(launcherUtils.getMineCraftVersionsLocation(OperatingSystemToUse));
        local.generateVersionList(launcherUtils.getMineCraftVersionsLocation(OperatingSystemToUse));

        try {
            local.readJson_libraries_downloads_artifact_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

        } catch (Exception ex) {
            this.setErrorLogs("Exception" + ex);

        }
        try {
            local.readJson_libraries_downloads_artifact_path(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

        } catch (Exception ex) {
            this.setErrorLogs("Exception" + ex);

        }
        try {
            local.readJson_libraries_name(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));

        } catch (Exception ex) {
            this.setErrorLogs("Exception" + ex);

        }
        ///************************************************************
        for (int i = 0; i < local.version_url_list.size(); i++) {
            this.setDownloadLogs("Downloading: " + local.version_url_list.get(i));
            try {
                network.downloadLibraries(OperatingSystemToUse, local.version_url_list.get(i).toString(), local.version_path_list.get(i).toString(), ForceDownload);
            } catch (Exception ex) {
                this.setErrorLogs("Due to: " + ex + " " + local.generateLibrariesPath(OperatingSystemToUse, local.version_name_list.get(i).toString()));
                local.version_path_list.add(local.generateLibrariesPath(OperatingSystemToUse, local.version_name_list.get(i).toString()));
                network.downloadLibraries(OperatingSystemToUse, local.version_url_list.get(i).toString(), local.generateLibrariesPath(OperatingSystemToUse, local.version_name_list.get(i).toString()), ForceDownload);
            }
        }

        //this may need to be edited!*************//
        this.setRunLogs(local.readJson_assetIndex_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)));
        this.setRunLogs(local.readJson_assetIndex_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)));
        //get assets index id!
        network.downloadLaunchermeta(OperatingSystemToUse, local.readJson_assetIndex_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)), local.readJson_assetIndex_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)), ForceDownload);

        this.setRunLogs(launcherUtils.getMineCraftAssetsIndexes_X_json(OperatingSystemToUse, VersionToUse));

        local.readJson_objects_KEY(launcherUtils.getMineCraftAssetsIndexes_X_json(OperatingSystemToUse, local.readJson_assetIndex_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse))));
        local.readJson_objects_KEY_hash(launcherUtils.getMineCraftAssetsIndexes_X_json(OperatingSystemToUse, local.readJson_assetIndex_id(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse))));

        for (int i = 0; i < local.objects_hash.size(); i++) {
            this.setDownloadLogs("HASH: " + local.objects_hash.get(i));
            this.setDownloadLogs("FOLDER: " + local.objects_hash.get(i).toString().substring(0, 2));
            this.setDownloadLogs("KEY: " + local.objects_KEY.get(i));

            this.setDownloadLogs("DOWNLOADING..." + "HASH: " + local.objects_hash.get(i));
            network.downloadAssetsObjects(OperatingSystemToUse, local.objects_hash.get(i).toString().substring(0, 2), local.objects_hash.get(i).toString());
            launcherUtils.copyToVirtual(OperatingSystemToUse, local.objects_hash.get(i).toString().substring(0, 2), local.objects_hash.get(i).toString(), local.objects_KEY.get(i).toString());
            //generate virtual folder as well.

        }

        this.setDownloadLogs("DOWNLOADING MINECRAFT JAR " + VersionToUse);
        if (MOD_jar == null) {
            
            int downloadMinecraftJarStatus = network.downloadMinecraftJar(OperatingSystemToUse, VersionToUse, ForceDownload) ;
            if (downloadMinecraftJarStatus == 1)
            {
                network.downloadMinecraftJar_fallBack_v1(OperatingSystemToUse, local.readJson_downloads_client_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)), VersionToUse, ForceDownload);
            }
        } else {
            //local.readJson_downloads_client_url(utils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse));
           int downloadMinecraftJarStatus = network.downloadMinecraftJar(OperatingSystemToUse, MOD_jar, ForceDownload);
           if (downloadMinecraftJarStatus == 1)
           {
                network.downloadMinecraftJar_fallBack_v1(OperatingSystemToUse, local.readJson_downloads_client_url(launcherUtils.getMineCraft_Version_Json(OperatingSystemToUse, VersionToUse)), MOD_jar, ForceDownload);
           }
        }
        
        //would have tp edit this line as we also need natives paths!
        this.setDownloadLogs("Getting NATIVES URL");
        local.readJson_libraries_downloads_classifiers_natives_X(launcherUtils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse), OperatingSystemToUse);
        this.setDownloadLogs("Getting NATIVES PATH");
        local.readJson_libraries_downloads_classifiers_natives_Y(launcherUtils.getMineCraft_Versions_X_X_json(OperatingSystemToUse, VersionToUse), OperatingSystemToUse);

        for (int i = 0; i < local.version_url_list_natives.size(); i++) {
            this.setDownloadLogs("NATIVE URL: " + local.version_url_list_natives.get(i));
            network.downloadLibraries(OperatingSystemToUse, local.version_url_list_natives.get(i).toString(), local.version_path_list_natives.get(i).toString(), ForceDownload);
            //extract them here..
            this.setRunLogs("Extracting...");
            this.setRunLogs(local.version_url_list_natives.get(i).toString());
            this.setRunLogs(launcherUtils.getMineCraft_Versions_X_Natives_Location(OperatingSystemToUse, VersionToUse));

            launcherUtils.jarExtract(OperatingSystemToUse, local.version_path_list_natives.get(i).toString(), launcherUtils.getMineCraft_Versions_X_Natives_Location(OperatingSystemToUse, VersionToUse));

        }
        this.setDownloadLogs("Download Complete!");
    }

}