package com.comprehensive.eureka.plan.entity.enums;

public enum DataPeriod {
    DAY("일"),
    MONTH("월");

    private final String label;

    DataPeriod(String label) {
        this.label = label;
    }

    public DataPeriod[] getAllDataPeriod() {
        return DataPeriod.values();
    }
}
