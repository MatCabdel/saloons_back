package com.backend_project_template.e2e;

import com.backend_project_template.domains.saloon.SaloonDTO;
import com.backend_project_template.integration.AbstractIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SaloonE2ETest extends AbstractIT {

    @Test
    void shouldRetrieveSaloonById() {
        SaloonDTO saloon =
                given()
                        .when()
                        .get("/saloon/1")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(SaloonDTO.class);

        assertThat(saloon.getId()).isEqualTo(1L);
        assertThat(saloon.getName()).isNotNull();
        assertThat(saloon.getImgUrl()).isNotNull();
    }
}
