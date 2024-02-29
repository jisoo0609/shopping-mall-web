package com.example.shoppingmall.shop.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.auth.entity.UserEntity;
import com.example.shoppingmall.auth.repo.UserRepository;
import com.example.shoppingmall.shop.dto.ShopDto;
import com.example.shoppingmall.shop.entity.Shop;
import com.example.shoppingmall.shop.entity.ShopCategory;
import com.example.shoppingmall.shop.entity.ShopStatus;
import com.example.shoppingmall.shop.repo.ShopCategoryRepository;
import com.example.shoppingmall.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopRepository shopRepository;
    private final ShopCategoryRepository shopCategoryRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authFacade;

    // CREATE
    // 쇼핑몰 개설
    public ShopDto crateShop(ShopDto dto) {
        // 유저 불러 오기
        UserEntity user = getUserEntity();

        // 쇼핑몰 생성
        Shop newShop = Shop.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .user(user)
                .status(ShopStatus.PREPARING)
                .build();

        // 쇼핑몰 카테고리 추가
        Set<ShopCategory> shopCategories = getShopCategories(new HashSet<>(dto.getShopCategories()));

        // 새로운 shop 생성
        newShop.setShopCategories(shopCategories);
        return ShopDto.fromEntity(shopRepository.save(newShop));
    }

    // 쇼핑몰 정보 불러오기
    public ShopDto readOne(Long id) {
        Optional<Shop> optionalShop = shopRepository.findById(id);
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Shop shop = optionalShop.get();

        log.info("shop Owner: {}", shop.getUser().getUsername());
        log.info("auth user: {}", authFacade.getAuthName());
        if (!shop.getUser().getUsername().equals(authFacade.getAuthName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ShopDto.fromEntity(shop);
    }

    // 쇼핑몰 정보 수정
    public ShopDto updateShop(Long id, ShopDto dto) {
        // 수정할 Shop 가져옴
        Shop target = getShop(id);

        // shop owner
        String user = target.getUser().getUsername();

        // 접근한 user와 shop owner가 같을때 정보 수정 가능
        if (!user.equals(authFacade.getAuthName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // 카테고리 정보 수정
        Set<ShopCategory> shopCategories
                = getShopCategories(new HashSet<>(dto.getShopCategories()));
        target.setName(dto.getName());
        target.setDescription(dto.getDescription());
        target.setShopCategories(shopCategories);

        return ShopDto.fromEntity(shopRepository.save(target));
    }

    // 쇼핑몰 폐쇄 요청

    // 관리자가 허가 또는 거절

    private UserEntity getUserEntity() {
        String authName = authFacade.getAuthName();
        UserEntity user = userRepository.findByUsername(authName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 유저 확인용 log
        log.info("name: {}", authName);
        log.info("Optional user Auth: {}", user.getAuthorities());

        return user;
    }

    private Shop getShop(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // shop 정보 확인용 log
        log.info("shop owner: {}", shop.getUser().getUsername());
        return shop;
    }

    // 쇼핑몰 카테고리 추출 메서드
    private Set<ShopCategory> getShopCategories(Set<String> categoryNames) {
        Set<ShopCategory> shopCategories = new HashSet<>();
        for (String categoryName : categoryNames) {
            ShopCategory shopCategory = shopCategoryRepository.findByName(categoryName);
            if (shopCategory == null) {
                // 쇼핑몰 카테고리가 존재하지 않는 경우 새로 생성
                shopCategory = new ShopCategory(categoryName);
                shopCategoryRepository.save(shopCategory);
            }
            shopCategories.add(shopCategory);
        }
        return shopCategories;
    }
}