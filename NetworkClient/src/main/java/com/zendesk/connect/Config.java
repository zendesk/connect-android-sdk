package com.zendesk.connect;

import java.util.*;
import com.zendesk.util.ObjectUtils;
/**
 * Config
 */
public class Config {
  private boolean enabled;

  private Object sdk = null;

  private Object account = null;

   /**
   * Get enabled
   * @return enabled
  **/
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Set enabled
   * @param enabled
  **/
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

   /**
   * Get sdk
   * @return sdk
  **/
  public Object getSdk() {
    return sdk;
  }

  /**
   * Set sdk
   * @param sdk
  **/
  public void setSdk(Object sdk) {
    this.sdk = sdk;
  }

   /**
   * Get account
   * @return account
  **/
  public Object getAccount() {
    return account;
  }

  /**
   * Set account
   * @param account
  **/
  public void setAccount(Object account) {
    this.account = account;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Config config = (Config) o;
    return ObjectUtils.equals(this.enabled, config.enabled) &&
        ObjectUtils.equals(this.sdk, config.sdk) &&
        ObjectUtils.equals(this.account, config.account);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hash(enabled, sdk, account);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Config {\n");
    
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    sdk: ").append(toIndentedString(sdk)).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
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

