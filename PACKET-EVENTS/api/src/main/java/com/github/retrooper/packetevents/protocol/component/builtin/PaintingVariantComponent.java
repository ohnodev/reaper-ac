/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2025 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.world.painting.PaintingVariant;
import com.github.retrooper.packetevents.protocol.world.painting.PaintingVariants;
import com.github.retrooper.packetevents.protocol.world.painting.StaticPaintingVariant;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.Objects;

public class PaintingVariantComponent {

    private PaintingVariant variant;

    public PaintingVariantComponent(PaintingVariant variant) {
        this.variant = variant;
    }

    public static PaintingVariantComponent read(PacketWrapper<?> wrapper) {
        PaintingVariant variant = wrapper.readMappedEntityOrDirect(PaintingVariants.getRegistry(), w -> {
            // This is the direct/inline reader (when id == 0)
            int width = w.readVarInt();
            int height = w.readVarInt();
            ResourceLocation assetId = w.readIdentifier();

            // Title (trusted optional component -> boolean followed by component)
            if (w.readBoolean()) {
                w.readComponent();
            }

            // Author (trusted optional component)
            if (w.readBoolean()) {
                w.readComponent();
            }

            // Return a direct instance (data is null because it has no registry key)
            return new StaticPaintingVariant(null, width, height, assetId);
        });

        return new PaintingVariantComponent(variant);
    }

    public static void write(PacketWrapper<?> wrapper, PaintingVariantComponent component) {
        wrapper.writeMappedEntityOrDirect(component.variant, (w, v) -> {
            // This is the direct/inline writer (when the variant isn't in the registry)
            // Note: Update these getters based on what's available in your PaintingVariant interface.
            if (v instanceof StaticPaintingVariant) {
                StaticPaintingVariant staticVar = (StaticPaintingVariant) v;
                w.writeVarInt(staticVar.getWidth());
                w.writeVarInt(staticVar.getHeight());
                w.writeIdentifier(staticVar.getAssetId());
            } else {
                // Safe fallback just in case
                w.writeVarInt(1);
                w.writeVarInt(1);
                w.writeIdentifier(new ResourceLocation("minecraft", "custom"));
            }

            // Write false for optional Title and Author as they aren't currently stored in PE
            w.writeBoolean(false);
            w.writeBoolean(false);
        });
    }

    public PaintingVariant getVariant() {
        return this.variant;
    }

    public void setVariant(PaintingVariant variant) {
        this.variant = variant;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PaintingVariantComponent)) return false;
        PaintingVariantComponent that = (PaintingVariantComponent) obj;
        return this.variant.equals(that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.variant);
    }
}
