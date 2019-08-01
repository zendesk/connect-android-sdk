package com.zendesk.connect;

import androidx.annotation.Nullable;

import com.zendesk.util.DigestUtils;

import javax.inject.Inject;

@ConnectScope
class StorageController {

    private static final String LOG_TAG = "StorageController";

    private static final String PREFERENCES_KEY_CONFIG = "connect_preferences_key_config";
    private static final String PREFERENCES_KEY_USER = "connect_preferences_key_user";
    private static final String PREFERENCES_KEY_PRIVATE_KEY = "connect_preferences_key_private_key";

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
     * @return the stored {@link Config}, or null if it doesn't exist
     */
    @Nullable
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
     * @return the stored {@link User}, or null if it doesn't exist
     */
    @Nullable
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

    /**
     * <p>
     *     Encrypts and stores the given private key into the provided {@link BaseStorage}
     * </p>
     *
     * @param privateKey the private key to be stored
     */
    void savePrivateKey(String privateKey) {
        String hashedKey = DigestUtils.sha256(privateKey);
        storage.put(PREFERENCES_KEY_PRIVATE_KEY, hashedKey);
    }

    /**
     * <p>
     *     Checks if the given private key is a new key
     * </p>
     *
     * @param privateKey the private key to compare
     * @return true if the keys match or the stored key is null, false otherwise
     */
    boolean isNewPrivateKey(String privateKey) {
        String storedKey = storage.get(PREFERENCES_KEY_PRIVATE_KEY);
        String hashedKey = DigestUtils.sha256(privateKey);
        return storedKey == null || !storedKey.equals(hashedKey);
    }

    /**
     * <p>
     *     Clears the stored private key from the provided {@link BaseStorage}
     * </p>
     */
    void clearPrivateKey() {
        storage.remove(PREFERENCES_KEY_PRIVATE_KEY);
    }

    /**
     * <p>
     *     Clears everything from storage
     * </p>
     */
    void clearAllStorage() {
        storage.clear();
    }
}
