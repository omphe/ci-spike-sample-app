package uk.gov.ida.dcsclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.dcsclient.config.DcsClientConfiguration;
import uk.gov.ida.dcsclient.dto.Result;
import uk.gov.ida.dcsclient.testutils.DcsClientApplicationTestBase;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DcsClientApplicationTest extends DcsClientApplicationTestBase {
    @Rule
    public final DropwizardAppRule<DcsClientConfiguration> dcsClientApplication =
            new DropwizardAppRule<>(
                    DcsClientApplication.class,
                    ResourceHelpers.resourceFilePath("test-dcs-client.yml"),
                    ConfigOverride.config("dcsUrl", stubDcs.baseUri().toString() + "/checks/driving-licence"));

    @Test
    public void checkEvidenceShouldSendASecuredMessageAndReadSecuredResponseFromDCS() throws IOException {
        Client client = new JerseyClientBuilder().build();
        ObjectMapper jsonMapper = new ObjectMapper();

        Response response = client.target(
                String.format("http://localhost:%d/check-evidence", dcsClientApplication.getLocalPort()))
                .request()
                .post(Entity.json("hello world"));

        String expectedResult = jsonMapper.writeValueAsString(new Result(200, "you sent me: hello world", ""));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo(expectedResult);
    }
}
