package com.zendesk.connect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * {@link Qualifier} to be used when providing or requiring as a dependency the connect
 * silent notification channel id defined by the integrator.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@interface ConnectSilentNotificationChannelQualifier {
}
