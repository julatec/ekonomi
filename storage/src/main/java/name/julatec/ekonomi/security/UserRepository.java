package name.julatec.ekonomi.security;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, UserId> {
}
