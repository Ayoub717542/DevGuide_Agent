package com.example.DevTutor.service;

import com.example.DevTutor.entity.PaddleCheckout;
import com.example.DevTutor.entity.User;
import com.example.DevTutor.repository.PaddleCheckoutRepository;
import com.example.DevTutor.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaddleCheckoutService {

    private final UserRepository userRepository;
    private final PaddleCheckoutRepository checkoutRepository;
    private final PaddleClient paddleClient;

    public PaddleCheckoutService(UserRepository userRepository,
                                 PaddleCheckoutRepository checkoutRepository,
                                 PaddleClient paddleClient) {
        this.userRepository = userRepository;
        this.checkoutRepository = checkoutRepository;
        this.paddleClient = paddleClient;
    }

    public String startCheckout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.hasProAccess()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have Pro");
        }

        String transactionId = paddleClient.createTransaction();

        PaddleCheckout checkout = new PaddleCheckout();
        checkout.setTransactionId(transactionId);
        checkout.setUser(user);
        checkoutRepository.saveAndFlush(checkout);

        return transactionId;
    }
}
