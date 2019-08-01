package com.zendesk.connect;

import com.google.gson.annotations.SerializedName;
import com.zendesk.util.ObjectUtils;

/**
 * PushBasicMetric
 */
class PushBasicMetric {

    @SerializedName("_oid")
    private String instanceId = null;

    /**
     * @param instanceId
     */
    PushBasicMetric(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Get oid (instanceId)
     * @return oid (instanceId)
     */
    String getOid() {
        return instanceId;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PushBasicMetric pushBasicMetric = (PushBasicMetric) o;
        return ObjectUtils.equals(this.instanceId, pushBasicMetric.instanceId);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(instanceId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PushBasicMetric {\n");
        
        sb.append("    instanceId: ").append(toIndentedString(instanceId)).append("\n");
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

