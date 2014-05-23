/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.groups.storage;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.Manager;
import com.afterkraft.groups.util.ExternalProviderRegistration;

public class StorageManager implements Manager {

    private final Groups plugin;
    private StorageFrontend storage;

    public StorageManager(Groups plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String configuredBackend = "yml";
        StorageBackend backend = ExternalProviderRegistration.getStorageBackendMap().get(configuredBackend);

        if (backend == null) {
            plugin.getLogger().severe("ERROR - You specified the '" + configuredBackend + "' storage type, but that storage type is not available.");
            StringBuilder sb = new StringBuilder("Available storage types are:");
            for (String str : ExternalProviderRegistration.getStorageBackendMap().keySet()) {
                sb.append(" '").append(str).append("'");
            }
            plugin.getLogger().severe(sb.toString());
            plugin.cancelEnable();
            return;
        }

        try {
            backend.initialize();
        } catch (Throwable e) {
            e.printStackTrace();
            plugin.getLogger().severe("The storage backend '" + configuredBackend + "' threw an exception during startup:");
            plugin.getLogger().severe(e.getMessage());
            plugin.cancelEnable();
            return;
        }

        storage = ExternalProviderRegistration.getStorageFrontendOverride().construct(plugin, backend);
        plugin.getLogger().info("Storage initialized with provider " + storage.getName());
    }

    public StorageFrontend getStorage() {
        return storage;
    }

    public void shutdown() {
        storage.shutdown();
    }
}
