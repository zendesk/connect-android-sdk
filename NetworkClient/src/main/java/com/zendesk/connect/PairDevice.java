package com.zendesk.connect;

import java.util.*;

import com.google.gson.annotations.SerializedName;
import com.zendesk.util.ObjectUtils;

/**
 * PairDevice
 */
class PairDevice {

    private long code;
    @SerializedName("deviceToken")
    private String deviceToken = null;
    @SerializedName("deviceName")
    private String deviceName = null;

    /**
     * @param code 
     * @param deviceToken 
     * @param deviceName 
     */
    PairDevice(long code, String deviceToken, String deviceName) {
        this.code = code;
        this.deviceToken = deviceToken;
        this.deviceName = deviceName;
    }

    /**
     * Get code
     * @return code
     */
    long getCode() {
      return code;
    }
    
    /**
     * Get deviceToken
     * @return deviceToken
     */
    String getDeviceToken() {
      return deviceToken;
    }
    
    /**
     * Get deviceName
     * @return deviceName
     */
    String getDeviceName() {
      return deviceName;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PairDevice pairDevice = (PairDevice) o;
        return ObjectUtils.equals(this.code, pairDevice.code) &&
                ObjectUtils.equals(this.deviceToken, pairDevice.deviceToken) &&
                ObjectUtils.equals(this.deviceName, pairDevice.deviceName);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(code, deviceToken, deviceName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PairDevice {\n");
        
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    deviceToken: ").append(toIndentedString(deviceToken)).append("\n");
        sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
          return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

