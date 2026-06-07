package com.subbu.minishop.cartservice.cart;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> getCart(Long userId) {
        return cartItemRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Transactional
    public CartItem addItem(CartItem cartItem) {
        CartItem newItem = new CartItem(
                cartItem.getUserId(),
                cartItem.getProductId(),
                cartItem.getQuantity()
        );
        return cartItemRepository.save(newItem);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!cartItemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        cartItemRepository.deleteById(id);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
