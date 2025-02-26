package umpaz.brewinandchewin.common.utility.dfu.schema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

public class BnCSchemaV1 extends NamespacedSchema {
    public BnCSchemaV1(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(final Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        schema.registerSimple(map, "brewinandchewin:keg");
        schema.registerSimple(map, "brewinandchewin:coaster");
        return map;
    }

}
