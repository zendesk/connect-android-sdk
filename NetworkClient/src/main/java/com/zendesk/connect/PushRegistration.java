package com.zendesk.connect;

import java.util.*;
import com.zendesk.util.ObjectUtils;

/**
 * PushRegistration
 */
class PushRegistration {

    private String userId = null;
    private String token = null;

    /**
     * @param userId 
     * @param token 
     */
    PushRegistration(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    /**
     * Get userId
     * @return userId
     */
    String getUserId() {
      return userId;
    }
    
    /**
     * Get token
     * @return token
     */
    String getToken() {
      return token;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PushRegistration pushRegistration = (PushRegistration) o;
        return ObjectUtils.equals(this.userId, pushRegistration.userId) &&
                ObjectUtils.equals(this.token, pushRegistration.token);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(userId, token);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PushRegistration {\n");
        
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    token: ").append(toIndentedString(token)).append("\n");
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

