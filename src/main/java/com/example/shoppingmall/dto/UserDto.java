package com.example.shoppingmall.dto;

import com.example.shoppingmall.entity.CustomUserDetails;
import com.example.shoppingmall.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String name;    // 사용자 이름
    private String nickname;    // 닉네임
    private Integer age;    // 연령대
    private String email;   // 이메일
    private String phone;   // 전화번호
    private String businessNumber;

    private String authorities;     // 권한

    public static UserEntity fromEntity(UserEntity entity) {
        return UserEntity.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .name(entity.getName())
                .nickname(entity.getNickname())
                .age(entity.getAge())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .authorities(entity.getAuthorities())
                .build();
    }
}