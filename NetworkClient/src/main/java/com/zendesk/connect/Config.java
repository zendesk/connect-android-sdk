package com.zendesk.connect;

import com.zendesk.util.ObjectUtils;

/**
 * Config
 */
class Config {

    private final boolean enabled;
    private final AccountConfig account;

    /**
     * @param enabled 
     * @param account 
     */
    Config(boolean enabled, AccountConfig account) {
        this.enabled = enabled;
        this.account = account;
    }

    /**
     * Get enabled
     * @return enabled
     */
    boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Get account
     * @return account
     */
    AccountConfig getAccount() {
        return account;
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
        return ObjectUtils.equals(this.enabled, config.enabled)
                && ObjectUtils.equals(this.account, config.account);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(enabled, account);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Config {\n");
        
        sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
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

