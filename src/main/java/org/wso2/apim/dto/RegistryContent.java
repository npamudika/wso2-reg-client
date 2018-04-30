package org.wso2.apim.dto;

/**
 * Registry Content DTO.
 */
public class RegistryContent {
    private int regContentId;
    private String regPath;

    public String getRegPath() {
        return regPath;
    }

    public int getRegContentId() {
        return regContentId;
    }

    public void setRegPath(String regPath) {
        this.regPath = regPath;
    }

    public void setRegContentId(int regContentId) {
        this.regContentId = regContentId;
    }

    @Override
    public String toString() {
        return "RegistryContent {\n" +
                "    reg_content_id: " + toIndentedString(regContentId) + "\n" +
                "    reg_path: " + toIndentedString(regPath) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
