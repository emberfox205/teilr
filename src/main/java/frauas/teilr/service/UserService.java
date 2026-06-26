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
        // Generate a random 4-digit ID that is not already taken
        java.util.Random random = new java.util.Random();
        Long generatedId;
        do {
            generatedId = (long) random.nextInt(10000); // 0 to 9999
        } while (userRepository.existsById(generatedId));
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
