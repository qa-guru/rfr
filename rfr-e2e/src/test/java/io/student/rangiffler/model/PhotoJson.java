package io.student.rangiffler.model;



public record PhotoJson(
        String src,
        String countryCode,
        String description,
        Integer likes
) {}
