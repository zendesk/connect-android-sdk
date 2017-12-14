# Migrating from Outbound Android SDK 0.3.X to 0.4.X

## Removed Notification Display Intent
Outbound Notifications will now be displayed immediately once received without using an intent to trigger its display.

### OutboundMessagingService changes

`OutboundMessagingService` now provides 3 methods which can be overriden with your own extended classes:
- `public void onReceivedMessage(RemoteMessage message)` - Called when an unhandled non-Outbound notification is received
- `public void onNotificationReceived(PushNotification notification)` - Called after an Outbound notification is received
- `public void onNotificationDisplayed(PushNotification notification)` - Called after an Outbound notification is displayed
- `public Notification buildNotification(PushNotification notification)` - Called to create an Android `Notification` from a `PushNotification`. Can be used to create custom notification styling.

For example:
```java
public class DebugMessagingService extends OutboundMessagingService {
    @Override
    public void onReceivedMessage(RemoteMessage message) {
        Log.d("DebugMessagingService", "non-outbound message received " + message.getMessageId());
    }

    @Override
    public void onNotificationDisplayed(PushNotification notification) {
        Log.d("DebugMessagingService", "outbound notification displayed " + notification.getInstanceId());
    }

    @Override
    public void onNotificationReceived(PushNotification notification) {
        Log.d("DebugMessagingService", "outbound notification received " + notification.getInstanceId());
    }

    @Override
    public Notification buildNotification(PushNotification notification) {
        NotificationCompat.Builder notificationBuilder = notification.createNotificationBuilder(this);
        try {
            // Use the custom JSON payload from the Campaign to specify your own custom fields
            if (notification.getPayload().getBoolean("showBigText")) {
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notification.getTitle()));
            }
        } catch (Exception e) {
            // Handle any exceptions here
        }

        return notificationBuilder.build();
    }
}
```

### OutboundService changes
`OutboundService` no longer provides an override for `onDisplayNotification` or `onReceiveNotification`.
This has been moved to `OutboundMessagingService` and been renamed to `onNotificationDisplayed`.

### AndroidManifest.xml changes

The `OutboundService` service no longer needs an intent filter for displaying notifications.

You need to remove the following actions from your `AndroidManifest.xml`:
- `<action android:name="YOUR_PACKAGE_NAME.outbound.action.TRACK_NOTIF" />`
- `<action android:name="YOUR_PACKAGE_NAME.outbound.action.DISPLAY_NOTIF" />`
- `<action android:name="YOUR_PACKAGE_NAME.outbound.action.RECEIVED_NOTIF" />`

And also add permissions for the firebase job dispatcher:
```xml
<service
    android:exported="false"
    android:name="io.outbound.sdk.OutboundJobService">
    <intent-filter>
        <action android:name="com.firebase.jobdispatcher.ACTION_EXECUTE"/>
    </intent-filter>
</service>
```

#### Before:

```xml
<service
  android:name="io.outbound.sdk.OutboundService"
  android:exported="false"
  android:label="OutboundService" >
  <intent-filter>
    <action android:name="YOUR_PACKAGE_NAME.outbound.action.TRACK_NOTIF" />
    <action android:name="YOUR_PACKAGE_NAME.outbound.action.DISPLAY_NOTIF" />
    <action android:name="YOUR_PACKAGE_NAME.outbound.action.RECEIVED_NOTIF" />
    <action android:name="YOUR_PACKAGE_NAME.outbound.action.OPEN_NOTIF" />
  </intent-filter>
</service>
```

#### After:
```xml
<service
    android:exported="false"
    android:name="io.outbound.sdk.OutboundJobService">
    <intent-filter>
        <action android:name="com.firebase.jobdispatcher.ACTION_EXECUTE"/>
    </intent-filter>
</service>

<service
  android:name="io.outbound.sdk.OutboundService"
  android:exported="false"
  android:label="OutboundService" >
  <intent-filter>
    <action android:name="YOUR_PACKAGE_NAME.outbound.action.OPEN_NOTIF" />
  </intent-filter>
</service>
```