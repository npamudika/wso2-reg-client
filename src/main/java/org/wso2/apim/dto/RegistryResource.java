package org.wso2.apim.dto;

/**
 * Registry Resource DTO.
 */
public class RegistryResource {
    private int regPathId;
    private String regName;
    private int regContentId;

    public String getRegName() {
        return regName;
    }

    public int getRegContentId() {
        return regContentId;
    }

    public int getId() {
        return regPathId;
    }

    public void setRegName(String regName) {
        this.regName = regName;
    }

    public void setRegContentId(int regContentId) {
        this.regContentId = regContentId;
    }

    public void setId(int regPathId) {
        this.regPathId = regPathId;
    }

    @Override
    public String toString() {
        return "RegistryResource {\n" +
                "    reg_path_id: " + toIndentedString(regPathId) + "\n" +
                "    reg_name: " + toIndentedString(regName) + "\n" +
                "    reg_content_id: " + toIndentedString(regContentId) + "\n" +
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
