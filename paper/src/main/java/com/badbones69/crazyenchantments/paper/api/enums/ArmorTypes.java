package com.badbones69.crazyenchantments.paper.api.enums;

public enum ArmorTypes {
    IRON("iron"),
    GOLD("gold");

    private String type;

    ArmorTypes(String type) {
        this.type = type;
    }

    ArmorTypes(String type, boolean scalar) {

    }
}
