package versatile_development.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import versatile_development.domain.UserDTO;
import versatile_development.entity.UserEntity;
import versatile_development.repository.UserRepository;
import versatile_development.service.UserService;
import versatile_development.utils.ObjectMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapperUtils modelMapper;

    @Override
    public void createUser(UserDTO userDTO) {
        UserEntity userEntity = DTOToEntityMapper(userDTO);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userRepository.save(userEntity);
    }

    @Override
    public List<UserDTO> findAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        List<UserDTO> userDTOs = new ArrayList<>();

        for (UserEntity userEntity : userEntities){
            userDTOs.add(entityToDTOMapper(userEntity));
        }

        return userDTOs;
    }

    @Override
    public UserDTO findByEmail(String email){
        return entityToDTOMapper(userRepository.findByEmailIgnoreCase(email));
    }

    @Override
    public boolean userExists(UserDTO userDTO) {
        return userDTO != null &&
                (userRepository.existsByEmailIgnoreCase(userDTO.getEmail()) || userRepository.existsByNickname(userDTO.getNickname()));
    }

    @Override
    public UserDTO findByConfirmationToken(String confirmationToken) {
        return entityToDTOMapper(userRepository.findByConfirmationToken(confirmationToken));
    }

    @Override
    public UserDTO findByNickname(String nickname) {
        return entityToDTOMapper(userRepository.findByNickname(nickname));
    }

    @Override
    public void updateUser(UserDTO userToUpdate) {
        if(findByEmail(userToUpdate.getEmail()) != null){
            userRepository.save(DTOToEntityMapper(userToUpdate));
        }
    }

    public UserDTO entityToDTOMapper(UserEntity userEntity){
        if (userEntity != null){
            UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
            userDTO.setId(userEntity.getId());
            return userDTO;
        }else return null;
    }

    public UserEntity DTOToEntityMapper(UserDTO userDTO){
        if (userDTO == null)return null;
        else return modelMapper.map(userDTO, UserEntity.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByNickname(username);
    }
}