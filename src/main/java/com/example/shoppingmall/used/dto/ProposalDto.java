package com.example.shoppingmall.used.dto;

import com.example.shoppingmall.used.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProposalDto {
    private Long id;
    private ItemStatus status;
    private Long userId;
    private Long itemId;
}
