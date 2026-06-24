package frauas.teilr.service;

import frauas.teilr.entity.User;
import frauas.teilr.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /** Find a user by their 4-digit ID. */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User register(User user) {
        if (user.getId() < 0 || user.getId() > 9999) {
            throw new IllegalArgumentException("User ID must be between 0000 and 9999.");
        }
        if (userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User ID " + user.getId() + " is already taken.");
        }
        return userRepository.save(user);
    }
}
