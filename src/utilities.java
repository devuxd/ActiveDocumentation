import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by saharmehrpour on 7/31/17.
 */

public class utilities {

    // determines if one file/directory is stored somewhere down the line in another directory
    static boolean isFileAChildOf(VirtualFile maybeChild, VirtualFile possibleParent) {
        final VirtualFile parent = possibleParent.getCanonicalFile();
        if (!parent.exists() || !parent.isDirectory()) {
            // this cannot possibly be the parent
            return false;
        }

        VirtualFile child = maybeChild.getCanonicalFile();
        while (child != null) {
            if (child.equals(parent)) {
                return true;
            }
            child = child.getParent();
        }
        // No match found, and we've hit the root directory
        return false;
    }

    static List<VirtualFile> createIgnoredFileList() {
        List<VirtualFile> ignoredFiles = new ArrayList<>();
        List<String> files = new ArrayList<>(Arrays.asList(".idea", "out", "source_xml.xml", "tempResultXmlFile.xml",
                "testProject.iml", ".DS_Store", "bin"));
        for (String f : files) {
            VirtualFile vfile = ProjectManager.getInstance().getOpenProjects()[0].getBaseDir().findFileByRelativePath(f);
            if (vfile != null) {
                ignoredFiles.add(vfile);
            }
        }
        return ignoredFiles;
    }


}
