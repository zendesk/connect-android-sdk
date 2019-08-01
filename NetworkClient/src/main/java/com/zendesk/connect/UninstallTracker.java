package com.zendesk.connect;

import com.google.gson.annotations.SerializedName;
import com.zendesk.util.ObjectUtils;

/**
 * UninstallTracker
 */
class UninstallTracker {

    @SerializedName("i")
    private String instanceId = null;
    private boolean revoked;

    /**
     * @param i 
     * @param revoked 
     */
    UninstallTracker(String i, boolean revoked) {
        this.instanceId = i;
        this.revoked = revoked;
    }

    /**
     * Get instanceId
     * @return instanceId
     */
    String getI() {
        return instanceId;
    }
    
    /**
     * Get revoked
     * @return revoked
     */
    boolean isRevoked() {
        return revoked;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UninstallTracker uninstallTracker = (UninstallTracker) o;
        return ObjectUtils.equals(this.instanceId, uninstallTracker.instanceId)
                && ObjectUtils.equals(this.revoked, uninstallTracker.revoked);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(instanceId, revoked);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UninstallTracker {\n");
        
        sb.append("    instanceId: ").append(toIndentedString(instanceId)).append("\n");
        sb.append("    revoked: ").append(toIndentedString(revoked)).append("\n");
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

