package org.wso2.apim.dto;

/**
 * Registry Path DTO.
 */
public class RegistryPath {
    private int regPathId;
    private String regPathValue;
    private String provider;
    private String apiName;

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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    @Override
    public String toString() {
        return "RegistryPath{" +
                "regPathId=" + regPathId +
                ", regPathValue='" + regPathValue + '\'' +
                ", provider='" + provider + '\'' +
                ", apiName='" + apiName + '\'' +
                '}';
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
