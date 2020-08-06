package ml.karmaconfigs.ModPackUpdater.Utils.Launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ml.karmaconfigs.ModPackUpdater.Utils.Files.FilesUtilities;
import ml.karmaconfigs.ModPackUpdater.Utils.ModPack.Modpack;
import ml.karmaconfigs.ModPackUpdater.Utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;

public final class Profiler {

    private final File profileFile = new File(FilesUtilities.getMinecraftDir(), "launcher_profiles.json");
    private final Modpack modpack;

    private final static String icon = "Crafting_Table";
    private final static String date = "1970-01-01T00:00:00.000Z";
    private final static String type = "custom";

    private final static Utils utils = new Utils();

    /**
     * Initialize the profile creator
     *
     * @param modpack the modpack
     */
    public Profiler(Modpack modpack) {
        if (!profileFile.exists()) {
            try {
                if (profileFile.createNewFile()) {
                    System.out.println("Executed");
                }
            } catch (Throwable e) {
                utils.log(e);
            }
        }
        this.modpack = modpack;
    }

    /**
     * Insert the modpack profile
     * into the launcher
     */
    @SuppressWarnings("all")
    /*
    Dues this is using an old API, I have
    to supress old API usage warnings
     */
    public final void insert() {
        try {
            utils.setDebug(utils.rgbColor("Trying to insert launcher profile for modpack " + modpack.getName(), 155, 240, 175), true);

            JSONObject newProfile = new JSONObject();
            newProfile.put("icon", icon);
            newProfile.put("lastUsed", date);
            newProfile.put("lastVersionId", modpack.getVersionName());
            newProfile.put("name", modpack.getName());
            newProfile.put("type", type);

            JSONObject original = getJSON(profileFile);
            JSONObject profiles;
            if (original.containsKey("profiles")) {
                profiles = (JSONObject) original.get("profiles");

                profiles.remove(modpack.getName());
                int amount = 0;
                for (Object val : profiles.keySet()) {
                    amount++;
                    utils.setDebug(utils.rgbColor("Detected profile " + val + " saving it...", 155, 240, 175), amount == 1);

                    JSONObject saved = (JSONObject) profiles.get(val);
                    profiles.put(val, saved);
                }
            } else {
                profiles = new JSONObject();
            }

            profiles.put(modpack.getName(), newProfile);
            original.put("profiles", profiles);


            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(original);

            Files.write(profileFile.toPath(), jsonOutput.getBytes());
        } catch (Throwable e) {
            utils.log(e);
        }
    }

    private static JSONObject getJSON(File file) throws Exception {
        FileReader reader = new FileReader(file);
        JSONParser jsonParser = new JSONParser();
        return (JSONObject) jsonParser.parse(reader);
    }
}