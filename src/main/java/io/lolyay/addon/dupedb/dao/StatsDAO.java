package io.lolyay.addon.dupedb.dao;

import java.util.List;

public record StatsDAO(
    Integer total,
    List<Bytypeitem> bytype,
    List<Bystatusitem> bystatus,
    Integer recentcount,
    Integer verifiedcount,
    Integer unverifiedcount,
    Integer usercount
) {
    public record Bystatusitem(
        String status,
        String count
    ) {
    }

    public record Bytypeitem(
        String type,
        String count
    ) {
    }
}


