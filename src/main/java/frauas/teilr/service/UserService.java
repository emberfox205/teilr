package frauas.teilr.service;

import frauas.teilr.entity.User;
import frauas.teilr.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
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
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        java.util.Set<Long> existingIds = userRepository.findAllIds();
        if (existingIds.size() >= 10000) {
            throw new IllegalStateException("Maximum user capacity reached (10,000 users).");
        }

        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim());

        // Generate a random 4-digit ID and use in-memory linear probing to find an empty slot
        // This guarantees exactly 1 database query regardless of how full the database is.
        java.util.Random random = new java.util.Random();
        Long generatedId = (long) random.nextInt(10000); // 0 to 9999
        
        while (existingIds.contains(generatedId)) {
            generatedId = (generatedId + 1) % 10000;
        }
        
        user.setId(generatedId);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already taken.");
        }
        user.setPasswordHash(BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt()));
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String identifier, String rawPassword) {
        Optional<User> userOpt;
        try {
            Long id = Long.parseLong(identifier);
            userOpt = userRepository.findById(id);
        } catch (NumberFormatException e) {
            userOpt = userRepository.findByEmail(identifier);
        }
        
        return userOpt.filter(user -> BCrypt.checkpw(rawPassword, user.getPasswordHash()));
    }
}
