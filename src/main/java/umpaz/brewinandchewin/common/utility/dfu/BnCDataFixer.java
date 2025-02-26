package umpaz.brewinandchewin.common.utility.dfu;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.ItemRenameFix;
import net.minecraft.util.datafix.fixes.NamespacedTypeRenameFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import umpaz.brewinandchewin.common.mixin.DataFixTypesAccessor;
import umpaz.brewinandchewin.common.utility.dfu.schema.BnCSchemaV1;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public record BnCDataFixer(DataFixer fixer) {
    public static final int CURRENT_VERSION = 2;
    private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    private static BnCDataFixer instance;

    public static BnCDataFixer get() {
        if (instance == null)
            instance = new BnCDataFixer(registerFixers());
        return instance;
    }

    private static DataFixer registerFixers() {
        DataFixerBuilder builder = new DataFixerBuilder(CURRENT_VERSION);
        builder.addSchema(0, (integer, schema) -> new Schema(integer, DataFixers.getDataFixer()
                .getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion()))));
        // Rename Scarlet Pierogies to Scarlet Pierogi
        Schema schema1 = builder.addSchema(1, BnCSchemaV1::new);
        Schema schema2 = builder.addSchema(2, SAME_NAMESPACED);
        builder.addFixer(ItemRenameFix.create(schema2, "Fix Scarlet Pierogi item name", createRenamer("brewinandchewin:scarlet_pierogies", "brewinandchewin:scarlet_pierogi")));
        builder.addFixer(new NamespacedTypeRenameFix(schema2, "Rename Scarlet Pierogi recipe", References.RECIPE, createRenamer("brewinandchewin:cooking/scarlet_pierogies", "brewinandchewin:cooking/scarlet_pierogi")));
        Executor executor = Executors.newSingleThreadExecutor((new ThreadFactoryBuilder()).setNameFormat("Brewin' and Chewin' Datafixer Bootstrap").setDaemon(true).setPriority(1).build());
        return builder.buildOptimized(SharedConstants.DATA_FIX_TYPES_TO_OPTIMIZE, executor);
    }

    private static UnaryOperator<String> createRenamer(String original, String newName) {
        return name -> name.equals(original) ? newName : name;
    }


    public <T> Dynamic<T> updateWithFixers(DataFixTypes types, Dynamic<T> dynamic) {
        return fixer.update(((DataFixTypesAccessor)(Object)types).brewinandchewin$getType(), dynamic, getModDataVersion(dynamic), CURRENT_VERSION);
    }

    public static <T> int getModDataVersion(Dynamic<T> dynamic) {
        return dynamic.get("brewinandchewin:data_version").asInt(0);
    }

    public static int getModDataVersion(CompoundTag tag) {
        return tag.getInt("brewinandchewin:data_version");
    }

    public static CompoundTag setModDataVersion(CompoundTag tag) {
        tag.putInt("brewinandchewin:data_version", CURRENT_VERSION);
        return tag;
    }
}
