package ml.karmaconfigs.modpackupdater.utils.modpack;

import ml.karmaconfigs.modpackupdater.utils.Utils;
import ml.karmaconfigs.modpackupdater.utils.files.CustomFile;
import ml.karmaconfigs.modpackupdater.utils.files.FilesUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class ListMods implements Runnable {

    private final static HashSet<File> staticMods = new HashSet<>();
    private static String url;
    private final Utils utils = new Utils();

    public ListMods(String url) {
        ListMods.url = url;
    }

    @Override
    public void run() {
        File modFolder = new File(FilesUtilities.getMinecraftDir() + "/mods");
        if (!modFolder.exists()) {
            if (modFolder.mkdirs()) {
                utils.setDebug(utils.rgbColor("Created mods folder", 255, 100, 100), true);
            }
        }

        try {
            URL downloadURL = new URL(url);

            File listCache = new File(FilesUtilities.getUpdaterDir(), "mod_list_cache.txt");

            if (listCache.exists() && listCache.delete()) {
                utils.setDebug(utils.rgbColor("Removed old mod_list_cache.txt file", 155, 240, 175), true);
            }

            ReadableByteChannel rbc = Channels.newChannel(downloadURL.openStream());
            FileOutputStream fos = new FileOutputStream(listCache);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            CustomFile file = new CustomFile(listCache, false);

            List<Object> modNames = file.getList("MODS");
            int mods = 0;
            for (Object modName : modNames) {
                mods++;
                File modFile = new File(FilesUtilities.getMinecraftDir() + "/mods/" + modName);

                if (modFile.exists()) {
                    utils.setDebug(utils.rgbColor("( " + mods + " ) " + FilesUtilities.getPath(modFile), 155, 240, 175), mods == 1);
                } else {
                    utils.setDebug(utils.rgbColor("( " + mods + " ) " + FilesUtilities.getPath(modFile), 220, 100, 100), mods == 1);
                }
            }
            rbc.close();
            fos.close();
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    private void list() {
        File modFolder = new File(FilesUtilities.getMinecraftDir() + "/mods");
        if (!modFolder.exists()) {
            if (modFolder.mkdirs()) {
                utils.setDebug(utils.rgbColor("Created mods folder", 255, 100, 100), true);
            }
        }

        try {
            URL downloadURL = new URL(url);

            File listCache = new File(FilesUtilities.getUpdaterDir(), "mod_list_cache.txt");

            if (listCache.exists() && listCache.delete()) {
                utils.setDebug(utils.rgbColor("Removed old mod_list_cache.txt file", 155, 240, 175), true);
            }

            ReadableByteChannel rbc = Channels.newChannel(downloadURL.openStream());
            FileOutputStream fos = new FileOutputStream(listCache);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            CustomFile file = new CustomFile(listCache, false);

            List<Object> modNames = file.getList("MODS");
            for (Object modName : modNames) {
                File modFile = new File(FilesUtilities.getMinecraftDir() + "/mods/" + modName);

                staticMods.add(modFile);
            }
            rbc.close();
            fos.close();
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    public ArrayList<File> getMods() {
        list();
        ArrayList<File> mods = new ArrayList<>();
        for (File file : staticMods) {
            if (!mods.contains(file)) {
                mods.add(file);
            }
        }
        return mods;
    }
}
