package com.vectorwing.farmersdelight.common.utility;

public class ItemBlockString {

    public String modid;
    public String name;
    public int metadata;

    protected ItemBlockString(String modid, String name, int metadata) {
        this.modid = modid;
        this.name = name;
        this.metadata = metadata;
    }
}
