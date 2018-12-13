package com.zendesk.connect;

import java.util.*;
import com.zendesk.util.ObjectUtils;

/**
 * PrePrompt
 */
class PrePrompt {

    private String title = null;
    private String body = null;
    private String noButton = null;
    private String yesButton = null;

    /**
     * @param title 
     * @param body 
     * @param noButton 
     * @param yesButton 
     */
    PrePrompt(String title, String body, String noButton, String yesButton) {
        this.title = title;
        this.body = body;
        this.noButton = noButton;
        this.yesButton = yesButton;
    }

    /**
     * Get title
     * @return title
     */
    String getTitle() {
      return title;
    }
    
    /**
     * Get body
     * @return body
     */
    String getBody() {
      return body;
    }
    
    /**
     * Get noButton
     * @return noButton
     */
    String getNoButton() {
      return noButton;
    }
    
    /**
     * Get yesButton
     * @return yesButton
     */
    String getYesButton() {
      return yesButton;
    }
    
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrePrompt prePrompt = (PrePrompt) o;
        return ObjectUtils.equals(this.title, prePrompt.title) &&
                ObjectUtils.equals(this.body, prePrompt.body) &&
                ObjectUtils.equals(this.noButton, prePrompt.noButton) &&
                ObjectUtils.equals(this.yesButton, prePrompt.yesButton);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hash(title, body, noButton, yesButton);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PrePrompt {\n");
        
        sb.append("    title: ").append(toIndentedString(title)).append("\n");
        sb.append("    body: ").append(toIndentedString(body)).append("\n");
        sb.append("    noButton: ").append(toIndentedString(noButton)).append("\n");
        sb.append("    yesButton: ").append(toIndentedString(yesButton)).append("\n");
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

