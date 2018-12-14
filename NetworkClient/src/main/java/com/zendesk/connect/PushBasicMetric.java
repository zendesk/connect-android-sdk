package com.zendesk.connect;

import java.util.*;
import com.zendesk.util.ObjectUtils;

/**
 * PushBasicMetric
 */
class PushBasicMetric {

    private String _oid = null;

    /**
     * @param _oid 
     */
    PushBasicMetric(String _oid) {
        this._oid = _oid;
    }

    /**
     * Get _oid
     * @return _oid
     */
    String getOid() {
      return _oid;
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
        return ObjectUtils.equals(this._oid, pushBasicMetric._oid);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(_oid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PushBasicMetric {\n");
        
        sb.append("    _oid: ").append(toIndentedString(_oid)).append("\n");
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

