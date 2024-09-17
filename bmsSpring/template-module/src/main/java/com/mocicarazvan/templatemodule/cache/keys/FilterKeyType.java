package com.mocicarazvan.templatemodule.cache.keys;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class FilterKeyType {
    protected List<Long> ids;
    protected String key;
    protected KeyRouteType routeType;

    public FilterKeyType() {
        this.ids = new ArrayList<>();
        this.key = "";
    }

    public FilterKeyType(String key) {
        this.ids = new ArrayList<>();
        this.key = key;
    }

    public FilterKeyType(List<Long> ids, String key) {
        this.ids = ids;
        this.key = key;
    }

    public FilterKeyType(String key, KeyRouteType routeType) {
        this.key = key;
        this.ids = new ArrayList<>();
        this.routeType = routeType;
    }

    public FilterKeyType(List<Long> ids, String key, KeyRouteType routeType) {
        this.ids = ids;
        this.key = key;
        this.routeType = routeType;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterKeyType that = (FilterKeyType) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public String toString() {
        return "FilterKeyType(ids=" + this.getIds() + ", key=" + this.getKey() + ")";
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class KeyRouteType {
        private Long trainerId = -1L;
        private Boolean isAdmin = false;
        private Boolean isPublic = false;

        public KeyRouteType() {
            this.trainerId = -1L;
            this.isAdmin = false;
            this.isPublic = false;
        }

        public static KeyRouteType createForTrainer(Long trainerId) {
            KeyRouteType keyRouteType = new KeyRouteType();
            keyRouteType.setTrainerId(trainerId);
            return keyRouteType;
        }

        public static KeyRouteType createForAdmin() {

            KeyRouteType keyRouteType = new KeyRouteType();
            keyRouteType.setIsAdmin(true);
            return keyRouteType;

        }

        public static KeyRouteType createForPublic() {
            KeyRouteType keyRouteType = new KeyRouteType();
            keyRouteType.setIsPublic(true);
            return keyRouteType;
        }

        public static KeyRouteType createIndependent() {
            return new KeyRouteType(-11L, false, false);
        }
    }
}
