package ml.karmaconfigs.ModPackUpdater.Utils;

import lombok.SneakyThrows;
import ml.karmaconfigs.ModPackUpdater.MainFrame;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.CopyFile;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.CustomFile;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Downloader;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.ListMods;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Utils extends MainFrame implements Runnable {

    public Utils() {
        if (!FilesUtilities.getUpdaterDir().exists()) {
            executeBoolean(FilesUtilities.getUpdaterDir().mkdirs());
        }
        File data = new File(FilesUtilities.getUpdaterDir() + "/modpacks");
        if (!data.exists()) {
            executeBoolean(data.mkdirs());
        }
        File logs = new File(FilesUtilities.getUpdaterDir() + "/logs");
        if (!logs.exists()) {
            executeBoolean(logs.mkdirs());
        }
        File downloads = new File(FilesUtilities.getUpdaterDir() + "/downloads");
        if (!downloads.exists()) {
            executeBoolean(downloads.mkdirs());
        }
        File uploads = new File(FilesUtilities.getUpdaterDir() + "/uploads");
        if (!uploads.exists()) {
            executeBoolean(uploads.mkdirs());
        }
    }

    public interface os {
        static String getOS() {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

            if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                return ("Mac");
            } else if (OS.contains("win")) {
                return ("Windows");
            } else if (OS.contains("nux")) {
                return ("Linux");
            } else {
                return ("Linux");
            }
        }
    }

    public final boolean ModExists(File mod) {
        return mod.exists();
    }
    
    public final void log(Throwable throwable) {
        try {
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            String date = dateFormat.format(today).replace("/", "-");
            File logsFolder = new File(FilesUtilities.getUpdaterDir() + "/logs");
            if (!logsFolder.exists()) {
                executeBoolean(logsFolder.mkdirs());
            }
            File newLog = new File(logsFolder, "log_" + date + ".txt");

            if (!newLog.exists()) {
                executeBoolean(newLog.createNewFile());
            }

            StringBuilder builder = new StringBuilder();
            for (StackTraceElement info : throwable.getStackTrace()) {
                builder.append(info.toString()).append("<br>");
            }

            setDebug(rgbColor("Exception: " + new Throwable(throwable).fillInStackTrace(), 255, 100, 100), true);
            setDebug(rgbColor(builder.toString(), 120, 100, 100), false);
            setDebug(rgbColor("Log " + FilesUtilities.getPath(newLog) + " have been saved", 120, 200, 155), false);

            modifyLog(newLog, "Exception info: " + new Throwable(throwable).fillInStackTrace(), throwable);
        } catch (Throwable e) {
            StringBuilder exception = new StringBuilder();

            for (StackTraceElement element : e.getStackTrace()) {
                exception.append(element).append("<br>");
            }

            setDebug(rgbColor("Exception: " + e.fillInStackTrace(), 255, 100, 100), true);
            setDebug(rgbColor(exception.toString(), 120, 100, 100), false);
        }
    }

    private static String baseURL = "";
    private static String name = "";
    private static boolean asZip = false;
    private static boolean includeShaders = false;
    private static boolean includeTextures = false;

    public final void setupCreator(String baseUrl, String modpackName, boolean zip, boolean withShaders, boolean withTextures) {
        baseURL = baseUrl;
        name = modpackName;
        asZip = zip;
        includeShaders = withShaders;
        includeTextures = withTextures;
    }

    @SneakyThrows
    @Override
    public void run() {
        Modpack modpack = new Modpack(name);
        modpack.createFile();
        if (!name.isEmpty()) {
            modpack.getFile().write("NAME", name);
        } else {
            modpack.getFile().write("NAME", "Null name");
        }
        modpack.getFile().write("DOWNLOAD", baseURL + "/download.txt");
        modpack.getFile().write("SHADERS", includeShaders);
        modpack.getFile().write("TEXTURES", includeTextures);

        delZip();
        deleteInModFolder(modpack);
        setDebug(rgbColor("Creating modpack download file...", 155, 240, 175), false);
        File modsFolder = new File(mcFolder + "/mods");
        if (modsFolder.exists()) {
            File[] mods = modsFolder.listFiles();

            ArrayList<String> urls = new ArrayList<>();
            ArrayList<String> modNames = new ArrayList<>();
            if (mods != null && !Arrays.asList(mods).isEmpty()) {
                int modsAmount = 0;
                for (File mod : mods) {
                    if (Downloader.isMod(mod)) {
                        modNames.add(mod.getName());
                        modsAmount++;
                        String url;
                        if (!asZip) {
                            url = baseURL + "/download/" + mod.getName() + "=" + mod.getName();
                            setDebug(rgbColor("Mod detected " + FilesUtilities.getPath(mod) + " ( <span style=\"color: rgb(95, 210, 210);\">" + url.split("=")[0] + "</span> )", 95, 140, 210), false);
                            urls.add(url);
                        } else {
                            CopyFile copy = new CopyFile(mod, null, false, false);
                            copy.copy(modpack, "mods");
                            setDebug(rgbColor("Mod detected " + FilesUtilities.getPath(mod) + " ( <span style=\"color: rgb(95, 210, 210);\"> Will be added to modpack.zip </span> )", 95, 140, 210), false);
                        }
                    }
                }

                setDebug(rgbColor("Detected a total of " + modsAmount + " mods", 120, 200, 155), false);

                if (includeTextures) {
                    includeTextures = copyTexturePacks(modpack);
                }
                if (includeShaders) {
                    includeShaders = copyShaderPacks(modpack);
                }

                if (asZip) {
                    CopyFile copy = new CopyFile(null, modpack, includeTextures, includeShaders);
                    Thread thread = new Thread(copy, "Zipping");
                    thread.start();
                }
            } else {
                throw new Exception("Mods folder is null or empty");
            }

            File dlFile = new File(FilesUtilities.getModpackUploadDir(modpack), "download.txt");

            if (!dlFile.exists()) {
                if (dlFile.createNewFile()) {
                    setDebug(rgbColor("Created file " + dlFile.getName(), 120, 200, 155), false);
                }
            }

            CustomFile file = new CustomFile(dlFile, true);
            if (!name.isEmpty()) {
                file.write("NAME", name);
            }
            file.write("DOWNLOAD", baseURL + "/download.txt");
            file.write("SHADERS", includeShaders);
            file.write("TEXTURES", includeTextures);
            if (!asZip) {
                file.write("URLS", urls);
                modpack.getFile().write("URLS", urls);
            } else {
                file.write("URL", baseURL + "/download/modpack.zip");
                modpack.getFile().write("URL", baseURL + "/download/modpack.zip");
            }
            file.write("MODS", modNames);
            modpack.getFile().write("MODS", modNames);
        } else {
            setDebug(rgbColor("Couldn't find mods folder, is it the minecraft folder? ( <span style=\"color: rgb(100, 100, 255);\">" + FilesUtilities.getPath(mcFolder) + "</span> )", 120, 100, 100), false);
        }
    }

    public final void setDebug(String newLine, boolean doubleLine) {
        String oldText = bPane.getText().replace("<html>", "").replace("</html>", "");

        String separator = "<br>";
        if (doubleLine) {
            separator = "<br><br>";
        }
        bPane.setText("<html><div>" + oldText + separator + newLine + "</div></html>");
        jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getMaximum());
    }

    public final boolean isOutdated(String url) {
        int error = 0;

        ListMods listMods = new ListMods(url);
        ArrayList<File> listedMods = listMods.getMods();
        if (!listedMods.isEmpty()) {
            for (File listedMod : listedMods) {
                if (Downloader.isMod(listedMod)) {
                    if (!ModExists(listedMod)) {
                        error++;
                    }
                }
            }
        }

        return error > 0;
    }

    private int blankPacks() {
        int amount = 0;

        File[] packs = new File(FilesUtilities.getUpdaterDir() + "/modpacks/").listFiles();
        if (packs != null && !Arrays.asList(packs).isEmpty()) {
            for (File pack : packs) {
                String name = pack.getName();
                if (name.contains("_")) {
                    if (name.split("_")[0].equals("blank")) {
                        amount++;
                    }
                }
            }
        }

        return amount;
    }

    public final String rgbColor(String text, int red, int blue, int green) {
        return "<span style=\"color: rgb({red}, {blue}, {green});\">".replace("{red}", String.valueOf(red)).replace("{blue}", String.valueOf(blue)).replace("{green}", String.valueOf(green)) + text + "</span>";
    }

    public final String rgbColor(ArrayList<String> list, int red, int blue, int green) {
        return "<span style=\"color: rgb({red}, {blue}, {green});\">".replace("{red}", String.valueOf(red)).replace("{blue}", String.valueOf(blue)).replace("{green}", String.valueOf(green)) + list.toString().replace("[", "").replace("]", "").replace(",", "<br>") + "</span>";
    }

    public final String rgbColor(HashMap<String, String> map, int redForKey, int blueForKey, int greenForKey, int redForValue, int blueForValue, int greenForValue) {
        ArrayList<String> toString = new ArrayList<>();

        for (String key : map.keySet()) {
            toString.add("<span style=\"color: rgb(" + redForKey + ", " + blueForKey + ", " + greenForKey + ");\">" + key + "</span>");
            toString.add("<span style=\"color: rgb(" + redForValue + ", " + blueForValue + ", " + greenForValue + ");\">" + map.get(key) + "</span>");
        }

        return "<div>" + toString.toString().replace("[", "").replace("]", "").replace(",", "<br>") + "</div>";
    }
    
    public final String getModpackName(String url) throws Throwable {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new URL(url).openStream()));

        String name = "";
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains(":")) {
                String path = inputLine.split(":")[0];
                String value = inputLine.replace(path + ": ", "");

                if (path.equals("NAME")) {
                    name = value;
                    break;
                }
            }
        }

        if (name.isEmpty()) name = "blank_" + blankPacks();

        return name;
    }

    public final void setProgress(String title, int progress) {
        bar.setValue(progress);
        barLabel.setText(title + " [ " + progress + "% ]");

        bar.setString(title + "[ " + progress + "% ]");
        bar.setStringPainted(true);
        bar.setValue(progress);
    }

    private void deleteInModFolder(Modpack modpack) {
        File inModFolder = new File(FilesUtilities.getModpackUploadDir(modpack) + "/mods/");
        if (inModFolder.exists()) {
            if (inModFolder.delete()) {
                setDebug(rgbColor("Removed old in-mods folder", 155, 240, 175), true);
            } else {
                File[] mods = inModFolder.listFiles();
                if (mods != null) {
                    for (File mod : mods) {
                        if (mod.delete()) {
                            setDebug(rgbColor("Removed old in-mod file " + mod.getName(), 155, 240, 175), true);
                        } else {
                            setDebug(rgbColor("Couldn't remove old in-mod file " + mod.getName(), 220, 100, 100), true);
                        }
                    }
                }
            }
        }
    }

    private void deleteInTextureFolder(Modpack modpack) {
        File inModFolder = new File(FilesUtilities.getModpackUploadDir(modpack) + "/texturepacks/");
        if (inModFolder.exists()) {
            if (inModFolder.delete()) {
                setDebug(rgbColor("Removed old in-textures folder", 155, 240, 175), true);
            } else {
                File[] mods = inModFolder.listFiles();
                if (mods != null) {
                    for (File mod : mods) {
                        if (mod.delete()) {
                            setDebug(rgbColor("Removed old in-texture file " + mod.getName(), 155, 240, 175), true);
                        } else {
                            setDebug(rgbColor("Couldn't remove old in-texture file " + mod.getName(), 220, 100, 100), true);
                        }
                    }
                }
            }
        }
    }

    private void deleteInShaderFolder(Modpack modpack) {
        File inModFolder = new File(FilesUtilities.getModpackUploadDir(modpack) + "/shaderpacks/");
        if (inModFolder.exists()) {
            if (inModFolder.delete()) {
                setDebug(rgbColor("Removed old in-shader folder", 155, 240, 175), true);
            } else {
                File[] mods = inModFolder.listFiles();
                if (mods != null) {
                    for (File mod : mods) {
                        if (mod.delete()) {
                            setDebug(rgbColor("Removed old in-shader file " + mod.getName(), 155, 240, 175), true);
                        } else {
                            setDebug(rgbColor("Couldn't remove old in-shader file " + mod.getName(), 220, 100, 100), true);
                        }
                    }
                }
            }
        }
    }

    private void delZip() {
        File destZip = new File(FilesUtilities.getUpdaterDir(), "modpack.zip");
        if (destZip.exists()) {
            if (destZip.delete()) {
                setDebug(rgbColor("Removed old modpack.zip", 155, 240, 175), true);
            } else {
                setDebug(rgbColor("Couldn't remove modpack.zip", 220, 100, 100), true);
            }
        }
    }

    private void executeBoolean(boolean bool) {
        try {
            if (bool) {
                System.out.println("Executed");
            }
        } catch (Throwable ignored) {}
    }

    private void modifyLog(File logFile, String firstLine, Throwable info) {
        InputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            Date now = new Date();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");

            String prefix = "[ " + dateFormat.format(now) + " ] ";

            in = new FileInputStream(logFile);
            inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            reader = new BufferedReader(inReader);

            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line + "\n");
            }

            lines.add(prefix + firstLine + "\n\n");
            StackTraceElement[] elements = info.getStackTrace();
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                if (i != elements.length - 1) {
                    lines.add(element + "\n");
                } else {
                    lines.add(element + "\n\n");
                }
            }

            FileWriter writer = new FileWriter(logFile);
            for (int i = 0; i < lines.size(); i++) {
                if (i != lines.size() - 1) {
                    writer.write(lines.get(i));
                } else {
                    writer.write(lines.get(i) + "\n-------------------------------------------------------------------");
                }
            }
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            StringBuilder exception = new StringBuilder();

            for (StackTraceElement element : e.getStackTrace()) {
                exception.append(element).append("<br>");
            }

            setDebug(rgbColor("Exception: " + e.fillInStackTrace(), 255, 100, 100), true);
            setDebug(rgbColor(exception.toString(), 120, 100, 100), false);
        } finally {
            try {
                if (in != null) {
                    in.close();
                    if (inReader != null) {
                        inReader.close();
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    private boolean copyShaderPacks(Modpack modpack) {
        deleteInShaderFolder(modpack);
        setDebug(rgbColor("Looking for shaderpacks packs...", 155, 240, 175), false);
        File shadersFolder = new File(mcFolder + "/shaderpacks");
        if (shadersFolder.exists()) {
            File[] shaderPacks = shadersFolder.listFiles();
            if (shaderPacks != null && !Arrays.asList(shaderPacks).isEmpty()) {
                HashMap<String, String> debugData = new HashMap<>();
                int shadersAmount = 0;
                for (File shaderpack : shaderPacks) {
                    if (Downloader.isZip(shaderpack)) {
                        shadersAmount++;
                        CopyFile copy = new CopyFile(shaderpack, null, false, false);
                        copy.copy(modpack, "shaderpacks");
                        debugData.put("Detected shaderpack " + FilesUtilities.getPath(shaderpack), "Shaderpack " + shaderpack.getName() + " have been added to modpack.zip");
                    }
                }

                setDebug(rgbColor(debugData, 100, 100, 255, 155, 0, 155), false);
                setDebug(rgbColor("Detected a total of " + shadersAmount + " shaderpacks", 120, 200, 155), false);
                return true;
            }
        } else {
            setDebug(rgbColor("Couldn't find shaderpacks folder, is it the minecraft folder? ( <span style=\"color: rgb(100, 100, 255);\">" + FilesUtilities.getPath(mcFolder) + "</span> )", 120, 100, 100), false);
        }
        return false;
    }

    private boolean copyTexturePacks(Modpack modpack) {
        deleteInTextureFolder(modpack);
        setDebug(rgbColor("Looking for texture packs...", 155, 240, 175), false);
        File texturesFolder = new File(mcFolder + "/resourcepacks");
        if (texturesFolder.exists()) {
            File[] texturePacks = texturesFolder.listFiles();
            if (texturePacks != null && !Arrays.asList(texturePacks).isEmpty()) {
                HashMap<String, String> debugData = new HashMap<>();
                int texturesAmount = 0;
                for (File texturepack : texturePacks) {
                    if (Downloader.isZip(texturepack)) {
                        texturesAmount++;
                        CopyFile copy = new CopyFile(texturepack, null, false, false);
                        copy.copy(modpack, "resourcepacks");
                        debugData.put("Detected texturepack " + FilesUtilities.getPath(texturepack), "Texturepack " + texturepack.getName() + " have been added to modpack.zip");
                    }
                }

                setDebug(rgbColor(debugData, 100, 100, 255, 155, 0, 155), false);
                setDebug(rgbColor("Detected a total of " + texturesAmount + " texturepacks", 120, 200, 155), false);
                return true;
            }
        } else {
            setDebug(rgbColor("Couldn't find texturepacks folder, is it the minecraft folder? ( <span style=\"color: rgb(100, 100, 255);\">" + FilesUtilities.getPath(mcFolder) + "</span> )", 120, 100, 100), false);
        }
        return false;
    }
}