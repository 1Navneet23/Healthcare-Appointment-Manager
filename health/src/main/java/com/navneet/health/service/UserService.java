package com.navneet.health.service;
import com.navneet.health.config.JwtUtil;
import com.navneet.health.entity.User;
import com.navneet.health.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User registerUser(User user){
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    public String login(String email,String rawPassword){
        User user =userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("Invalid email or password"));
        if(!passwordEncoder.matches(rawPassword, user.getPassword())){
            throw new RuntimeException("Invalid email or password");

        }
        return jwtUtil.generateToken(user.getEmail(),user.getRole().name(),user.getId());
    }
}
