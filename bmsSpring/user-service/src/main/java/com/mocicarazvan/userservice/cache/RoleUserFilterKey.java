package com.mocicarazvan.userservice.cache;

import com.mocicarazvan.templatemodule.cache.keys.FilerKeyTypeWithExtra;
import com.mocicarazvan.templatemodule.cache.keys.FilterKeyType;
import com.mocicarazvan.templatemodule.enums.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleUserFilterKey extends FilerKeyTypeWithExtra<Set<String>, Set<Role>> {

    public RoleUserFilterKey() {
        super(new HashSet<>());
    }

    public RoleUserFilterKey(Set<Role> defaultMap) {
        super(defaultMap);
    }

    public RoleUserFilterKey(String key, Set<Role> defaultMap) {
        super(key, defaultMap);
    }

    public RoleUserFilterKey(List<Long> ids, String key, Set<Role> defaultMap) {
        super(ids, key, defaultMap);
    }

    public RoleUserFilterKey(String key, FilterKeyType.KeyRouteType routeType, Set<Role> defaultMap) {
        super(key, routeType, defaultMap);
    }

    public RoleUserFilterKey(List<Long> ids, String key, FilterKeyType.KeyRouteType routeType, Set<Role> defaultMap) {
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


    @Override
    public String toString() {
        return "RoleUserFilterKey(ids=" + this.getIds() + ", key=" + this.getKey() + ", extra=" + this.getExtra() + ")";
    }

    @Override
    public Set<String> mapToExtra(Set<Role> map) {
        return map == null ? new HashSet<>() : map.stream().map(Enum::name).collect(Collectors.toSet());
    }

    public String mapToExtraSingle(Role role) {
        return role == null ? "" : role.name();
    }
}
