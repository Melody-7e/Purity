package com.ri.meta;

import static com.ri.meta.Projects.PROJECT_DIR;

import java.io.File;
import java.util.Objects;

// state([Type] <category> id) Name
public class ProjectName {
    private final ProjectType type;
    private final ProjectPD pd;
    private final ProjectCategory category;
    private final byte id;
    private final String name;
    private final ProjectState state;

    private final String fullName;
    private final String urlSafeName;

    public ProjectName(ProjectType type, ProjectPD pd, ProjectCategory category, byte id, String name, ProjectState state) {
        // @formatter:off
        this.type     = type;
        this.pd       = pd;
        this.category = category;
        this.id       = id;
        this.name     = (name == null) ? null : name.trim();
        this.state    = state;
        // @formatter:on

        fullName = String.format("([%s%s]%s <%02x> %02x) %s", type.getCode(), pd.symbol(), state.symbol(), category.value(), id, this.name).trim();
        urlSafeName = String.format("([%s%s]%s %02x-%02x) %s", type.getCode(), pd.symbol(), state.symbol(), category.value(), id, this.name).trim();
    }

    public static ProjectName valueOf(String string) {
        ProjectType type;
        ProjectPD pd;
        ProjectCategory category;
        byte id;
        String name;
        ProjectState state;

        if (!string.startsWith("([")) throw new RuntimeException("Invalid Name");



        int baseNameLen = string.indexOf(')');

        String baseName = string.substring(2, baseNameLen); // TypePd]State Category-Id  or TypePd]State <Category> Id
        name = string.substring(baseNameLen + 1);

        if (name.isEmpty()) name = null;

        boolean isUrl = baseName.contains(" ");

        int typePdLen = baseName.lastIndexOf(']');
        int stateStringLen = baseName.indexOf(' ');
        int categoryLen = baseName.indexOf(isUrl ? '-' : ' ', stateStringLen);

        String typePdString = baseName.substring(0, typePdLen);
        String stateString = baseName.substring(typePdLen + 1, stateStringLen);
        String categoryString = baseName.substring(stateStringLen + 1, categoryLen);
        String idString = baseName.substring(categoryLen + 1);

        {
            int[] index = new int[1];
            type = ProjectType.valueOf(typePdString, index);

            String pdString = typePdString.substring(index[0]);
            pd = ProjectPD.fromSymbol(pdString);
        }

        {
            state = ProjectState.fromSymbol(stateString);

            if (categoryString.startsWith("<") && categoryString.endsWith(">")) {
                if (isUrl) throw new RuntimeException("Invalid Name");
                categoryString = categoryString.substring(1, categoryString.length() - 1);
            }

            category = ProjectCategory.fromValue((byte) Integer.parseInt(categoryString, 16));

            id = (byte) Integer.parseInt(idString, 16);
        }

        return new ProjectName(type, pd, category, id, name, state);
    }

    public ProjectType getType() {
        return type;
    }

    public ProjectPD getPd() {
        return pd;
    }

    public ProjectCategory getCategory() {
        return category;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProjectState getState() {
        return state;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUrlSafeName() {
        return urlSafeName;
    }

    public File getFile(String extension, String... extras) {
        if (extension.startsWith(".")) extension = extension.substring(1);

        StringBuilder extra = new StringBuilder();

        if (extras.length == 1) {
            extra.append(' ').append(extras[0]);
        } else if (extras.length > 1) {
            extra.append(" (");
            for (int i = 0; i < extras.length - 1; i++) {
                extra.append(extras[i]).append(";");
            }
            extra.append(extras[extras.length - 1]);
            extra.append(')');
        }

        int i = 0;
        File file;
        do {
            file = new File(PROJECT_DIR, urlSafeName + extra + ((i != 0) ? " #" + i : "") + '.' + extension);
            i++;
        } while (file.exists());

        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectName that = (ProjectName) o;
        return Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fullName);
    }

    @Override
    public String toString() {
        return fullName;
    }
}
