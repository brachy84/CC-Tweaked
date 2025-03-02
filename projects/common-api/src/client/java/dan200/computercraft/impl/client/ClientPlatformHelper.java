// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.impl.client;

import dan200.computercraft.impl.Services;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public interface ClientPlatformHelper {
    /**
     * Get a model from a resource.
     *
     * @param manager          The model manager.
     * @param resourceLocation The model resourceLocation.
     * @return The baked model.
     */
    BakedModel getModel(ModelManager manager, ResourceLocation resourceLocation);

    static ClientPlatformHelper get() {
        var instance = Instance.INSTANCE;
        return instance == null ? Services.raise(ClientPlatformHelper.class, Instance.ERROR) : instance;
    }

    final class Instance {
        static final @Nullable ClientPlatformHelper INSTANCE;
        static final @Nullable Throwable ERROR;

        static {
            var helper = Services.tryLoad(ClientPlatformHelper.class);
            INSTANCE = helper.instance();
            ERROR = helper.error();
        }

        private Instance() {
        }
    }
}
