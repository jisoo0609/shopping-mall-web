package com.example.shoppingmall.used.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.auth.entity.UserEntity;
import com.example.shoppingmall.auth.repo.UserRepository;
import com.example.shoppingmall.used.dto.ProposalDto;
import com.example.shoppingmall.used.entity.Item;
import com.example.shoppingmall.used.entity.Proposal;
import com.example.shoppingmall.used.repo.ItemRepository;
import com.example.shoppingmall.used.repo.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProposalService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ProposalRepository proposalRepository;
    private final AuthenticationFacade authFacade;

    // 구매 제안 조회 - Item을 등록한 유저
    public List<ProposalDto> readAll(Long id) {
        // 페이지에 접근한 유저 정보 불러옴
        UserEntity user = getUserEntity();

        // 구매 제안 정보 조회할 아이템 가져옴
        Item item = getItem(id);

        // item을 등록한 사용자와 페이지에 접근한 사용자가 일치하는 경우
        log.info("register User: {}", item.getUser().getUsername());
        log.info("auth User: {}", authFacade.getAuthName());
        List<Proposal> proposalList;
        if (!item.getUser().getUsername().equals(authFacade.getAuthName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        proposalList = proposalRepository.findByItemId(item.getId());
        return proposalList.stream()
                .map(proposal -> ProposalDto.builder()
                        .id(proposal.getId())
                        .status(proposal.getStatus())
                        .userId(proposal.getUser().getId())
                        .itemId(proposal.getItem().getId())
                        .build())
                .collect(Collectors.toList());
    }

    // 구매 제안 조회 - 제안한 유저
    public List<ProposalDto> readAll() {
        UserEntity user = getUserEntity();
        List<Proposal> proposalList = proposalRepository.findByUserId(user.getId());
        return proposalList.stream()
                .map(proposal -> ProposalDto.builder()
                        .id(proposal.getId())
                        .status(proposal.getStatus())
                        .userId(proposal.getUser().getId())
                        .itemId(proposal.getItem().getId())
                        .build())
                .collect(Collectors.toList());
    }

    // 구매 제안 수락 또는 거절

    // 구매 확정


    private UserEntity getUserEntity() {
        String authName = authFacade.getAuthName();
        return userRepository.findByUsername(authName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}