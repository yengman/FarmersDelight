package com.vectorwing.farmersdelight.common.utility;

import org.apache.commons.lang3.math.NumberUtils;

import com.vectorwing.farmersdelight.FarmersDelight;

public class MiscUtils {

    public static ItemBlockString parseItemBlockString(String fullName, boolean checkMeta, int defMeta) {
        if (fullName == null) {
            return null;
        }
        String[] splitName = fullName.split(":");
        if (splitName.length <= 1) {
            FarmersDelight.LOG.warn("Invalid name found: {}", fullName);
            return null;
        }
        String modid = splitName[0];
        String name = splitName[1];
        int metadata = defMeta;
        if (checkMeta && splitName.length > 2) {
            if (NumberUtils.isNumber(splitName[2])) {
                metadata = Integer.decode(splitName[2]);
            } else {
                FarmersDelight.LOG.warn("Invalid metadata found: {}", fullName);
                return null;
            }
        }
        return new ItemBlockString(modid, name, metadata);
    }

}
