package juegojava;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public final class ResourceLoader {
    private ResourceLoader() {}

    public static Image loadImage(String resourcePath) {
        File file = findFile(resourcePath);
        if (file == null) {
            return null;
        }
        return new ImageIcon(file.getPath()).getImage();
    }

    public static File findFile(String resourcePath) {
        List<String> candidates = new ArrayList<>();
        candidates.add(resourcePath);

        String normalized = resourcePath.replace("\\", "/");
        if (!normalized.startsWith("Resources/")) {
            candidates.add("Resources/" + normalized);
        }
        candidates.add("./" + normalized);
        candidates.add("../" + normalized);
        if (!normalized.startsWith("Resources/")) {
            candidates.add("./Resources/" + normalized);
            candidates.add("../Resources/" + normalized);
        }

        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
}
