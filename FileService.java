package Lam.camu.p100fx.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.io.File;

import Lam.camu.p100fx.model.ParametFile;
import Lam.camu.p100fx.model.DataBlock;
import Lam.camu.p100fx.util.ParametUtils;

public class FileService {
    private static final String RECENT_FILES_KEY = "recentFiles";
    private static final int MAX_RECENT = 10;

    private Preferences prefs;

    public FileService() {
        prefs = Preferences.userNodeForPackage(FileService.class);
    }

    public void save(ParametFile file, Path path) throws IOException {
        file.saveToFile(path.toString());
        addRecentFile(path);
    }

    public ParametFile load(Path path) throws IOException {
        ParametFile file = ParametFile.loadFromFile(path.toString());
        addRecentFile(path);
        return file;
    }

    public void exportBlockToPNG(DataBlock block, Path path) throws IOException {
        // Sử dụng ParametUtils.exportBlockToPNG (giả sử đã có)
        ParametUtils.exportBlockToPNG(block, path.toString());
    }

    // --- Recent files management ---
    private void addRecentFile(Path path) {
        List<Path> recents = getRecentFiles();
        recents.remove(path); // xóa nếu đã có
        recents.add(0, path); // thêm lên đầu
        if (recents.size() > MAX_RECENT) {
            recents = recents.subList(0, MAX_RECENT);
        }
        // Lưu vào Preferences dưới dạng chuỗi
        String[] paths = recents.stream().map(Path::toString).toArray(String[]::new);
        prefs.put(RECENT_FILES_KEY, String.join(File.pathSeparator, paths));
    }

    public List<Path> getRecentFiles() {
        String value = prefs.get(RECENT_FILES_KEY, "");
        if (value.isEmpty()) return new ArrayList<>();
        String[] parts = value.split(File.pathSeparator);
        List<Path> list = new ArrayList<>();
        for (String p : parts) {
            list.add(Paths.get(p));
        }
        return list;
    }
}
