package com.zendesk.connect;

import java.util.List;

/**
 * <p>
 *     Since Kotlin doesn't allow us to pass null values into methods annotated as NonNull,
 *     we have this workaround to trick Kotlin. These Java methods will return null but Kotlin
 *     will believe that the return is NonNull. This is just so we can test the null handling
 *     logic of the SDK.
 * </p>
 */
public class NullableTypesAsNonNull<T> {
    T getNullObject() {
        return null;
    }

    List<T> getNullList() {
        return null;
    }
}
