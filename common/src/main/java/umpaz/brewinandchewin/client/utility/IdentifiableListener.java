package umpaz.brewinandchewin.client.utility;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface IdentifiableListener extends PreparableReloadListener {
    Identifier getId();
}
