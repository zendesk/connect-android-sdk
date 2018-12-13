package com.zendesk.connect;

import java.util.*;
import com.zendesk.util.ObjectUtils;

/**
 * AccountConfig
 */
class AccountConfig {

    private boolean prompt;
    private String promptEvent = null;
    private PrePrompt prePrompt = null;

    /**
     * @param prompt 
     * @param promptEvent 
     * @param prePrompt 
     */
    AccountConfig(boolean prompt, String promptEvent, PrePrompt prePrompt) {
        this.prompt = prompt;
        this.promptEvent = promptEvent;
        this.prePrompt = prePrompt;
    }

    /**
     * Get prompt
     * @return prompt
     */
    boolean isPrompt() {
      return prompt;
    }
    
    /**
     * Get promptEvent
     * @return promptEvent
     */
    String getPromptEvent() {
      return promptEvent;
    }
    
    /**
     * Get prePrompt
     * @return prePrompt
     */
    PrePrompt getPrePrompt() {
      return prePrompt;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountConfig accountConfig = (AccountConfig) o;
        return ObjectUtils.equals(this.prompt, accountConfig.prompt) &&
                ObjectUtils.equals(this.promptEvent, accountConfig.promptEvent) &&
                ObjectUtils.equals(this.prePrompt, accountConfig.prePrompt);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(prompt, promptEvent, prePrompt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountConfig {\n");
        
        sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
        sb.append("    promptEvent: ").append(toIndentedString(promptEvent)).append("\n");
        sb.append("    prePrompt: ").append(toIndentedString(prePrompt)).append("\n");
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

