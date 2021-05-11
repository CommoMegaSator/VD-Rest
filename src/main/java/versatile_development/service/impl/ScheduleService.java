package versatile_development.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import versatile_development.entity.UserEntity;
import versatile_development.repository.UserRepository;
import versatile_development.service.UserService;

import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class ScheduleService {

    private UserRepository userRepository;
    private UserService userService;

    ScheduleService(UserRepository userRepository,
                    UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteAllUsersWithExpiredActivation(){
        List<UserEntity> users = userRepository.findAllByTokenExpirationLessThanCurrentTime();
        log.info("Starting deleting all non activated accounts...");

        for (UserEntity user : users) {
            if (!user.isActivated())
                userService.deleteAccountByNickname(user.getNickname());
        }
        log.info("All non activated accounts were deleted successfully.");
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void ageIncrementer(){
        Integer day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Integer month = Calendar.getInstance().get(Calendar.MONTH);
        List<UserEntity> users = userRepository.findAllByBirthday(day, month);
        int userNumber = 0;

        if (!users.isEmpty()) {
            for(UserEntity user : users) {
                int newAge = user.getAge();
                ++userNumber;

                user.setAge(++newAge);
                userRepository.save(user);
            }
            if (userNumber == 1) log.info("Only one user was born at this day!");
            else if (userNumber > 1) log.info(userNumber + " users were born at this day!");
        } else log.info("No one was born at this day!");
    }
}