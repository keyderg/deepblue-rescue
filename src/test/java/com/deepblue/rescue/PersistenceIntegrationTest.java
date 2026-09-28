package com.deepblue.rescue;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("deepblue_test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");

    @Autowired
    RescueCenterRepository rescueCenterRepository;

    @Autowired
    RescueCaseRepository rescueCaseRepository;

    @Autowired
    AnimalRepository animalRepository;

    @Autowired
    SpecialistRepository specialistRepository;

    @Autowired
    ExpertiseRepository expertiseRepository;

    @Autowired
    TreatmentRepository treatmentRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void flywayShouldHaveExecutedMigrations() {
        var versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true",
                String.class
        );

        assertThat(versions).contains("1", "2");
    }

    @Test
    void shouldSaveAndFindRescueCenterUsingInheritedMethods() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta");

        RescueCenter saved = rescueCenterRepository.save(center);

        assertThat(rescueCenterRepository.findById(saved.getId())).isPresent();
        assertThat(rescueCenterRepository.existsById(saved.getId())).isTrue();
        assertThat(rescueCenterRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldPersistOneToManyRelationshipBetweenCenterAndCases() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta");

        RescueCase case1 = new RescueCase("RES-2026-001", LocalDate.now(), "Playa Salguero", RescueStatus.ADMITTED);
        RescueCase case2 = new RescueCase("RES-2026-002", LocalDate.now(), "Bahía Concha", RescueStatus.ADMITTED);

        center.addCase(case1);
        center.addCase(case2);

        rescueCenterRepository.save(center);
        rescueCaseRepository.save(case1);   // <-- agregar
        rescueCaseRepository.save(case2);   // <-- agregar

        List<RescueCase> cases = rescueCaseRepository.findByRescueCenterCode("DB-CAR");

        assertThat(cases).hasSize(2);
        assertThat(cases).allMatch(c -> c.getRescueCenter().getCode().equals("DB-CAR"));
    }

    @Test
    void shouldPersistOneToOneRelationshipBetweenCaseAndAnimal() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta");

        RescueCase rescueCase = new RescueCase("RES-2026-001", LocalDate.now(), "Playa Salguero", RescueStatus.ADMITTED);
        center.addCase(rescueCase);

        Animal animal = new Animal("AN-2026-001", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);

        rescueCenterRepository.save(center);
        rescueCaseRepository.save(rescueCase);
        animalRepository.save(animal);

        RescueCase savedCase = rescueCaseRepository.findByCaseCode("RES-2026-001").orElseThrow();
        Animal savedAnimal = animalRepository.findByAnimalCode("AN-2026-001").orElseThrow();

        assertThat(savedCase.getAnimal().getAnimalCode()).isEqualTo("AN-2026-001");
        assertThat(savedAnimal.getRescueCase().getCaseCode()).isEqualTo("RES-2026-001");
    }

    @Test
    void shouldPersistOneToOneRelationshipBetweenAnimalAndMedicalRecordUsingCascade() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta");
        RescueCase rescueCase = new RescueCase("RES-2026-002", LocalDate.now(), "Rodadero", RescueStatus.ADMITTED);
        center.addCase(rescueCase);

        Animal animal = new Animal("AN-2026-002", "Loggerhead Turtle", "Caretta caretta", AnimalSex.MALE);
        rescueCase.assignAnimal(animal);

        MedicalRecord medicalRecord = new MedicalRecord(
                new BigDecimal("28.40"),
                "STABLE",
                "Left front flipper injury",
                null
        );
        animal.assignMedicalRecord(medicalRecord);

        rescueCenterRepository.save(center);
        rescueCaseRepository.save(rescueCase);

        Animal savedAnimal = animalRepository.save(animal);

        assertThat(savedAnimal.getId()).isNotNull();
        assertThat(savedAnimal.getMedicalRecord().getId()).isNotNull();
    }

    @Test
    void shouldPersistManyToManyRelationshipBetweenSpecialistAndExpertise() {
        Expertise trauma = expertiseRepository.findByNameIgnoreCase("Trauma").orElseThrow();
        Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow();

        Specialist elena = new Specialist("SP-001", "Elena", "Vargas", "elena.vargas@deepblue.com", true);
        elena.addExpertise(trauma);
        elena.addExpertise(rehabilitation);

        specialistRepository.save(elena);

        Specialist saved = specialistRepository.findById(elena.getId()).orElseThrow();

        assertThat(saved.getExpertiseAreas()).hasSize(2);
    }

    @Test
    void shouldFindRescueCasesByStatus() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta");

        RescueCase case1 = new RescueCase("RES-001", LocalDate.now(), "Playa Salguero", RescueStatus.IN_REHABILITATION);
        RescueCase case2 = new RescueCase("RES-002", LocalDate.now(), "Bahía Concha", RescueStatus.READY_FOR_RELEASE);
        RescueCase case3 = new RescueCase("RES-003", LocalDate.now(), "Rodadero", RescueStatus.IN_REHABILITATION);

        center.addCase(case1);
        center.addCase(case2);
        center.addCase(case3);

        rescueCenterRepository.save(center);
        rescueCaseRepository.save(case1);
        rescueCaseRepository.save(case2);
        rescueCaseRepository.save(case3);

        List<RescueCase> inRehabilitation = rescueCaseRepository.findByStatusOrderByRescueDateAsc(RescueStatus.IN_REHABILITATION);

        assertThat(inRehabilitation).hasSize(2);
    }

    @Test
    void shouldFindAnimalsByRescueCenter() {
        RescueCenter caribbean = new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta");
        RescueCenter pacific = new RescueCenter("DB-PAC", "DeepBlue Pacific Center", "Buenaventura");

        RescueCase caseCar = new RescueCase("RES-CAR-001", LocalDate.now(), "Playa Salguero", RescueStatus.ADMITTED);
        RescueCase casePac = new RescueCase("RES-PAC-001", LocalDate.now(), "Bahía Málaga", RescueStatus.ADMITTED);

        caribbean.addCase(caseCar);
        pacific.addCase(casePac);

        Animal animalCar = new Animal("AN-CAR-001", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        Animal animalPac = new Animal("AN-PAC-001", "Olive Ridley Turtle", "Lepidochelys olivacea", AnimalSex.MALE);

        caseCar.assignAnimal(animalCar);
        casePac.assignAnimal(animalPac);

        rescueCenterRepository.save(caribbean);
        rescueCenterRepository.save(pacific);
        rescueCaseRepository.save(caseCar);
        rescueCaseRepository.save(casePac);
        animalRepository.save(animalCar);
        animalRepository.save(animalPac);

        List<Animal> caribbeanAnimals = animalRepository.findByRescueCaseRescueCenterCode("DB-CAR");

        assertThat(caribbeanAnimals).hasSize(1);
        assertThat(caribbeanAnimals).extracting(Animal::getAnimalCode).containsExactly("AN-CAR-001");
    }
}