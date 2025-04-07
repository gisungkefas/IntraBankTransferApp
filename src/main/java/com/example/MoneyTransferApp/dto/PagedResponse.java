package com.example.MoneyTransferApp.dto;

import lombok.Data;
import java.util.List;

@Data
public class PagedResponse<T> {

    private List<T> content;
    private int totalPages;
    private long totalElements;
    private boolean last;
    private int size;
    private int number;
    private boolean first;
    private int numberOfElements;
    private boolean empty;
}
