package com.example.shoppingmall.auth;

import com.example.shoppingmall.jwt.dto.JwtRequestDto;
import com.example.shoppingmall.jwt.dto.JwtResponseDto;
import com.example.shoppingmall.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("token")
@RequiredArgsConstructor
public class TokenController {
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsManager manager;
    private final PasswordEncoder passwordEncoder;


    // POST /token/issue
    @PostMapping("/issue")
    public JwtResponseDto issueJwt(@RequestBody JwtRequestDto dto) {
        // 사용자가 제공한 username(id), password가 저장된 사용자인지 판단
        if (!manager.userExists(dto.getUsername()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        UserDetails userDetails
                = manager.loadUserByUsername(dto.getUsername());

        // 비밀번호 확인
        if (!passwordEncoder.matches(dto.getPassword(), userDetails.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // JWT 발급
        String jwt = jwtTokenUtils.generateToken(userDetails);
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);
        return response;
    }

    // 발급된 토큰이 유효한지 확인
    @GetMapping("/validate")
    public Claims validateToken(@RequestParam("token") String token) {
        if (!jwtTokenUtils.validate(token))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return jwtTokenUtils.parseClaims(token);
    }
}
