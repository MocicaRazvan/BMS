package com.mocicarazvan.templatemodule.cache.keys;

import java.util.List;

public class ApproveFilterKey extends FilerKeyTypeWithExtra<String, Boolean> {

    public ApproveFilterKey() {
        super(null);
    }

    public ApproveFilterKey(Boolean defaultMap) {
        super(defaultMap);
    }

    public ApproveFilterKey(String key, Boolean defaultMap) {
        super(key, defaultMap);
    }

    public ApproveFilterKey(List<Long> ids, String key, Boolean defaultMap) {
        super(ids, key, defaultMap);
    }

    public ApproveFilterKey(String key, FilterKeyType.KeyRouteType routeType, Boolean defaultMap) {
        super(key, routeType, defaultMap);
    }

    public ApproveFilterKey(List<Long> ids, String key, FilterKeyType.KeyRouteType routeType, Boolean defaultMap) {
        super(ids, key, routeType, defaultMap);
    }


    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return "ApproveFilterKey(ids=" + this.getIds() + ", key=" + this.getKey() + ", extra=" + this.getExtra() + ")";
    }

    @Override
    public String mapToExtra(Boolean map) {
        if (map == null) {
            return "null";
        } else if (map) {
            return "true";
        } else {
            return "false";
        }

    }
}
//@Setter
//@Getter
//public class ApproveFilterKey extends FilterKeyType {
//    private String approved;
//
//    public ApproveFilterKey() {
//        super();
//        this.approved = null;
//    }
//
//    public ApproveFilterKey(String key) {
//        super(key);
//        this.approved = null;
//    }
//
//    public ApproveFilterKey(List<Long> ids, String key, Boolean approved) {
//        super(ids, key);
//        this.approved = mapApproved(approved);
//    }
//
//    @Override
//    public boolean equals(final Object o) {
//        return super.equals(o);
//    }
//
//    @Override
//    public int hashCode() {
//        return super.hashCode();
//    }
//
//    public String toString() {
//        return "ApproveFilterKey(ids=" + this.getIds() + ", key=" + this.getKey() + ", approved=" + this.getApproved() + ")";
//    }
//
//    public String setApproved(Boolean approved) {
//        this.approved = mapApproved(approved);
//        return this.approved;
//    }
//
//    public static String mapApproved(Boolean approved) {
//        if (approved == null) {
//            return "null";
//        } else if (approved) {
//            return "true";
//        } else {
//            return "false";
//        }
//    }
//}
