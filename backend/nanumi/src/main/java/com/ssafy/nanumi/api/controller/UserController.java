package com.ssafy.nanumi.api.controller;

import com.ssafy.nanumi.api.request.UserJoinDTO;
import com.ssafy.nanumi.api.response.ProductAllDTO;
import com.ssafy.nanumi.api.response.ReviewReadDTO;
import com.ssafy.nanumi.api.response.UserReadDTO;
import com.ssafy.nanumi.api.service.UserService;
import com.ssafy.nanumi.config.response.CustomResponse;
import com.ssafy.nanumi.config.response.ResponseService;
import com.ssafy.nanumi.db.entity.User;
import com.ssafy.nanumi.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private  final UserRepository userRepository;
    private final ResponseService responseService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /* 로컬 회원가입 */
    @PostMapping("/users/join")
    @CrossOrigin(origins = "*")
    public CustomResponse join(@RequestBody User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_USER");
//        userService.join(userJoinDTO);
        userRepository.save(user);
        return responseService.getSuccessResponse();
    }

    /* 로그아웃 */
    @GetMapping("/users/logout")
    public CustomResponse logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return responseService.getSuccessResponse();
    }

    /* 회원 정보 조회 */
    @GetMapping("/users")
    public ResponseEntity<UserReadDTO> getUser(){
        User user = userRepository.findById(1L).get();
        return new ResponseEntity<>(userService.getUser(user), HttpStatus.OK);
    }

    /* 회원 정보 수정 */

    /* 회원 정보 탈퇴 */
    @DeleteMapping("/users")
    public void deleteUser(){
        User user = userRepository.findById(1L).get();
        userService.deleteUser(user);
    }

    /* 거래 후기 조회 (남이 나에게) */
    @GetMapping("/users/reviews")
    public ResponseEntity<List<ReviewReadDTO>> getAllReview(){
        User user = userRepository.findById(1L).get();
        return new ResponseEntity<>(userService.getAllReview(user),HttpStatus.OK);
    }

    /* 나눔 상품 목록 조회 (모든 거래) */
    @GetMapping("/users/products")
    public ResponseEntity<List<ProductAllDTO>> getAllReceiveProduct(){
        User user = userRepository.findById(1L).get();
        return new ResponseEntity<>(userService.getAllReceiveProduct(user), HttpStatus.OK);
    }

    /* 매칭 목록 (현재 진행중 "나눔" 목록) */
    @GetMapping("/users/matches")
    public ResponseEntity<List<ProductAllDTO>> getMatchingProduct(){
        User user = userRepository.findById(1L).get();
        return new ResponseEntity<>(userService.getMatchingProduct(user), HttpStatus.OK);
    }



}
