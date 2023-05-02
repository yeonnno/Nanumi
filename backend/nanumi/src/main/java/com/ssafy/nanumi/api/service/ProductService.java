package com.ssafy.nanumi.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ssafy.nanumi.api.request.ProductInsertDTO;
import com.ssafy.nanumi.api.response.MatchSuccessDto;
import com.ssafy.nanumi.api.response.ProductAllDTO;
import com.ssafy.nanumi.api.response.ProductDetailDTO;
import com.ssafy.nanumi.api.response.ProductSearchResDTO;
import com.ssafy.nanumi.config.response.exception.CustomException;
import com.ssafy.nanumi.config.response.exception.CustomExceptionStatus;
import com.ssafy.nanumi.db.entity.*;
import com.ssafy.nanumi.db.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ssafy.nanumi.config.response.exception.CustomExceptionStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AddressRepository addressRepository;
    private final ProductImageRepository productImageRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    public ProductSearchResDTO searchProductByWords(long userId, String words, PageRequest pageRequest){
        User user = userRepository.findById(userId).orElseThrow(() ->  new CustomException(NOT_FOUND_USER));
        Address address = addressRepository.findById(user.getAddress().getId()).orElseThrow( () ->  new CustomException(NOT_FOUND_ADDRESS_CODE));

        Page<Product> pages = productRepository.searchAll(address.getId(), words, pageRequest);
        List<ProductAllDTO> data = new ArrayList<>();

        for (Product p : pages.getContent()) {
            data.add(new ProductAllDTO(p));
        }

        return new ProductSearchResDTO(data, pages.getTotalPages(), pageRequest.getPageNumber());
    }

    public Page<ProductAllDTO> findProductAll(User user, PageRequest pageRequest) {
        userRepository.findById(user.getId()).orElseThrow( () -> new CustomException(NOT_FOUND_USER));
        Long addressId = user.getAddress().getId();
        return productRepository.findAllProduct(addressId, pageRequest);
    }

    public ProductDetailDTO findByProductId(Long productId) {
        return productRepository.findById(productId)
                .map(ProductDetailDTO::new)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
    }

    public Page<ProductAllDTO> findCateProductAll(Long categoryId, User user, Pageable pageRequest) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(CustomExceptionStatus.NOT_FOUND_CATEGORY));
        Long addressId = user.getAddress().getId();
        return productRepository.findAllCategoryProuduct(addressId,categoryId, pageRequest);
}

    public void createProduct(MultipartFile[] images, ProductInsertDTO request, User user) throws IOException {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_CATEGORY));
        Address address = user.getAddress();
        Product product = Product.builder()
                .name(request.getName())
                .content(request.getContent())
                .isClosed(false)
                .isDeleted(false)
                .user(user)
                .category(category)
                .address(address)
                .build();
        Product createProduct = productRepository.save(product);

        for(MultipartFile file : images) {
            String s3FileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

            ObjectMetadata objMeta = new ObjectMetadata();
            objMeta.setContentLength(file.getInputStream().available());

            amazonS3.putObject(bucket, s3FileName, file.getInputStream(), objMeta);

            String imageString = amazonS3.getUrl(bucket, s3FileName).toString();

            ProductImage productImage = ProductImage.builder()
                    .imageUrl(imageString)
                    .product(createProduct)
                    .build();
            productImageRepository.save(productImage);
        }
    }
    public void updateProduct(ProductInsertDTO request, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_CATEGORY));
        product.setName(request.getName());
        product.setContent(request.getContent());
        product.setCategory(category);
    }
    public void deleteProduct(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
        product.delete();
    }
    public MatchSuccessDto applicationProduct(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
        if ((long) product.getMatches().size() < 3){
            Match match = Match.builder()
                    .isMatching(false)
                    .product(product)
                    .user(user)
                    .build();
            Match newMatch = matchRepository.save(match);
            return MatchSuccessDto.builder()
                    .result(true)
                    .resultMessage("신청 되었습니다.")
                    .matchId(newMatch.getId())
                    .build();
        }
        else {
            product.setClosed(true);
            return MatchSuccessDto.builder()
                    .result(false)
                    .resultMessage("인원이 다 찼습니다.")
                    .matchId(null)
                    .build();
        }
    }
}
