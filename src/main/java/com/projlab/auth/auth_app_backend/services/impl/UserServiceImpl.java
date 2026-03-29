package com.projlab.auth.auth_app_backend.services.impl;

import com.projlab.auth.auth_app_backend.dtos.UserDto;
import com.projlab.auth.auth_app_backend.entities.Provider;
import com.projlab.auth.auth_app_backend.entities.User;
import com.projlab.auth.auth_app_backend.exceptions.ResourceNotFoundException;
import com.projlab.auth.auth_app_backend.helpers.UserHelper;
import com.projlab.auth.auth_app_backend.repositories.UserRepository;
import com.projlab.auth.auth_app_backend.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if(userDto.getEmail()==null || userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email is Required");
        }
        if (userRepository.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("User with given email already exists");
        }

        //Take every field in UserDto and copy it into User if the names match.
        User user = modelMapper.map(userDto, User.class);
        user.setProvider(userDto.getProvider()!=null ? userDto.getProvider(): Provider.LOCAL);
        //role assign here to new user for authorization
        User saverUser = userRepository.save(user);
        //“Convert saverUser (User) → UserDto” Take my saved database object and turn it into a clean response object.
        return modelMapper.map(saverUser, userDto.getClass());

    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("user not found with given Email"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        User existingUser = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(()-> new ResourceNotFoundException("user not found for the provided userid"));

        if(userDto.getName()!=null) existingUser.setName(userDto.getName());
        if(userDto.getImage()!=null) existingUser.setImage(userDto.getImage());
        if(userDto.getProvider()!=null) existingUser.setProvider(userDto.getProvider());

        //todo change the pw updation later
        if(userDto.getPassword()!=null) existingUser.setPassword(userDto.getPassword());

        existingUser.setEnable(userDto.isEnable());
        User updatedUser = userRepository.save(existingUser);

        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID UID= UserHelper.parseUUID(userId);
        User user = userRepository.findById(UID).orElseThrow(()-> new ResourceNotFoundException("user not found for the provided userid"));
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(()-> new ResourceNotFoundException("user not found for the provided userid"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public List<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user ->modelMapper.map(user,UserDto.class))  //For each user:  Convert User → UserDto
                .toList();
    }
}
