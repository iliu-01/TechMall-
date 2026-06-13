package com.techmall.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int pages;

    public static <T> PageDTO<T> of(List<T> records, long total, int page, int size) {
        int pages = (int) Math.ceil((double) total / size);
        return new PageDTO<>(records, total, page, size, pages);
    }
}
