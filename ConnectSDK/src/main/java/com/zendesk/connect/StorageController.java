package com.zendesk.connect;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
class StorageController {

    private static final String LOG_TAG = "StorageController";

    private static final String PREFERENCES_KEY_CONFIG = "connect_preferences_key_config";
    private static final String PREFERENCES_KEY_USER = "connect_preferences_key_user";

    private BaseStorage storage;

    @Inject
    StorageController(BaseStorage storage) {
        this.storage = storage;
    }

    /**
     * Returns an instance of {@link BaseStorage} held by this controller. Intended for
     * internal use of the ConnectSDK.
     *
     * @return an instance of {@link BaseStorage}
     */
    BaseStorage storage() {
        return storage;
    }

    /**
     * <p>
     *     Stores the given {@link Config} into the provided {@link BaseStorage}
     * </p>
     *
     * @param config the {@link Config} to be stored
     */
    void saveConfig(Config config) {
        storage.put(PREFERENCES_KEY_CONFIG, config);
    }

    /**
     * <p>
     *     Retrieves the {@link Config} stored in the provided {@link BaseStorage}
     * </p>
     *
     * @return the stored {@link Config}
     */
    Config getConfig() {
        return storage.get(PREFERENCES_KEY_CONFIG, Config.class);
    }

    /**
     * <p>
     *     Clears the stored {@link Config} from the provided {@link BaseStorage}
     * </p>
     */
    void clearConfig() {
        storage.remove(PREFERENCES_KEY_CONFIG);
    }

    /**
     * <p>
     *     Stores the given {@link User} into the provided {@link BaseStorage}
     * </p>
     *
     * @param user the {@link User} to be stored
     */
    void saveUser(User user) {
        storage.put(PREFERENCES_KEY_USER, user);
    }

    /**
     * <p>
     *     Retrieves the {@link User} stored in the provided {@link BaseStorage}
     * </p>
     *
     * @return the stored {@link User}
     */
    User getUser() {
        return storage.get(PREFERENCES_KEY_USER, User.class);
    }

    /**
     * <p>
     *     Clears the stored {@link User} from the provided {@link BaseStorage}
     * </p>
     */
    void clearUser() {
        storage.remove(PREFERENCES_KEY_USER);
    }
}
