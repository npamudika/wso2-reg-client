package org.wso2.apim.dto;

/**
 * Registry Path DTO.
 */
public class RegistryPath {
    private int regPathId;
    private String regPathValue;

    public String getRegPathValue() {
        return regPathValue;
    }

    public int getId() {
        return regPathId;
    }

    public void setRegPathValue(String regPathValue) {
        this.regPathValue = regPathValue;
    }

    public void setId(int regPathId) {
        this.regPathId = regPathId;
    }

    @Override
    public String toString() {
        return "RegistryPath {\n" +
                "    reg_path_id: " + toIndentedString(regPathId) + "\n" +
                "    reg_path_value: " + toIndentedString(regPathValue) + "\n" +
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
