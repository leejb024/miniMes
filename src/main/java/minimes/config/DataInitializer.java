package minimes.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import minimes.auth.domain.User;
import minimes.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(ApplicationArguments args) {
		if (userRepository.existsByUserId("admin")) {
			return;
		}

		User admin = User.builder()
				.userId("admin")
				.userNm("관리자")
				.password(passwordEncoder.encode("admin123"))
				.useYn("Y")
				.build();
		userRepository.save(admin);
		log.info("초기 관리자 계정을 생성했습니다. userId=admin");
	}
}
