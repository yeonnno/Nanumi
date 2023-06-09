package com.ssafy.nanumi.api.service;

import com.ssafy.nanumi.api.response.MatchInterface;
import com.ssafy.nanumi.api.response.MatchResDTO;
import com.ssafy.nanumi.config.response.exception.CustomException;
import com.ssafy.nanumi.db.entity.Match;
import com.ssafy.nanumi.db.entity.Product;
import com.ssafy.nanumi.db.entity.User;
import com.ssafy.nanumi.db.repository.MatchRepository;
import com.ssafy.nanumi.db.repository.ProductRepository;
import com.ssafy.nanumi.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ssafy.nanumi.config.response.exception.CustomExceptionStatus.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public long getMachId(long opponentId, long productId, long userId){
        User user =  userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));
        Match match = matchRepository.getMatchId(opponentId, productId, userId).orElseThrow( () -> new CustomException(NOT_FOUND_MATCH));

        return match.getId();
    }

    public List<MatchResDTO> getMatchList(long productId, long userId){
        User user =  userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));
        if(product.getUser().getId() != user.getId()) throw new CustomException(INVALID_USER);

        ArrayList<MatchInterface> matches = matchRepository.getMatchListByProduct(product.getId());
        return matches.stream()
                .map(match -> new MatchResDTO(
                        match.getUserId(),
                        match.getUserNickName(),
                        match.getProfileUrl(),
                        match.getProductId(),
                        match.getMatchId(),
                        match.getCreateDate()
                ))
                .collect(Collectors.toList());
    }
}
