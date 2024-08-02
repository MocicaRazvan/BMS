package com.mocicarazvan.recipeservice.enums;

import java.util.List;

public enum DietType {

    VEGAN {
        @Override
        public boolean canEat(DietType type) {
            return type == VEGAN;
        }
    },
    VEGETARIAN {
        @Override
        public boolean canEat(DietType type) {
            return type == VEGAN || type == VEGETARIAN;
        }
    },
    //    CARNIVORE {
//        @Override
//        public boolean canEat(DietType type) {
//            return type == CARNIVORE;
//        }
//    },
    OMNIVORE {
        @Override
        public boolean canEat(DietType type) {
            return true;
        }
    };

    public abstract boolean canEat(DietType type);

    public static boolean isDietTypeValid(DietType type, List<DietType> dietTypes) {
//        for (DietType dietType : dietTypes) {
//            if (!type.canEat(dietType)) {
//                return false;
//            }
//        }
//        return true;
//    }
        DietType leastRestrictiveType = determineDietType(dietTypes);
        return type.canEat(leastRestrictiveType);

    }

    public static DietType determineDietType(List<DietType> dietTypes) {
        if (dietTypes.contains(OMNIVORE)) {
            return OMNIVORE;
        } else if (dietTypes.contains(VEGETARIAN)) {
            return VEGETARIAN;
        } else {
            return VEGAN;
        }
    }
}


