package io.student.rangiffler;


import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.jupiter.annotation.CloseConnections;
import io.student.rangiffler.model.UserJson;
import io.student.rangiffler.service.UserDbClient;
import io.student.rangiffler.service.UserDbClientHibernate;
import io.student.rangiffler.service.UserDbClientRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.UUID;

import static io.student.rangiffler.data.UserData.STANDART_PASSWORD;

@CloseConnections
public class CreateAuthorityDbTest {

    private final Faker faker = new Faker();
    UserDbClientHibernate userDbClientHibernate = new UserDbClientHibernate();


    @ParameterizedTest
    @ValueSource(strings = {
            "userA_1769948637729","userB_1769948637730"
    })
    public void createHibernateTest(String username) throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("-----------------");
        System.out.println(Thread.currentThread().toString());
        System.out.println("-----------------");
        System.out.println(username);
        System.out.println("-----------------");
        System.out.println("-----------------");
        System.out.println(userDbClientHibernate.createUserRepositoryHibernate(username, STANDART_PASSWORD));
    }

    @Test
    public void createHibernateRepositoryUserTest() {
        UUID countryId = UUID.fromString("11f1070f-a2a0-6785-83d6-0242ac110002");

        UserEntity userA = new UserEntity();
        userA.setUsername("userA_" + System.currentTimeMillis());
        userA.setFirstname("A");
        userA.setLastName("Test");
        userA.setAvatar(new byte[0]);
        userA.setCountry(CountryEntity.builder().id(countryId).build());

        UserEntity userB = new UserEntity();
        userB.setUsername("userB_" + System.currentTimeMillis());
        userB.setFirstname("B");
        userB.setLastName("Test");
        userB.setAvatar(new byte[0]);
        userB.setCountry(CountryEntity.builder().id(countryId).build());

        // 1. create users in rangiffler-api.user
        userDbClientHibernate.createUserdataUser(userA);
        userDbClientHibernate.createUserdataUser(userB);

        System.out.println("Created userA: " + userA.getUsername());
        System.out.println("Created userB: " + userB.getUsername());

        // 2. A -> B invitation
        userDbClientHibernate.addOutcomeInvitation(userA, userB);
        System.out.println("Invitation A -> B (PENDING)");

        // 3. B -> A invitation (edge-case)
        userDbClientHibernate.addOutcomeInvitation(userB, userA);
        System.out.println("Invitation B -> A (PENDING)");

        // 4. Accept friendship
        userDbClientHibernate.addFriend(userA, userB);
        System.out.println("Friendship accepted");


    }
}
